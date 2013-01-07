/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Mouthpiece calculation for a fipple mouthpiece, modeling
 * the window as a (short) tube with area equal to the window area
 * and flanged open end.
 * @author Burton Patkau
 * 
 */
public class SimpleFippleMouthpieceCalculator extends MouthpieceCalculator
{
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
		double freq = waveNumber * parameters.getSpeedOfSound() / ( 2* Math.PI );
		
		Complex Zwindow = calcZ(mouthpiece, freq, parameters);
		
		return new TransferMatrix(Complex.ONE, Zwindow, Complex.ZERO, Complex.ONE);		
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
				mouthpiece.getFipple().getWindowHeight(), 
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
