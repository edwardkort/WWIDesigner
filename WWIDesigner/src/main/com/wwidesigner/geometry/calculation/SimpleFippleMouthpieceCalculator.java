/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.MouthpieceCalculator;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class SimpleFippleMouthpieceCalculator extends MouthpieceCalculator
{

	private PhysicalParameters params;

	/**
	 * @param mouthpiece
	 */
	public SimpleFippleMouthpieceCalculator(Mouthpiece mouthpiece)
	{
		super(mouthpiece);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(double waveNumber,
			PhysicalParameters parameters)
	{
		TransferMatrix matrix = new TransferMatrix();
		params = parameters;
		double z0 = parameters.calcZ0(mouthpiece.getBoreDiameter() / 2.);
		double freqInRadians = waveNumber * parameters.getSpeedOfSound();
		double k_delta_l = calcKDeltaL(freqInRadians, z0);

		Complex k_delta = new Complex(Math.cos(k_delta_l));
		matrix.setPP(k_delta);
		matrix.setUU(k_delta);

		k_delta = new Complex(0., -1.).multiply(Math.sin(k_delta_l) * z0);
		matrix.setPU(k_delta);

		k_delta = new Complex(0., -1.).multiply(Math.sin(k_delta_l) / z0);
		matrix.setUP(k_delta);

		return matrix;
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
		return 1;
	}

	protected double calcKDeltaL(double freqInRadians, double z0)
	{
		double result = Math
				.atan(1.0 / (z0 * (calcJYE(freqInRadians) + calcJYC(freqInRadians))));

		return result;
	}

	protected double calcJYE(double freqInRadians)
	{
		double gamma = params.getGamma();
		double result = getCharacteristicLength() / (gamma * freqInRadians);

		return result;
	}

	protected double calcJYC(double freqInRadians)
	{
		double gamma = params.getGamma();
		double speedOfSound = params.getSpeedOfSound();
		double v = calcHeadspaceVolume();

		double result = -(freqInRadians * v)
				/ (gamma * speedOfSound * speedOfSound);

		return result;
	}

	protected double calcHeadspaceVolume()
	{
		double volume = 0.;
		for (BoreSection section : mouthpiece.getHeadspace())
		{
			volume += getSectionVolume(section);
		}

		return volume;
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

	protected double getCharacteristicLength()
	{
		double windowLength = mouthpiece.getFipple().getWindowLength();
		double windowWidth = mouthpiece.getFipple().getWindowWidth();
		double fippleFactor = mouthpiece.getFipple().getFippleFactor();

		double effectiveArea = windowLength * windowWidth * fippleFactor;
		double equivDiameter = Math.sqrt(Math.PI * effectiveArea) * 2.;

		return equivDiameter;
	}

}
