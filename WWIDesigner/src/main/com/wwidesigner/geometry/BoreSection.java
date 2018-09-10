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
		rightBorePosition = 0.;
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
	 *            the new value for mLeftRadius
	 */
	public void setLeftRadius(double leftRadius)
	{
		mLeftRadius = leftRadius;
	}

	/**
	 * @param rightRadius
	 *            the new value for mRightRadius
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
	 * @param aRightBorePosition
	 *            the new value for rightBorePosition
	 */
	public void setRightBorePosition(double aRightBorePosition)
	{
		this.rightBorePosition = aRightBorePosition;
	}

}