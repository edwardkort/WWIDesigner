package com.wwidesigner.impedance.geometry;

import com.wwidesigner.impedance.math.TransferMatrix;

public interface ComponentInterface
{

	/**
	 * Should be called before any work is done.
	 */
	public void validate();

	/**
	 * Calculate the transfer matrix at frequency a freq.
	 */
	public void calcT(TransferMatrix t, double freq);

}