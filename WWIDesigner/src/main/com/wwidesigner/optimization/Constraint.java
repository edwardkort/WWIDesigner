package com.wwidesigner.optimization;

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

}
