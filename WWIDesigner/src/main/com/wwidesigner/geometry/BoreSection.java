package com.wwidesigner.geometry;


public class BoreSection implements ComponentInterface
{
	private double mLength;
	private double mLeftRadius;
	private double mRightRadius;
	private double rightBorePosition;

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
	 * @param leftRadius
	 *            the mLeftRadius to set
	 */
	public void setLeftRadius(double leftRadius)
	{
		mLeftRadius = leftRadius;
	}

	/**
	 * @param rightRadius
	 *            the mRightRadius to set
	 */
	public void setRightRadius(double rightRadius)
	{
		mRightRadius = rightRadius;
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

}