package com.wwidesigner.optimization;

public class Constraint
{
	private String displayName;
	private boolean isDimensional;

	public Constraint(String displayName, boolean isDimensional)
	{
		this.displayName = displayName;
		this.isDimensional = isDimensional;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public boolean isDimensional()
	{
		return isDimensional;
	}

}
