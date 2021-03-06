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
	/**
	 * Adjustment factor in meters for finger intrusion on a closed tonehole.
	 * Use zero for no intrusion, or for specific approximations: 0.025 for
	 * effect of volume reduction from cap of 15 mm sphere, 0.020 for effect of
	 * volume reduction from cap of 13 mm dia sphere, 0.011 for height of cap of
	 * 13 mm dia sphere, 0.010 for adjustment that Paul Dickens used in his 2007
	 * thesis.
	 */
	protected double fingerAdjustment = DEFAULT_FINGER_ADJ;
	public static final double NO_FINGER_ADJ = 0.000;
	public static final double CAP_VOLUME_FINGER_ADJ = 0.020;
	public static final double CAP_HEIGHT_FINGER_ADJ = 0.011;
	public static final double DEFAULT_FINGER_ADJ = 0.010;
	public static final double DEFAULT_HOLE_SIZE_MULT = 1.0;

	protected boolean isPlugged = false;

	protected double mHoleSizeMult = 1.0;

	public DefaultHoleCalculator()
	{
		setHoleSizeMult(DEFAULT_HOLE_SIZE_MULT);
		this.isPlugged = false;
		setFingerAdjustment(DEFAULT_FINGER_ADJ);
	}

	public DefaultHoleCalculator(double holeSizeMult)
	{
		setHoleSizeMult(holeSizeMult);
		this.isPlugged = false;
		setFingerAdjustment(NO_FINGER_ADJ);
	}

	public DefaultHoleCalculator(boolean aIsPlugged, double aFingerAdj)
	{
		setHoleSizeMult(DEFAULT_HOLE_SIZE_MULT);
		this.isPlugged = aIsPlugged;
		setFingerAdjustment(aFingerAdj);
	}

	public double getFingerAdjustment()
	{
		return fingerAdjustment;
	}

	public void setFingerAdjustment(double aFingerAdj)
	{
		this.fingerAdjustment = aFingerAdj;
	}

	public double getHoleSizeMult()
	{
		return mHoleSizeMult;
	}

	public void setHoleSizeMult(double holeSizeMult)
	{
		mHoleSizeMult = holeSizeMult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 *
	 * Reference: Antoine Lefebvre, Computational Acoustic Methods for the
	 * Design of Woodwind Instruments. Ph.D. thesis, McGill University, 2010.
	 */
	public TransferMatrix calcTransferMatrix_2010(Hole hole, boolean isOpen,
			double waveNumber, PhysicalParameters parameters)
	{
		double radius = mHoleSizeMult * hole.getDiameter() / 2;
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
							* (0.17d * ka + 0.92d * ka * ka
									+ 0.16d * ka * ka * ka
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
		double radius = mHoleSizeMult * hole.getDiameter() / 2.;
		double boreRadius = hole.getBoreDiameter() / 2.;
		Complex Ys = Complex.ZERO; // Shunt admittance == 1/Zs
		Complex Za = Complex.ZERO; // Series impedance

		double Z0h = parameters.calcZ0(radius); // Characteristic impedance of
												// hole.
		double delta = radius / boreRadius;
		double delta2 = delta * delta;

		// Equation 8.
		double tm = 0.125d * radius * delta * (1. + 0.207d * delta * delta2);
		// From Equation 2.3.12 from Lefebvre 2010.
		// double outerDelta = radius / (boreRadius + hole.getHeight());
		// tm = tm - 0.125d * (radius * outerDelta)
		// * (1 + 0.207d * outerDelta * outerDelta * outerDelta);
		double te = hole.getHeight() + tm;
		// Equation 31.
		double ti = radius * (0.822d + delta * (-0.095d + delta * (-1.566d
				+ delta * (2.138d + delta * (-1.640d + delta * 0.502d)))));
		double tiCorrected = radius
				* (0.822d + delta * (-0.095d + delta * (-1.566d + delta
						* (2.138d + delta * (-1.640d + delta * 0.502d)))));

		double ta = 0.;

		if (isOpen)
		{
			double kb = waveNumber * radius;
			double ka = waveNumber * boreRadius;

			// Equation 33.
			ta = (-0.35d
					+ 0.06d * FastMath.tanh(2.7d * hole.getHeight() / radius))
					* radius * delta2;

			// Equation 31 times equation 32.
			ti = tiCorrected * (1. + (1. - 4.56d * delta + 6.55d * delta2) * ka
					* (0.17d + ka * (0.92d + ka * (0.16d - 0.29d * ka))));

			// Normalized radiation resistance, real part of Zs, per equation 3,
			// (rather than real part of Zr in equation 10).
			double Rr = 0.25d * kb * kb;
			// Radiation length correction (equation 10 with Zr/Z0h = jk*tr
			// without real part).
			// Equation 11 times radius.
			// (Both Dalmont, et al., 2001, and Dickens, 2007, obtained
			// larger values experimentally.)
			double tr = radius * (0.822d - 0.47d * FastMath
					.pow(radius / (boreRadius + hole.getHeight()), 0.8d));
			// Benade and Murday, 1967.
			// tr = 0.64d * radius * (1.0 + 0.32d *
			// FastMath.log(0.3d/outerDelta));

			// Equation 3 and 7, inverted.
			double kttotal = waveNumber * ti
					+ FastMath.tan(waveNumber * (te + tr));
			Ys = Complex.ONE
					.divide(Complex.I.multiply(kttotal).add(Rr).multiply(Z0h));

		}
		else if (isPlugged)
		{
			// Tonehole is fully plugged. Ignore the hole entirely.
			ta = 0.;
			Ys = Complex.ZERO;
		}
		else if (hole.getKey() == null)
		{
			// Tonehole closed by player's finger.
			// Equation 34, revised constants to better fit figure 13.
			// ta = (-0.12d - 0.17d * FastMath.tanh(
			ta = (-0.20d
					- 0.10d * FastMath.tanh(2.4d * hole.getHeight() / radius))
					* radius * delta2;
			double tf = 0.0;
			// Dickens, 2007, from data limited to bore radius 7.5 mm.
			// tf = 0.76d * radius * delta;
			if (fingerAdjustment > 0.0)
			{
				// Approximate curve fit.
				tf = radius * radius / fingerAdjustment;
				// if (tf > te)
				// {
				// tf = te;
				// fingerAdjustment = te / (radius * delta);
				// }
				// Estimated from volume removed by finger divided by hole area.
				// double h = FingerRadius - FastMath.sqrt(FingerRadius *
				// FingerRadius
				// - radius * radius);
				// tf = h * (3.0d + h * h / (radius * radius))/6.0d;
			}
			// Equation 16, inverted.
			double tankt = FastMath.tan(waveNumber * (te - tf));
			Ys = Complex.valueOf(0.0,
					tankt / (Z0h * (1.0 - waveNumber * ti * tankt)));
		}
		else
		{
			// Tonehole closed by key, not yet implemented.
			ta = (-0.12d
					- 0.17d * FastMath.tanh(2.4d * hole.getHeight() / radius))
					* radius * delta2;
			double tankt = FastMath.tan(waveNumber * te);
			Ys = Complex.valueOf(0.0,
					tankt / (Z0h * (1.0 - waveNumber * ti * tankt)));
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
