/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author Burton Patkau
 * 
 */
public class SimpleBoreSectionCalculator extends BoreSectionCalculator
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.BoreSectionCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(BoreSection section,
			double waveNumber, PhysicalParameters params)
	{
		double leftRadius = section.getLeftRadius();
		double rightRadius = section.getRightRadius();
		double length = section.getLength();
		
		return Tube.calcConeMatrix(waveNumber, length, leftRadius, rightRadius, params);
	}

}
