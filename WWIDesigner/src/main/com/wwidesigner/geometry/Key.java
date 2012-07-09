/**
 * 
 */
package com.wwidesigner.geometry;

/**
 * @author kort
 * 
 */
public class Key
{
	protected double diameter;
	protected double holeDiameter;
	protected double height;
	protected double thickness;
	protected double wallThickness;
	protected double chimneyHeight;

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

	/**
	 * @return the holeDiameter
	 */
	public double getHoleDiameter()
	{
		return holeDiameter;
	}

	/**
	 * @param holeDiameter
	 *            the holeDiameter to set
	 */
	public void setHoleDiameter(double holeDiameter)
	{
		this.holeDiameter = holeDiameter;
	}

	/**
	 * @return the height
	 */
	public double getHeight()
	{
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(double height)
	{
		this.height = height;
	}

	/**
	 * @return the thickness
	 */
	public double getThickness()
	{
		return thickness;
	}

	/**
	 * @param thickness
	 *            the thickness to set
	 */
	public void setThickness(double thickness)
	{
		this.thickness = thickness;
	}

	/**
	 * @return the wallThickness
	 */
	public double getWallThickness()
	{
		return wallThickness;
	}

	/**
	 * @param wallThickness
	 *            the wallThickness to set
	 */
	public void setWallThickness(double wallThickness)
	{
		this.wallThickness = wallThickness;
	}

	/**
	 * @return the chimneyHeight
	 */
	public double getChimneyHeight()
	{
		return chimneyHeight;
	}

	/**
	 * @param chimneyHeight
	 *            the chimneyHeight to set
	 */
	public void setChimneyHeight(double chimneyHeight)
	{
		this.chimneyHeight = chimneyHeight;
	}

	public void convertDimensions(double multiplier)
	{
		diameter *= multiplier;
		holeDiameter *= multiplier;
		height *= multiplier;
		thickness *= multiplier;
		wallThickness *= multiplier;
		chimneyHeight *= multiplier;
	}
}
