package com.wwidesigner.geometry;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class BoreSection implements ComponentInterface
{
	private double mLength;
	private double mLeftRadius;
	private double mRightRadius;
	private double rightBorePosition;

	protected BoreSectionCalculator boreSectionCalculator;

	public BoreSection()
	{

	}

	public BoreSection(double length, double left_radius, double right_radius)
	{
		mLength = length;
		mLeftRadius = left_radius;
		mRightRadius = right_radius;
	}

	public double getLength()
	{
		return mLength;
	}

	public double getLeftRadius()
	{
		return mLeftRadius;
	}

	public double getRightRadius()
	{
		return mRightRadius;
	}

	public void setLength(double length)
	{
		mLength = length;
	}

	/**
	 * @param mLeftRadius
	 *            the mLeftRadius to set
	 */
	public void setLeftRadius(double leftRadius)
	{
		mLeftRadius = leftRadius;
	}

	/**
	 * @param mRightRadius
	 *            the mRightRadius to set
	 */
	public void setRightRadius(double rightRadius)
	{
		mRightRadius = rightRadius;
	}

	public TransferMatrix calcTransferMatrix(double wave_number,
			PhysicalParameters params)
	{
		return boreSectionCalculator.calcTransferMatrix(wave_number, params);
	}

	/**
	 * @return the rightBorePosition
	 */
	public double getRightBorePosition()
	{
		return rightBorePosition;
	}

	/**
	 * @param rightBorePosition
	 *            the rightBorePosition to set
	 */
	public void setRightBorePosition(double rightBorePosition)
	{
		this.rightBorePosition = rightBorePosition;
	}

	/**
	 * @param sectionCalculator
	 *            the sectionCalculator to set
	 */
	public void setBoreSectionCalculator(
			BoreSectionCalculator boreSectionCalculator)
	{
		this.boreSectionCalculator = boreSectionCalculator;
	}

}