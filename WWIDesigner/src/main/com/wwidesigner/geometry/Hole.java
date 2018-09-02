package com.wwidesigner.geometry;

import com.wwidesigner.util.InvalidFieldHandler;

public class Hole implements ComponentInterface, BorePointInterface
{
	protected String name;
	protected double height;
	protected double position;
	protected double diameter;
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
	 * @param diameter
	 *            the diameter to set
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

	public void checkValidity(InvalidFieldHandler handler, Double minPosition, Double maxPosition)
	{
		String holeName = "Hole";
		if (this.name != null && !this.name.isEmpty())
		{
			holeName += " " + this.name;
		}
		if (Double.isNaN(position))
		{
			handler.logError(holeName + " position must be specified.");
		}
		else 
		{
			if (minPosition != null && minPosition >= position)
			{
				handler.logError(holeName + " position must be greater than mouthpiece position.");
			}
			if (maxPosition != null && maxPosition <= position)
			{
				handler.logError(holeName + " position must be less than highest bore position.");
			}
		}
		if (Double.isNaN(diameter))
		{
			handler.logError(holeName + " diameter must be specified.");
		}
		else if (diameter <= 0.0)
		{
			handler.logError(holeName + " diameter must be positive.");
		}
		if (Double.isNaN(height))
		{
			handler.logError(holeName + " height must be specified.");
		}
		else if (height <= 0.0)
		{
			handler.logError(holeName + " height must be positive.");
		}
	}
}
