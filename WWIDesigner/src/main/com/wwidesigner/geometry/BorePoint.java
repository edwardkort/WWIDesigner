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
	protected double position;
	protected double diameter;

	public BorePoint()
	{
	}

	/**
	 * @return the position
	 */
	public double getPosition()
	{
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(double position)
	{
		this.position = position;
	}

	/**
	 * @return the diameter
	 */
	public double getDiameter()
	{
		return diameter;
	}

	/**
	 * @param diameter
	 *            the diameter to set
	 */
	public void setDiameter(double diameter)
	{
		this.diameter = diameter;
	}

}
