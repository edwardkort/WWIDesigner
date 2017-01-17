/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class HoleCalculator
{
	public abstract TransferMatrix calcTransferMatrix(Hole hole,
			boolean isOpen, double waveNumber, PhysicalParameters parameters);

}
