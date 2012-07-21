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
public abstract class HoleCalculator
{
	protected Hole hole;

	public HoleCalculator(Hole hole)
	{
		this.hole = hole;
	}

	public abstract TransferMatrix calcTransferMatrix(double waveNumber,
			PhysicalParameters parameters);

}
