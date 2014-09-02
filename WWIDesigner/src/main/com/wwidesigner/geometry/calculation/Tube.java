/**
 * Class to calculate transmission matrices for tubular waveguides.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Class to calculate transmission matrices for tubular waveguides.
 * @author Burton Patkau
 *
 */
public class Tube
{
	public Tube()
	{
	}

    /**
     * Calculate the impedance of an unflanged open end of a real pipe.
     * @param freq: fundamental frequency of the waveform.
     * @param radius: radius of pipe, in metres.
     * @return impedance as seen by pipe.
     */
    public static Complex calcZload( double freq, double radius, PhysicalParameters params )
    {
    	Complex zRel = new Complex(9.87 * freq * radius / params.getSpeedOfSound(), 3.84 )
    						.multiply( freq * radius / params.getSpeedOfSound() );
    	return zRel.multiply( params.calcZ0(radius) );
    }

    /**
     * Calculate the impedance of an open end of a real pipe,
     * assuming an infinite flange.
     * @param freq: fundamental frequency of the waveform.
     * @param radius: radius of pipe, in metres.
     * @return impedance as seen by pipe.
     */
    public static Complex calcZflanged( double freq, double radius, PhysicalParameters params )
    {
    	Complex zRel = new Complex(19.7 * freq * radius / params.getSpeedOfSound(), 5.33 )
    					.multiply( freq * radius / params.getSpeedOfSound() );
    	return zRel.multiply( params.calcZ0(radius) );
    }
    
	/**
	 * Calculate the transfer matrix of a cylinder.
	 * @param waveNumber: 2*pi*f/c, in radians per metre
	 * @param length: length of the cylinder, in metres.
	 * @param radius: radius of the cylinder, in metres.
	 * @param params: physical parameters
	 * @return Transfer matrix
	 */
	public static TransferMatrix calcCylinderMatrix(double waveNumber, 
			double length, double radius, PhysicalParameters params)
	{
		double Zc = params.calcZ0(radius);
		double epsilon = params.getAlphaConstant()/(radius * Math.sqrt(waveNumber));
		Complex gammaL = new Complex( epsilon, 1.0+epsilon ).multiply( waveNumber * length );
		Complex coshL = gammaL.cosh();
		Complex sinhL = gammaL.sinh();
        TransferMatrix result = new TransferMatrix(coshL, sinhL.multiply(Zc), sinhL.divide(Zc), coshL);
        
		return result;
	}

	/**
	 * Calculate the transfer matrix of a conical tube.
	 * @param freq: frequency in Hz.
	 * @param length: length of the tube, in metres.
	 * @param sourceRadius: radius of source end the tube, in metres.
	 * @param loadRadius: radius of load end the tube, in metres.
	 * @param params: physical parameters
	 * @return Transfer matrix
	 */
	public static TransferMatrix calcConeMatrix(double waveNumber, 
			double length, double sourceRadius, double loadRadius, PhysicalParameters params)
	{
		// From: Antoine Lefebvre and Jean Kergomard.
		
		if ( sourceRadius == loadRadius )
		{
			return calcCylinderMatrix(waveNumber, length, sourceRadius, params);
		}

		// Mean complex wave vector along the whole cone, from Lefebvre and Kergomard.
		double alpha_0 = params.getAlphaConstant()/Math.sqrt(waveNumber);
		double epsilon;
		if (Math.abs(loadRadius - sourceRadius) <= 0.00001 * sourceRadius)
		{
			// Use limiting value as loadRadius approaches sourceRadius.
			epsilon = alpha_0/loadRadius;
		}
		else
		{
			epsilon = alpha_0/(loadRadius - sourceRadius) * Math.log(loadRadius/sourceRadius);
		}
		Complex mean = new Complex( 1.0 + epsilon, - epsilon );
		Complex kMeanL = mean.multiply(waveNumber * length);
		
		// Cotangents of theta_in and theta_out. 
		Complex cot_in  = new Complex((loadRadius-sourceRadius)/sourceRadius)
							.divide(kMeanL);
		Complex cot_out = new Complex((loadRadius-sourceRadius)/loadRadius)
							.divide(kMeanL);

		// sine and cosine of kMean * L.
		Complex sin_kL = kMeanL.sin();
		Complex cos_kL = kMeanL.cos();

		Complex A = cos_kL.multiply(loadRadius/sourceRadius).subtract(sin_kL.multiply(cot_in));
		Complex B = Complex.I.multiply(sin_kL)
				.multiply(params.calcZ0(loadRadius) * (loadRadius/sourceRadius));
		Complex C = Complex.I.multiply(loadRadius/(sourceRadius*params.calcZ0(sourceRadius))).multiply(
				sin_kL.multiply(cot_out.multiply(cot_in).add(1.0))
				.add(cos_kL.multiply(cot_out.subtract(cot_in))));
		Complex D = cos_kL.multiply(sourceRadius/loadRadius).add(sin_kL.multiply(cot_out));

		TransferMatrix tm = new TransferMatrix(A, B, C, D); 
		assert tm.determinant() == Complex.valueOf(1.0,0.0);
		return tm;
	}

}