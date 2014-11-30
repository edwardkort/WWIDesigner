package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.util.Constants.LengthType;

public class Constraint
{
	protected String displayName;
	protected String category;
	protected ConstraintType type;
	private transient Constraints parent;
	protected Double lowerBound;
	protected Double upperBound;

	public Constraint()
	{

	}

	public Constraint(String category, String displayName, ConstraintType type)
	{
		this.category = category;
		this.displayName = displayName;
		this.type = type;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public String getCategory()
	{
		return category;
	}

	public ConstraintType getType()
	{
		return type;
	}

	public enum ConstraintType
	{
		BOOLEAN, INTEGER, DIMENSIONAL, DIMENSIONLESS
	}

	public static boolean isValid(Constraint constraint)
	{
		if (constraint == null)
		{
			return false;
		}
		if (constraint.category == null
				|| constraint.category.trim().length() == 0)
		{
			return false;
		}
		if (constraint.displayName == null
				|| constraint.displayName.trim().length() == 0)
		{
			return false;
		}

		return true;
	}

	public static String getHoleName(Hole hole, int sortedIdx, int minIdx,
			int maxIdx)
	{
		if (hole == null)
		{
			return null;
		}
		String givenName = hole.getName();
		String name = (givenName != null && givenName.trim().length() > 0) ? givenName
				: "Hole " + sortedIdx;
		if (sortedIdx == maxIdx)
		{
			name += " (top)";
		}
		else if (sortedIdx == minIdx)
		{
			name += " (bottom)";
		}

		return name;
	}

	public Double getLowerBound()
	{
		return lowerBound;
	}

	public void setLowerBound(Double lowerBound)
	{
		this.lowerBound = lowerBound;
	}

	public Double getUpperBound()
	{
		return upperBound;
	}

	public void setUpperBound(Double upperBound)
	{
		this.upperBound = upperBound;
	}

	public void setParent(Constraints parent)
	{
		this.parent = parent;
	}

	public double convertBound(boolean isLowerBound, boolean toMetres)
	{
		// Set upper and lower bounds if they have not been set
		Double bound = isLowerBound ? getLowerBound() : getUpperBound();
		if (bound == null)
		{
			bound = 0.;
			if (isLowerBound)
			{
				setLowerBound(bound);
			}
			else
			{
				setUpperBound(bound);
			}
		}

		double multiplier;
		LengthType dimensionType = parent.getDimensionType();
		if (getType() == ConstraintType.DIMENSIONAL)
		{
			multiplier = toMetres ? dimensionType.getMultiplierToMetres()
					: dimensionType.getMultiplierFromMetres();
		}
		else
		{
			multiplier = 1.;
		}

		return bound * multiplier;
	}

	public String getConstraintDimension()
	{
		if (type == ConstraintType.DIMENSIONAL)
		{
			return parent.getDimensionType().toString();
		}

		return type.toString();
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public void setType(ConstraintType type)
	{
		this.type = type;
	}

}
