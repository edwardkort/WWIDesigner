package com.wwidesigner.geometry;

import com.wwidesigner.math.TransferMatrix;

public interface EmbouchureInterface
{

	/**
	 * 
	 * @see com.wwidesigner.geometry.Component#calcT(com.wwidesigner.math.TransferMatrix,
	 *      double)
	 */
	public void calcT(TransferMatrix t, double freq);

	/**
	 * 
	 * @see com.wwidesigner.geometry.Component#validate()
	 */
	public void validate();

}