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
		double freq = parameters.calcFrequency(waveNumber);
		
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

	/**
	 * Calculate the impedance of the whistle window, at specified frequency.
	 * @param mouthpiece
	 * @param freq
	 * @param physicalParams
	 * @return Complex impedance of whistle window.
	 */
	public Complex calcZ(Mouthpiece mouthpiece,
			double freq, PhysicalParameters physicalParams)
	{
		// Reactance modeled from measurements of real whistles.
		double effSize = Math.sqrt(mouthpiece.getFipple().getWindowLength()
				* mouthpiece.getFipple().getWindowWidth() );
		// Model for use in absence of blade height measurement.
		double Xw = physicalParams.getRho() * freq/effSize
				* ( 7.345 - 2.18e-4 * freq
					- 3.95e-3 / mouthpiece.getFipple().getWindowHeight() );
		// Model for use when blade height measurement is available.
		// double Xw = physicalParams.getRho() * freq/effSize
		// 		* ( 5.824 - 2.76e-4 * freq
		//				+ 2.04 * mouthpiece.getFipple().getWindowHeight()/effSize
		//				- 3.36 * mouthpiece.getFipple().getBladeHeight()/mouthpiece.getFipple().getWindwayHeight());
		
		// Resistance modeled as short cylindrical tube with same area as window.
		double Rw = physicalParams.getRho()
				* ( 6.42 * freq*freq/physicalParams.getSpeedOfSound()
						+ 0.0184 * Math.sqrt(freq)*mouthpiece.getFipple().getWindowHeight()
							/ (effSize*effSize*effSize));
		return new Complex(Rw,Xw);
	}

	public Complex calcZ_old(Mouthpiece mouthpiece,
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
