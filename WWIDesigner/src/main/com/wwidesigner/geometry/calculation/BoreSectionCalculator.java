/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class BoreSectionCalculator
{
	public BoreSectionCalculator()
	{
	}

	public abstract TransferMatrix calcTransferMatrix(BoreSection section,
			double waveNumber, PhysicalParameters parameters);
}
