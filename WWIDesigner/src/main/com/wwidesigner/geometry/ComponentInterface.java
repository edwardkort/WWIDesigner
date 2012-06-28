package com.wwidesigner.geometry;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public interface ComponentInterface
{

	/**
	 * Calculate the transfer matrix at a given wave number.
	 */
	public TransferMatrix calcTransferMatrix(double waveNumber,
			PhysicalParameters parameters);

}