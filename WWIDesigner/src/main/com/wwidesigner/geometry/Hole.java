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

	public Hole(double aPosition, double aDiameter, double aHeight)
	{
		this.position = aPosition;
		this.diameter = aDiameter;
		this.height = aHeight;
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
	public void setDiameter(double aDiameter)
	{
		this.diameter = aDiameter;
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
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param aName
	 *            the name to set
	 */
	public void setName(String aName)
	{
		this.name = aName;
	}

	/**
	 * @return the innerCurvatureRadius
	 */
	public Double getInnerCurvatureRadius()
	{
		return innerCurvatureRadius;
	}

	/**
	 * @param aInnerCurvatureRadius
	 *            the innerCurvatureRadius to set
	 */
	public void setInnerCurvatureRadius(Double aInnerCurvatureRadius)
	{
		this.innerCurvatureRadius = aInnerCurvatureRadius;
	}

	public double getBorePosition()
	{
		return position;
	}

	public void setBorePosition(double aPosition)
	{
		this.position = aPosition;
	}

	/**
	 * @return the key
	 */
	public Key getKey()
	{
		return key;
	}

	/**
	 * @param aKey
	 *            the key to set
	 */
	public void setKey(Key aKey)
	{
		this.key = aKey;
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
	public void setBoreDiameter(double aBoreDiameter)
	{
		this.boreDiameter = aBoreDiameter;
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
