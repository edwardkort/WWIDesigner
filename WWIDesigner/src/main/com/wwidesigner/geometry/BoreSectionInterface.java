package com.wwidesigner.geometry;

import com.wwidesigner.math.TransferMatrix;

public interface BoreSectionInterface
{

	/**
	 * 
	 * @see com.wwidesigner.geometry.Component#calcT(com.wwidesigner.math.TransferMatrix,
	 *      double)
	 */
	public abstract void calcT(TransferMatrix t, double freq);

	/**
	 * 
	 * @see com.wwidesigner.geometry.Component#validate()
	 */
	public abstract void validate();

	/**
	 * @return the lB
	 */
	public abstract double getLB();

	/**
	 * @param lb
	 *            the lB to set
	 */
	public abstract void setLB(double lb);

	/**
	 * @return the rBL
	 */
	public abstract double getRBL();

	/**
	 * @param rbl
	 *            the rBL to set
	 */
	public abstract void setRBL(double rbl);

	/**
	 * @return the rBR
	 */
	public abstract double getRBR();

	/**
	 * @param rbr
	 *            the rBR to set
	 */
	public abstract void setRBR(double rbr);

}