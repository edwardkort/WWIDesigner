/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.MouthpieceCalculator;
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
	private double mRB;
	private double mLChar;
	private double mLCav;

	/**
	 * @param mouthpiece
	 */
	public GordonFippleMouthpieceCalculator(Mouthpiece mouthpiece)
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
		double omega = waveNumber * parameters.getSpeedOfSound();
		double k_delta_l = calcKDeltaL(omega, z0);

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

	protected double calcKDeltaL(double omega, double z0)
	{
		double result = Math
				.atan(1.0 / (z0 * (calcJYE(omega) + calcJYC(omega))));

		return result;
	}

	protected double calcJYE(double omega)
	{
		double gamma = params.getGamma();
		double result = getCharacteristicLength() / (gamma * omega);

		return result;
	}

	protected double calcJYC(double omega)
	{
		double gamma = params.getGamma();
		double speedOfSound = params.getSpeedOfSound();
		double v = calcHeadspaceVolume();

		double result = -(omega * v) / (gamma * speedOfSound * speedOfSound);

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

		double effectiveArea = windowLength * windowWidth;
		double equivDiameter = 2. * Math.sqrt(effectiveArea / Math.PI)
				* fippleFactor;

		return equivDiameter;
	}
	
	protected void setVariables() {
		mRB = mouthpiece.getBoreDiameter()/2.;
		mLChar = getCharacteristicLength();
	}

	@Override
	public Complex calcZ(double freq, PhysicalParameters physicalParams)
	{
		// Assume the open window acts as a flanged tube with an effective radius
		// that corresponds to the window area.
		double effRadius = Math.sqrt(this.mouthpiece.getFipple().getWindowLength()
				* this.mouthpiece.getFipple().getWindowWidth() / Math.PI );
		double waveNumber = physicalParams.calcWaveNumber(freq);

		StateVector sv = new StateVector(
				Tube.calcZflanged( freq, effRadius, physicalParams ),
				Complex.ONE );
		TransferMatrix tm = Tube.calcCylinderMatrix(waveNumber,
				this.mouthpiece.getFipple().getWindowHeight(), 
				effRadius, physicalParams );
		sv = tm.multiply( sv );
		return sv.Impedance();
	}

	@Override
	public Double calcGain(double freq, Complex Z,
			PhysicalParameters physicalParams)
	{
		double radius = mouthpiece.getBoreDiameter() / 2.;
		double waveNumber = physicalParams.calcWaveNumber(freq);
		return this.mouthpiece.getGainFactor() * waveNumber * radius*radius
				/ Z.abs();
	}
}
