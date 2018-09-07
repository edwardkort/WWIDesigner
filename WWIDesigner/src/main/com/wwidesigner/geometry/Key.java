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
	 * @param aDiameter
	 *            the diameter to set
	 */
	public void setDiameter(double aDiameter)
	{
		this.diameter = aDiameter;
	}

	/**
	 * @return the holeDiameter
	 */
	public double getHoleDiameter()
	{
		return holeDiameter;
	}

	/**
	 * @param aHoleDiameter
	 *            the holeDiameter to set
	 */
	public void setHoleDiameter(double aHoleDiameter)
	{
		this.holeDiameter = aHoleDiameter;
	}

	/**
	 * @return the height
	 */
	public double getHeight()
	{
		return height;
	}

	/**
	 * @param aHeight
	 *            the height to set
	 */
	public void setHeight(double aHeight)
	{
		this.height = aHeight;
	}

	/**
	 * @return the thickness
	 */
	public double getThickness()
	{
		return thickness;
	}

	/**
	 * @param aThickness
	 *            the thickness to set
	 */
	public void setThickness(double aThickness)
	{
		this.thickness = aThickness;
	}

	/**
	 * @return the wallThickness
	 */
	public double getWallThickness()
	{
		return wallThickness;
	}

	/**
	 * @param aWallThickness
	 *            the wallThickness to set
	 */
	public void setWallThickness(double aWallThickness)
	{
		this.wallThickness = aWallThickness;
	}

	/**
	 * @return the chimneyHeight
	 */
	public double getChimneyHeight()
	{
		return chimneyHeight;
	}

	/**
	 * @param aChimneyHeight
	 *            the chimneyHeight to set
	 */
	public void setChimneyHeight(double aChimneyHeight)
	{
		this.chimneyHeight = aChimneyHeight;
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
