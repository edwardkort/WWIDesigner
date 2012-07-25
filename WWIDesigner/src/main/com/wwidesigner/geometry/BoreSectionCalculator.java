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
public abstract class BoreSectionCalculator
{
	protected BoreSection section;

	public BoreSectionCalculator(BoreSection section)
	{
		this.section = section;
	}

	public abstract TransferMatrix calcTransferMatrix(double waveNumber,
			PhysicalParameters parameters);
}
