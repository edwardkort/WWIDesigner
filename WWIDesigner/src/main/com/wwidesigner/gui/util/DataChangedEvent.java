package com.wwidesigner.gui.util;

/**
 * If this event is created, then the associated data has changed.
 * 
 * @author Edward N. Kort
 *
 */
public class DataChangedEvent
{
	private Object source;

	public DataChangedEvent(Object source)
	{
		this.source = source;
	}

	public Object getSource()
	{
		return source;
	}
}
