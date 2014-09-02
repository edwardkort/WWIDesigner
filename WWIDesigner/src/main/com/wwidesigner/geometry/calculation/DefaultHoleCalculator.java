/**
 * Calculator to compute the transfer matrix of a soundhole in a round tube.
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

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Calculator to compute the transfer matrix of a soundhole in a round tube.
 * 
 * From Antoine Lefebvre and Gary P. Scavone, Characterization of woodwind instrument
 * toneholes with the finite element method, J. Acoust. Soc. Am. V. 131 (n. 4), April 2012.
 * 
 * @author kort
 * 
 */
public class DefaultHoleCalculator extends HoleCalculator
{
    private double mFudgeFactor = 1.0;
    
    public DefaultHoleCalculator()
    {
    }
    
	public DefaultHoleCalculator(double fudgeFactor)
	{
		mFudgeFactor = fudgeFactor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(Hole hole,
			double waveNumber, PhysicalParameters parameters)
	{
		double radius = mFudgeFactor * hole.getDiameter() / 2;
		double boreRadius = hole.getBoreDiameter() / 2;
		Complex Zs = null;
		Complex Za = null;

		// double Z0 = parameters.calcZ0(boreRadius);
		double Z0h = parameters.calcZ0(radius);

		double delta = radius / boreRadius;

		double tm = (radius * delta / 8.)
				* (1. + 0.207 * delta * delta * delta);
		double te = hole.getHeight() + tm;

		double ta = 0.;

		// Complex Gamma = Complex.I.multiply(wave_number);

		if (hole.isOpenHole()) // open
		{
			double kb = waveNumber * radius;
			double ka = waveNumber * boreRadius;
			double xhi = 0.25 * kb * kb;

			ta = (-0.35 + 0.06 * Math.tanh(2.7 * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;

			Complex Zr = Complex.I.multiply(waveNumber * 0.61 * radius)
					.add(xhi);

			Complex Zo = (Zr.multiply(Math.cos(waveNumber * te)).add(Complex.I
					.multiply(Math.sin(waveNumber * te)))).divide(Complex.I
					.multiply(Zr).multiply(Math.sin(waveNumber * te))
					.add(Math.cos(waveNumber * te)));

			double ti = radius
					* (0.822 - 0.10 * delta - 1.57 * delta * delta + 2.14
							* delta * delta * delta - 1.6 * delta * delta
							* delta * delta + 0.50 * delta * delta * delta
							* delta * delta)
					* (1. + (1. - 4.56 * delta + 6.55 * delta * delta)
							* (0.17 * ka + 0.92 * ka * ka + 0.16 * ka * ka * ka - 0.29
									* ka * ka * ka * ka));

			Zs = Complex.I.multiply(waveNumber * ti).add(Zo).multiply(Z0h);

		}
		else
		{
			ta = (-0.12 - 0.17 * Math.tanh(2.4 * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;
			Zs = Complex.valueOf(0, -Z0h / Math.tan(waveNumber * te));
		}

		Za = Complex.I.multiply(Z0h * waveNumber * ta);
		Complex Za_Zs = Za.divide(Zs);

		TransferMatrix result = new TransferMatrix(Za_Zs.divide(2.).add(1.),
				Za.multiply(Za_Zs.divide(4.).add(1.)), Complex.ONE.divide(Zs),
				Za_Zs.divide(2.0).add(1.));
		
		assert result.determinant() == Complex.ONE;

		return result;
	}

}
