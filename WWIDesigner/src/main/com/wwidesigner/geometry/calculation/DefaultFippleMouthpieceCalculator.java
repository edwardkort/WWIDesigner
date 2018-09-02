/**
 * Mouthpiece calculator for a fipple mouthpiece, principally for NAFs.
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

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.SimplePhysicalParameters;

public class DefaultFippleMouthpieceCalculator extends MouthpieceCalculator
{
	private SimplePhysicalParameters mParams;
	private static final double DEFAULT_WINDWAY_HEIGHT = 0.00078740d;
	private static final double AIR_GAMMA = 1.4018297351222222d;

	@Override
	protected TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		if (mouthpiece.isPressureNode())
		{
			// Resort to default if this is not a flow-node mouthpiece.
			return super.calcTransferMatrix(mouthpiece, waveNumber, parameters);
		}
		// Use a simplified version of PhysicalParameters: no editable pressure
		// nor CO2 concentration. This mouthpiece representation gives very
		// wrong answers when they are varied.
		// The SimplePhysicalParameters gives correct answers for varying
		// temperature and humidity, all that a NAF maker is likely to measure.
		mParams = new SimplePhysicalParameters(parameters);

		double radius = 0.5d*mouthpiece.getBoreDiameter();
		double z0 = parameters.calcZ0(radius);
		double omega = waveNumber * parameters.getSpeedOfSound();
		double k_delta_l = calcKDeltaL(mouthpiece, omega, z0);
		// Add a series resistance for radiation loss.
		double r_rad = Tube.calcR(omega/(2d*Math.PI), radius, parameters);
		double cos_kl = FastMath.cos(k_delta_l);
		double sin_kl = FastMath.sin(k_delta_l);

		Complex A = new Complex(cos_kl, r_rad * sin_kl / z0);
		Complex B = new Complex(0.d, 1.d).multiply(sin_kl * z0).add(r_rad * cos_kl);
		Complex C = new Complex(0.d, 1.d).multiply(sin_kl / z0);
		Complex D = new Complex(cos_kl);
		return new TransferMatrix(A, B, C, D);
	}

	protected double calcKDeltaL(Mouthpiece mouthpiece, double omega, double z0)
	{
		double result = Math
				.atan(1.0d / (z0 * (calcJYE(mouthpiece, omega) + calcJYC(
						mouthpiece, omega))));

		return result;
	}

	protected double calcJYE(Mouthpiece mouthpiece, double omega)
	{
		double gamma = AIR_GAMMA; // mParams.getGamma();
		double result = getCharacteristicLength(mouthpiece) / (gamma * omega);

		return result;
	}

	protected double calcJYC(Mouthpiece mouthpiece, double omega)
	{
		double gamma = AIR_GAMMA; // mParams.getGamma();
		double speedOfSound = mParams.getSpeedOfSound();
		double v = 2.d * calcHeadspaceVolume(mouthpiece);

		double result = -(omega * v) / (gamma * speedOfSound * speedOfSound);

		return result;
	}

	protected double calcHeadspaceVolume(Mouthpiece mouthpiece)
	{
		double volume = 0.d;
		for (BoreSection section : mouthpiece.getHeadspace())
		{
			volume += getSectionVolume(section);
		}

		return volume * 2.0d; // Multiplier reset using a more accurate headspace
								// representation, and verified with a
								// square-end flute with better intonation than
								// the Ken Light flute that was originally used.
	}

	protected double getSectionVolume(BoreSection section)
	{
		double leftRadius = section.getLeftRadius();
		double rightRadius = section.getRightRadius();
		double length = section.getLength();

		double volume = Math.PI
				* length
				* (leftRadius * leftRadius + leftRadius * rightRadius + rightRadius
						* rightRadius) / 3.d;

		return volume;
	}

	protected double getCharacteristicLength(Mouthpiece mouthpiece)
	{
		double windowLength = mouthpiece.getFipple().getWindowLength();
		double windowWidth = mouthpiece.getFipple().getWindowWidth();
		double fippleFactor = getScaledFippleFactor(mouthpiece);

		double effectiveArea = windowLength * windowWidth;
		double equivDiameter = 2.d * Math.sqrt(effectiveArea / Math.PI)
				* fippleFactor;

		return equivDiameter;
	}

	private double getScaledFippleFactor(Mouthpiece mouthpiece)
	{
		Double windwayHeight = mouthpiece.getFipple().getWindwayHeight();
		if (windwayHeight == null)
		{
			windwayHeight = DEFAULT_WINDWAY_HEIGHT;
		}

		double ratio = Math.pow(DEFAULT_WINDWAY_HEIGHT / windwayHeight, 1.d / 3d);
		double scaledFippleFactor;
		if (mouthpiece.getFipple().getFippleFactor() == null)
		{
			scaledFippleFactor = ratio;
		}
		else
		{
			scaledFippleFactor = mouthpiece.getFipple().getFippleFactor()
					* ratio;
		}

		return scaledFippleFactor;
	}
}
