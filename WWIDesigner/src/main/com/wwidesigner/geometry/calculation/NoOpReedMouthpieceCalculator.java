package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.MouthpieceCalculator;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class NoOpReedMouthpieceCalculator extends MouthpieceCalculator
{

	public NoOpReedMouthpieceCalculator(Mouthpiece mouthpiece)
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
		return 1;
	}

}
