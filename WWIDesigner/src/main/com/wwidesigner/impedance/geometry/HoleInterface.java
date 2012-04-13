package com.wwidesigner.impedance.geometry;

import com.wwidesigner.impedance.math.TransferMatrix;

public interface HoleInterface
{

	/**
	 * 
	 * @see com.wwidesigner.impedance.geometry.Component#calcT(com.wwidesigner.impedance.math.TransferMatrix,
	 *      double)
	 */
	public void calcT(TransferMatrix t, double freq);

	/**
	 * 
	 * @see com.wwidesigner.impedance.geometry.Component#validate()
	 */
	public void validate();

}