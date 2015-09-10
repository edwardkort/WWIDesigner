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
	public TransferMatrix calcTransferMatrix_2010(Hole hole, double waveNumber,
			PhysicalParameters parameters)
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

			Complex Zo = (Zr.multiply(Math.cos(waveNumber * te))
					.add(Complex.I.multiply(Math.sin(waveNumber * te))))
							.divide(Complex.I.multiply(Zr)
									.multiply(Math.sin(waveNumber * te))
									.add(Math.cos(waveNumber * te)));

			double ti = radius
					* (0.822 - 0.10 * delta - 1.57 * delta * delta
							+ 2.14 * delta * delta * delta
							- 1.6 * delta * delta * delta * delta
							+ 0.50 * delta * delta * delta * delta * delta)
					* (1. + (1. - 4.56 * delta + 6.55 * delta * delta)
							* (0.17 * ka + 0.92 * ka * ka + 0.16 * ka * ka * ka
									- 0.29 * ka * ka * ka * ka));

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
	public TransferMatrix calcTransferMatrix(Hole hole, double waveNumber,
			PhysicalParameters parameters)
	{
		double radius = mFudgeFactor * hole.getDiameter() / 2;
		double boreRadius = hole.getBoreDiameter() / 2;
		Complex Ys = Complex.ZERO; // Shunt admittance == 1/Zs
		Complex Za = Complex.ZERO; // Series impedance

		double Z0h = parameters.calcZ0(radius);
		double delta = radius / boreRadius;
		double delta2 = delta * delta;
		// double Z0 = parameters.calcZ0(boreRadius);
		// Z0 == Z0h * delta*delta

		double tm = (radius * delta / 8.) * (1. + 0.207 * delta * delta2);
		double te = hole.getHeight() + tm;

		double ta = 0.;

		// Complex Gamma = Complex.I.multiply(wave_number);

		if (hole.isOpenHole()) // open
		{
			double kb = waveNumber * radius;
			double ka = waveNumber * boreRadius;

			ta = (-0.35 + 0.06 * FastMath.tanh(2.7 * hole.getHeight() / radius))
					* radius * delta2;

			Complex Zr = new Complex(0.25 * kb * kb,
					(0.822 - 0.47 * FastMath
							.pow(radius / (boreRadius + hole.getHeight()), 0.8))
					* waveNumber * radius);
			double cos = FastMath.cos(waveNumber * te);
			Complex jsin = new Complex(0, FastMath.sin(waveNumber * te));

			Complex Zo = (Zr.multiply(cos).add(jsin))
					.divide(Zr.multiply(jsin).add(cos));

			double ti = radius
					* (0.822 + delta * (-0.095 + delta * (-1.566 + delta
							* (2.138 + delta * (-1.640 + delta * 0.502)))))
					* (1. + (1. - 4.56 * delta + 6.55 * delta2) * ka
							* (0.17 + ka * (0.92 + ka * (0.16 - 0.29 * ka))));

			Ys = Complex.ONE.divide(
					Complex.I.multiply(waveNumber * ti).add(Zo).multiply(Z0h));

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
				ta = (-0.12 - 0.17 * FastMath.tanh(
						2.4 * (hole.getHeight() - AssumedFingerSize) / radius))
						* radius * delta2;
				Ys = Complex.valueOf(0,
						FastMath.tan(waveNumber * (te - AssumedFingerSize))
								/ Z0h);
			}
		}
		else
		{
			// Tonehole closed by key.
			ta = (-0.12 - 0.17 * FastMath.tanh(2.4 * hole.getHeight() / radius))
					* radius * delta2;
			Ys = Complex.valueOf(0, FastMath.tan(waveNumber * te) / Z0h);
		}

		Za = Complex.I.multiply(Z0h * delta2 * waveNumber * ta);
		Complex Za_Zs = Za.multiply(Ys);

		Complex A = Za_Zs.divide(2.).add(1.);
		Complex B = Za.multiply(Za_Zs.divide(4.).add(1.));
		Complex C = Ys;
		// Choose A and D to make the determinant = 1.
		// Complex A = Complex.ONE.add(B.multiply(C)).sqrt();
		TransferMatrix result = new TransferMatrix(A, B, C, A);

		assert result.determinant() == Complex.valueOf(1.0, 0.0);

		return result;
	}

}
