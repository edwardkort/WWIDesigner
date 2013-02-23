package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Hole;

public class Constraint
{
	private String displayName;
	private String category;
	private ConstraintType type;

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

}
