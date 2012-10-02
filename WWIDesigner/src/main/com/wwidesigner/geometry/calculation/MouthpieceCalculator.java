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
public abstract class MouthpieceCalculator
{
	public MouthpieceCalculator()
	{
	}

	public abstract TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters);

	public abstract int calcReflectanceMultiplier();

	public abstract Double calcGain(Mouthpiece mouthpiece,
			double freq, Complex Z, PhysicalParameters physicalParams);
}
