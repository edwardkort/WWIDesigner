/**
 * 
 */
package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class MouthpieceCalculator
{
	protected Mouthpiece mouthpiece;

	public MouthpieceCalculator(Mouthpiece mouthpiece)
	{
		this.mouthpiece = mouthpiece;
	}

	public abstract TransferMatrix calcTransferMatrix(double waveNumber,
			PhysicalParameters parameters);

	public abstract int calcReflectanceMultiplier();

	public Complex calcZ(double freq, PhysicalParameters physicalParams)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Double calcGain(double freq, Complex Z, PhysicalParameters physicalParams)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
