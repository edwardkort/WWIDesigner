package com.wwidesigner.gui.util;

public class DataPopulatedEvent
{
	private boolean isPopulated;
	private Object source;
	private String dataName;

	public DataPopulatedEvent(Object source, boolean isPopulated)
	{
		this.source = source;
		this.isPopulated = isPopulated;
	}

	public DataPopulatedEvent(Object source, String dataName,
			boolean isPopulated)
	{
		this(source, isPopulated);
		this.dataName = dataName;
	}

	public Object getSource()
	{
		return source;
	}

	public boolean isPopulated()
	{
		return isPopulated;
	}

	public Boolean isPopulated(String dataName)
	{
		if (this.dataName == null || !this.dataName.equals(dataName))
		{
			return null;
		}

		return isPopulated;
	}
}
