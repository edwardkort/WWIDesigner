package com.wwidesigner.gui.util;

public class DataPopulatedEvent
{
	private boolean isPopulated;
	private Object source;

	public DataPopulatedEvent(Object source, boolean isPopulated)
	{
		this.source = source;
		this.isPopulated = isPopulated;
	}

	public Object getSource()
	{
		return source;
	}

	public boolean isPopulated()
	{
		return isPopulated;
	}
}
