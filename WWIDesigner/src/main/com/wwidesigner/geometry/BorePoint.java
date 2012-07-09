/**
 * 
 */
package com.wwidesigner.geometry;

/**
 * @author kort
 * 
 */
public class BorePoint implements PositionInterface
{
	protected double borePosition;
	protected double boreDiameter;

	public BorePoint()
	{
	}

	/**
	 * @return the borePosition
	 */
	public double getBorePosition()
	{
		return borePosition;
	}

	/**
	 * @param borePosition
	 *            the borePosition to set
	 */
	public void setBorePosition(double borePosition)
	{
		this.borePosition = borePosition;
	}

	/**
	 * @return the boreDiameter
	 */
	public double getBoreDiameter()
	{
		return boreDiameter;
	}

	/**
	 * @param boreDiameter
	 *            the boreDiameter to set
	 */
	public void setBoreDiameter(double boreDiameter)
	{
		this.boreDiameter = boreDiameter;
	}

	public void convertDimensions(double multiplier)
	{
		borePosition *= multiplier;
		boreDiameter *= multiplier;
	}

}
