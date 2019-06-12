package com.wwidesigner.geometry.calculation;

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

public class SimpleHoleCalculator extends HoleCalculator
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

	protected boolean isPlugged = false;

	private double mFudgeFactor = 1.0;

	public SimpleHoleCalculator()
	{
		setFudgeFactor(1.0);
		this.isPlugged = false;
		setFingerAdjustment(DEFAULT_FINGER_ADJ);
	}

	public SimpleHoleCalculator(double fudgeFactor)
	{
		setFudgeFactor(fudgeFactor);
		this.isPlugged = false;
		setFingerAdjustment(NO_FINGER_ADJ);
	}

	public SimpleHoleCalculator(boolean aIsPlugged, double aFingerAdj)
	{
		setFudgeFactor(1.0);
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

	public double getFudgeFactor()
	{
		return mFudgeFactor;
	}

	public void setFudgeFactor(double fudgeFactor)
	{
		this.mFudgeFactor = fudgeFactor;
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
		double radius = hole.getDiameter() / 2.;
		double radiusCorrected = mFudgeFactor * hole.getDiameter() / 2.;
		double boreRadius = hole.getBoreDiameter() / 2.;
		Complex Ys = Complex.ZERO; // Shunt admittance == 1/Zs
		Complex Za = Complex.ZERO; // Series impedance

		double Z0h = parameters.calcZ0(radiusCorrected); // Characteristic
															// impedance of
		// hole.
		double delta = radius / boreRadius;
		double deltaCorrected = radiusCorrected / boreRadius;
		double delta2 = delta * delta;
		double delta2Corrected = deltaCorrected * deltaCorrected;

		// Equation 8.
		double te = hole.getHeight();
		// Equation 31.
		double ti = radius * (0.82 - 1.4 * delta2);
		double tiCorrected = radiusCorrected * (0.82 - 1.4 * delta2Corrected);

		double ta = 0.;

		if (isOpen)
		{
			double kb = waveNumber * radiusCorrected;
			// double ka = waveNumber * boreRadius;

			// Equation 33.
			ta = -0.3d * radiusCorrected * delta2;

			// Normalized radiation resistance, real part of Zs, per equation 3,
			// (rather than real part of Zr in equation 10).
			double Rr = 0.25d * kb * kb;

			// Radiation length correction (equation 10 with Zr/Z0h = jk*tr
			// without real part).
			// Equation 11 times radius.
			double tr = radiusCorrected * (0.822d - 0.47d * FastMath.pow(
					radiusCorrected / (boreRadius + hole.getHeight()), 0.8d));
			// Equation 3 and 7, inverted.
			double kttotal = waveNumber * tiCorrected
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
			ta = (-0.12d
					- 0.17d * FastMath.tanh(2.4d * hole.getHeight() / radius))
					* radius * delta2;
			double tf = 0.0;
			if (fingerAdjustment > 0.0)
			{
				// Approximate curve fit.
				tf = radius * delta * fingerAdjustment;
				if (tf > te)
				{
					tf = te;
					fingerAdjustment = te / (radius * delta);
				}
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
		Za = Complex.I.multiply(Z0h * delta2 * waveNumber * ta);
		Complex Za_Zs = Za.multiply(Ys);

		// Transfer matrix (equation 2).
		Complex A = Za_Zs.divide(2.).add(1.);
		Complex B = Za.multiply(Za_Zs.divide(4.).add(1.));
		Complex C = Ys;
		TransferMatrix result = new TransferMatrix(A, B, C, A);

		return result;
	}

}
