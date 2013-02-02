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
		Complex gammaL = new Complex( epsilon, 1.0/(1.0-epsilon) ).multiply( waveNumber * length );
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
		// From: Y. Kulik, Transfer matrix of conical waveguides with any geometric
		//       parameters for increased precision in computer modeling, 
		//       JASA v. 122, pp. EL179-EL184, November 2007.
		
		if ( sourceRadius == loadRadius )
		{
			return calcCylinderMatrix(waveNumber, length, sourceRadius, params);
		}

		// Complex wave vector, at source and load.
		double alpha_0 = params.getAlphaConstant()/Math.sqrt(waveNumber);
		double epsilon = alpha_0/sourceRadius;
		Complex k_in  = new Complex( 1.0/(1.0-epsilon), -epsilon ).multiply( waveNumber );
		epsilon = alpha_0/loadRadius;
		Complex k_out = new Complex( 1.0/(1.0-epsilon), -epsilon ).multiply( waveNumber );

		// Cotangents of theta_in and theta_out. 
		Complex cot_in  = new Complex(loadRadius-sourceRadius)
							.divide(k_in.multiply(sourceRadius * length));
		Complex cot_out = new Complex(loadRadius-sourceRadius)
							.divide(k_out.multiply(loadRadius * length));

		// Mean complex wave vector along the whole cone, from Kulik 2007.
		
		epsilon = alpha_0/(loadRadius - sourceRadius);
		Complex mean = new Complex( 1.0 + epsilon * Math.log((loadRadius - alpha_0)/(sourceRadius - alpha_0)),
				- epsilon * Math.log(loadRadius/sourceRadius));
		Complex kMeanL = mean.multiply(waveNumber * length);

		// sine and cosine of kMean * L, pre-multiplied by ratio of radii.
		Complex sin_kL = kMeanL.sin().multiply(loadRadius/sourceRadius);
		Complex cos_kL = kMeanL.cos().multiply(loadRadius/sourceRadius);

		Complex A = cos_kL.subtract(sin_kL.multiply(cot_out));
		Complex B = Complex.I.multiply(sin_kL).multiply(params.calcZ0(loadRadius));
		Complex C = Complex.I.multiply(sin_kL.multiply(cot_out.multiply(cot_in).add(1.0))
				.add(cos_kL.multiply(cot_out.subtract(cot_in)))).divide(params.calcZ0(sourceRadius));
		Complex D = cos_kL.add(sin_kL.multiply(cot_in));

		TransferMatrix tm = new TransferMatrix(A, B, C, D); 
		assert tm.determinant() == Complex.valueOf(1.0,0.0);
		return tm;
	}

}