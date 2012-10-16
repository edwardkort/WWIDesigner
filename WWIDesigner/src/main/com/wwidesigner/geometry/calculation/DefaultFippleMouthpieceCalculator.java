package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class DefaultFippleMouthpieceCalculator extends MouthpieceCalculator
{
	private PhysicalParameters mParams;
	private static final double DEFAULT_WINDWAY_HEIGHT = 0.00078740d;

	@Override
	public TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		mParams = parameters;

		double z0 = parameters.calcZ0(mouthpiece.getBoreDiameter() / 2.);
		double omega = waveNumber * parameters.getSpeedOfSound();
		double k_delta_l = calcKDeltaL(mouthpiece, omega, z0);

		Complex k_delta = new Complex(Math.cos(k_delta_l));

		Complex B = new Complex(0., 1.).multiply(Math.sin(k_delta_l) * z0);

		Complex C = new Complex(0., 1.).multiply(Math.sin(k_delta_l) / z0);

		return new TransferMatrix(k_delta, B, C, k_delta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcReflectanceMultiplier()
	 */
	@Override
	public int calcReflectanceMultiplier()
	{
		return -1;
	}

	protected double calcKDeltaL(Mouthpiece mouthpiece, double omega, double z0)
	{
		double result = Math
				.atan(1.0 / (z0 * (calcJYE(mouthpiece, omega) + calcJYC(
						mouthpiece, omega))));

		return result;
	}

	protected double calcJYE(Mouthpiece mouthpiece, double omega)
	{
		double gamma = mParams.getGamma();
		double result = getCharacteristicLength(mouthpiece) / (gamma * omega);

		return result;
	}

	protected double calcJYC(Mouthpiece mouthpiece, double omega)
	{
		double gamma = mParams.getGamma();
		double speedOfSound = mParams.getSpeedOfSound();
		double v = 2. * calcHeadspaceVolume(mouthpiece);

		double result = -(omega * v) / (gamma * speedOfSound * speedOfSound);

		return result;
	}

	protected double calcHeadspaceVolume(Mouthpiece mouthpiece)
	{
		double volume = 0.;
		for (BoreSection section : mouthpiece.getHeadspace())
		{
			volume += getSectionVolume(section);
		}

		return volume * 1.7; // Multiplier set by eye to fit LightG6HoleNaf
								// tuning.
	}

	protected double getSectionVolume(BoreSection section)
	{
		double leftRadius = section.getLeftRadius();
		double rightRadius = section.getRightRadius();
		double length = section.getLength();

		double volume = Math.PI
				* length
				* (leftRadius * leftRadius + leftRadius * rightRadius + rightRadius
						* rightRadius) / 3.;

		return volume;
	}

	protected double getCharacteristicLength(Mouthpiece mouthpiece)
	{
		double windowLength = mouthpiece.getFipple().getWindowLength();
		double windowWidth = mouthpiece.getFipple().getWindowWidth();
		double fippleFactor = getScaledFippleFactor(mouthpiece);

		double effectiveArea = windowLength * windowWidth;
		double equivDiameter = 2. * Math.sqrt(effectiveArea / Math.PI)
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

		double ratio = Math.pow(DEFAULT_WINDWAY_HEIGHT / windwayHeight, 1. / 3);
		double scaledFippleFactor = mouthpiece.getFipple().getFippleFactor()
				* ratio;

		return scaledFippleFactor;
	}

	@Override
	public Double calcGain(Mouthpiece mouthpiece, double freq, Complex Z,
			PhysicalParameters physicalParams)
	{
		double radius = mouthpiece.getBoreDiameter() / 2.;
		double waveNumber = physicalParams.calcWaveNumber(freq);
		return mouthpiece.getGainFactor() * waveNumber * radius * radius
				/ Z.abs();
	}
}
