package com.wwidesigner.note;

public class NoteInterval
{
	protected String name;
	protected Double interval;

	public NoteInterval(String name, Double interval)
	{
		setName(name);
		setInterval(interval);
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the interval
	 */
	public Double getInterval()
	{
		return interval;
	}

	/**
	 * @param interval
	 *            the interval to set
	 */
	public void setInterval(Double interval)
	{
		this.interval = interval;
	}

	public String toString()
	{
		return name;
	}
}
