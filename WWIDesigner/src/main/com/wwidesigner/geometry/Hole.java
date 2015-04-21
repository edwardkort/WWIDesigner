package com.wwidesigner.geometry;

import com.wwidesigner.util.InvalidFieldException;


public class Hole implements ComponentInterface, BorePointInterface
{
	protected String name;
	protected double height;
	protected double position;
	protected double diameter;
	protected boolean openHole;
	protected Double innerCurvatureRadius;
	protected Key key;

	protected double boreDiameter;

	public Hole()
	{

	}

	public Hole(double position, double diameter, double height)
	{
		this.position = position;
		this.diameter = diameter;
		this.height = height;
	}

	/**
	 * @return the radius
	 */
	public double getDiameter()
	{
		return diameter;
	}

	/**
	 * @param radius
	 *            the radius to set
	 */
	public void setDiameter(double diameter)
	{
		this.diameter = diameter;
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
	 * @return the openHole
	 */
	public boolean isOpenHole()
	{
		return openHole;
	}

	/**
	 * @param openHole
	 *            the openHole to set
	 */
	public void setOpenHole(boolean openHole)
	{
		this.openHole = openHole;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the innerCurvatureRadius
	 */
	public Double getInnerCurvatureRadius()
	{
		return innerCurvatureRadius;
	}

	/**
	 * @param innerCurvatureRadius
	 *            the innerCurvatureRadius to set
	 */
	public void setInnerCurvatureRadius(Double innerCurvatureRadius)
	{
		this.innerCurvatureRadius = innerCurvatureRadius;
	}

	public double getBorePosition()
	{
		return position;
	}

	public void setBorePosition(double position)
	{
		this.position = position;
	}

	/**
	 * @return the key
	 */
	public Key getKey()
	{
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(Key key)
	{
		this.key = key;
	}

	public void convertDimensions(double multiplier)
	{
		height *= multiplier;
		position *= multiplier;
		diameter *= multiplier;
		boreDiameter *= multiplier;
		if (innerCurvatureRadius != null)
		{
			innerCurvatureRadius *= multiplier;
		}
		if (key != null)
		{
			key.convertDimensions(multiplier);
		}
	}

	@Override
	public void setBoreDiameter(double boreDiameter)
	{
		this.boreDiameter = boreDiameter;
	}

	@Override
	public double getBoreDiameter()
	{
		return boreDiameter;
	}

	public double getRatio()
	{
		return diameter / boreDiameter;
	}

	public void setRatio(double alpha)
	{
		diameter = alpha * boreDiameter;
	}
	
	public void checkValidity() throws InvalidFieldException
	{
		String holeName = "Hole";
		if (this.name != null && ! this.name.isEmpty())
		{
			holeName += " " + this.name;
		}
		if (diameter <= 0.0)
		{
			throw new InvalidFieldException("Instrument", holeName + " diameter must be positive.");
		}
		if (height <= 0.0)
		{
			throw new InvalidFieldException("Instrument", holeName + " height must be positive.");
		}
	}
}
