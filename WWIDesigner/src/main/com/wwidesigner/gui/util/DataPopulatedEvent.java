package com.wwidesigner.gui.util;

public class DataPopulatedEvent
{
	private boolean isPopulated;
	private Object source;
	private String dataName;

	public DataPopulatedEvent(Object aSource, boolean aIsPopulated)
	{
		this.source = aSource;
		this.isPopulated = aIsPopulated;
	}

	public DataPopulatedEvent(Object aSource, String aDataName,
			boolean aIsPopulated)
	{
		this(aSource, aIsPopulated);
		this.dataName = aDataName;
	}

	public Object getSource()
	{
		return source;
	}

	public boolean isPopulated()
	{
		return isPopulated;
	}

	public Boolean isPopulated(String aDataName)
	{
		if (this.dataName == null || !this.dataName.equals(aDataName))
		{
			return null;
		}

		return isPopulated;
	}
}
