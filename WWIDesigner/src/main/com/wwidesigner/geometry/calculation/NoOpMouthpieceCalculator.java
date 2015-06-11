/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class NoOpMouthpieceCalculator extends MouthpieceCalculator
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
		return TransferMatrix.makeIdentity();
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

	@Override
	public Double calcGain(Mouthpiece mouthpiece, double freq, Complex Z,
			PhysicalParameters physicalParams)
	{
		return 1.0;
	}

}
