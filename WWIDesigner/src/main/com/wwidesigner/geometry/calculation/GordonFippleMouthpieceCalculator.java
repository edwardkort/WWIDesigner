/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Mouthpiece calculation for a fipple mouthpiece.
 * @author kort
 * 
 */
public class GordonFippleMouthpieceCalculator extends MouthpieceCalculator
{

	private PhysicalParameters params;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		TransferMatrix matrix = new TransferMatrix();
		params = parameters;
		double z0 = parameters.calcZ0(mouthpiece.getBoreDiameter() / 2.);
		double omega = waveNumber * parameters.getSpeedOfSound();
		double k_delta_l = calcKDeltaL(mouthpiece, omega, z0);

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
		return -1;
	}

	protected double calcKDeltaL(Mouthpiece mouthpiece, double omega, double z0)
	{
		double result = Math
				.atan(1.0 / (z0 * (calcJYE(mouthpiece, omega) + calcJYC(mouthpiece, omega))));

		return result;
	}

	protected double calcJYE(Mouthpiece mouthpiece, double omega)
	{
		double gamma = params.getGamma();
		double result = getCharacteristicLength(mouthpiece) / (gamma * omega);

		return result;
	}

	protected double calcJYC(Mouthpiece mouthpiece, double omega)
	{
		double gamma = params.getGamma();
		double speedOfSound = params.getSpeedOfSound();
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

	protected double getCharacteristicLength(Mouthpiece mouthpiece)
	{
		double windowLength = mouthpiece.getFipple().getWindowLength();
		double windowWidth = mouthpiece.getFipple().getWindowWidth();
		double fippleFactor = mouthpiece.getFipple().getFippleFactor();

		double effectiveArea = windowLength * windowWidth;
		double equivDiameter = 2. * Math.sqrt(effectiveArea / Math.PI)
				* fippleFactor;

		return equivDiameter;
	}
	
	@Override
	public Complex calcZ(Mouthpiece mouthpiece,
			double freq, PhysicalParameters physicalParams)
	{
		// Assume the open window acts as a flanged tube with an effective radius
		// that corresponds to the window area.
		double effRadius = Math.sqrt(mouthpiece.getFipple().getWindowLength()
				* mouthpiece.getFipple().getWindowWidth() / Math.PI );
		double waveNumber = physicalParams.calcWaveNumber(freq);

		StateVector sv = new StateVector(
				Tube.calcZflanged( freq, effRadius, physicalParams ),
				Complex.ONE );
		TransferMatrix tm = Tube.calcCylinderMatrix(waveNumber,
				0.0008,//this.mouthpiece.getFipple().getWindowHeight() is null, so set a representative NAF value.
				effRadius, physicalParams );
		sv = tm.multiply( sv );
		return sv.Impedance();
	}

	@Override
	public Double calcGain(Mouthpiece mouthpiece,
			double freq, Complex Z,
			PhysicalParameters physicalParams)
	{
		double radius = mouthpiece.getBoreDiameter() / 2.;
		double waveNumber = physicalParams.calcWaveNumber(freq);
		return mouthpiece.getGainFactor() * waveNumber * radius*radius
				/ Z.abs();
	}
}
