/**
 * 
 */
package com.wwidesigner.geometry;

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

}
