package com.wwidesigner.gui.util;

public class DataOpenException extends Exception
{
	public static final String OPTIMIZER_NOT_SUPPORTED = "Referenced optimizer not supported";
	public static final String DATA_TYPE_NOT_SUPPORTED = "Data is not a supported type";
	public static final String INVALID_CONSTRAINTS = "Data is not a valid constraints set";
	public static final String CONSTRAINTS_NOT_SHOWN = "Constraints set is not shown";

	private String type;

	public DataOpenException(String message, String type)
	{
		super(message);
		this.type = type;
	}

	public String getType()
	{
		return type;
	}

	public boolean isWarning()
	{
		if (CONSTRAINTS_NOT_SHOWN.equals(type))
		{
			return true;
		}

		return false;
	}

}
