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
import org.apache.commons.math3.util.FastMath;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Calculator to compute the transfer matrix of a soundhole in a round tube.
 * 
 * From Antoine Lefebvre and Gary P. Scavone, Characterization of woodwind
 * instrument toneholes with the finite element method, J. Acoust. Soc. Am. V.
 * 131 (n. 4), April 2012.
 * 
 * @author kort
 * 
 */
public class DefaultHoleCalculator extends HoleCalculator
{
	// For bare (key-less) toneholes, assume the player's finger
	// occupies a fixed length of the tonehole, in meters.
	private static double AssumedFingerSize = 0.000;
	private double mFudgeFactor = 1.0;

	public DefaultHoleCalculator()
	{
		this.mFudgeFactor = 1.0;
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
	 *
	 * Reference:
	 * Antoine Lefebvre, Computational Acoustic Methods for the Design of
     * Woodwind Instruments.  Ph.D. thesis, McGill University, 2010.
	 */
	public TransferMatrix calcTransferMatrix_2010(Hole hole, boolean isOpen,
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
				* (1. + 0.207d * delta * delta * delta);
		double te = hole.getHeight() + tm;

		double ta = 0.;

		// Complex Gamma = Complex.I.multiply(wave_number);

		if (isOpen) // open
		{
			double kb = waveNumber * radius;
			double ka = waveNumber * boreRadius;
			double xhi = 0.25d * kb * kb;

			ta = (-0.35d + 0.06d * Math.tanh(2.7d * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;

			Complex Zr = Complex.I.multiply(waveNumber * 0.61d * radius)
					.add(xhi);

			Complex Zo = (Zr.multiply(Math.cos(waveNumber * te))
					.add(Complex.I.multiply(Math.sin(waveNumber * te))))
							.divide(Complex.I.multiply(Zr)
									.multiply(Math.sin(waveNumber * te))
									.add(Math.cos(waveNumber * te)));

			double ti = radius
					* (0.822d - 0.10d * delta - 1.57d * delta * delta
							+ 2.14d * delta * delta * delta
							- 1.6d * delta * delta * delta * delta
							+ 0.50d * delta * delta * delta * delta * delta)
					* (1. + (1. - 4.56d * delta + 6.55d * delta * delta)
							* (0.17d * ka + 0.92d * ka * ka + 0.16d * ka * ka * ka
									- 0.29d * ka * ka * ka * ka));

			Zs = Complex.I.multiply(waveNumber * ti).add(Zo).multiply(Z0h);

		}
		else
		{
			ta = (-0.12d - 0.17d * Math.tanh(2.4d * hole.getHeight() / radius))
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 *
	 * Reference: Antoine Lefebvre and Gary P. Scavone, Characterization of
	 * woodwind instrument toneholes with the finite element method, J. Acoust.
	 * Soc. Am. V. 131 (n. 4), April 2012.
	 */
	@Override
	public TransferMatrix calcTransferMatrix(Hole hole, boolean isOpen,
			double waveNumber, PhysicalParameters parameters)
	{
		double radius = mFudgeFactor * hole.getDiameter() / 2.;
		double boreRadius = hole.getBoreDiameter() / 2.;
		Complex Ys = Complex.ZERO; // Shunt admittance == 1/Zs
		Complex Za = Complex.ZERO; // Series impedance

		double Z0h = parameters.calcZ0(radius); // Characteristic impedance of hole.
		double delta = radius / boreRadius;
		double delta2 = delta * delta;

		// Equation 8.
		double tm = (radius * delta / 8.) * (1. + 0.207d * delta * delta2);
		double te = hole.getHeight() + tm;
		// Equation 31.
		double ti = radius
				* (0.822d + delta * (-0.095d + delta * (-1.566d + delta
						* (2.138d + delta * (-1.640d + delta * 0.502d)))));

		double ta = 0.;

		if (isOpen)
		{
			double kb = waveNumber * radius;
			double ka = waveNumber * boreRadius;

			// Equation 33.
			ta = (-0.35d + 0.06d * FastMath.tanh(2.7d * hole.getHeight() / radius))
					* radius * delta2;

			// Equation 31 times equation 32.
			ti = ti	* (1. + (1. - 4.56d * delta + 6.55d * delta2) * ka
							* (0.17d + ka * (0.92d + ka * (0.16d - 0.29d * ka))));

			// Normalized radiation resistance, real part of Zs, per equation 3,
			// (rather than real part of Zr in equation 10).
			double Rr = 0.25d * kb * kb;
			// Radiation length correction (equation 10 with Zr/Z0h = jk*tr
			// without real part).
			// Equation 11 times radius.
			double tr = radius * (0.822d - 0.47d * FastMath
					.pow(radius / (boreRadius + hole.getHeight()), 0.8d));

			// Equation 3 and 7, inverted.
			double kttotal = waveNumber * ti + FastMath.tan(waveNumber * (te + tr));
			Ys = Complex.ONE.divide(
					Complex.I.multiply(kttotal).add(Rr).multiply(Z0h));

		}
		else if (hole.getKey() == null)
		{
			// Tonehole closed by player's finger.
			if (hole.getHeight() <= AssumedFingerSize)
			{
				// Finger is likely to fill the hole. Ignore the hole entirely.
				ta = 0.;
				Ys = Complex.ZERO;
			}
			else
			{
				// Equation 34.
				ta = (-0.12d - 0.17d * FastMath.tanh(
						2.4d * (hole.getHeight() - AssumedFingerSize) / radius))
						* radius * delta2;
				// Equation 16, inverted.
				double kttotal = waveNumber * ti
						- 1.0/FastMath.tan(waveNumber * (te - AssumedFingerSize));
				Ys = Complex.valueOf(0.0, - 1.0/(Z0h * kttotal));
			}
		}
		else
		{
			// Tonehole closed by key, not yet implemented.
			ta = (-0.12d - 0.17d * FastMath.tanh(2.4d * hole.getHeight() / radius))
					* radius * delta2;
			Ys = Complex.valueOf(0.0, FastMath.tan(waveNumber * te) / Z0h);
		}

		// Equation 4, 6.
		// double Z0 = parameters.calcZ0(boreRadius);
		// Z0 == Z0h * delta*delta
		Za = Complex.I.multiply(Z0h * delta2 * waveNumber * ta);
		Complex Za_Zs = Za.multiply(Ys);

		// Transfer matrix (equation 2).
		Complex A = Za_Zs.divide(2.).add(1.);
		Complex B = Za.multiply(Za_Zs.divide(4.).add(1.));
		Complex C = Ys;
		TransferMatrix result = new TransferMatrix(A, B, C, A);

		// assert result.determinant() == Complex.ONE;

		return result;
	}

}
