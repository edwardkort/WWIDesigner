package com.wwidesigner.note;

public class NoteInterval
{
	protected String name;
	protected Double interval;

	public NoteInterval(String aName, Double aInterval)
	{
		setName(aName);
		setInterval(aInterval);
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
	 * @return the interval
	 */
	public Double getInterval()
	{
		return interval;
	}

	/**
	 * @param aInterval
	 *            the interval to set
	 */
	public void setInterval(Double aInterval)
	{
		this.interval = aInterval;
	}

	public String toString()
	{
		return name;
	}
}
