/**
 * 
 */
package com.wwidesigner.note;

/**
 * @author kort
 * 
 */
public class Note
{
	protected String name;
	protected Double frequency;
	protected Double frequencyMin;
	protected Double frequencyMax;

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
	 * @return the frequency
	 */
	public Double getFrequency()
	{
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(Double frequency)
	{
		this.frequency = frequency;
	}

	/**
	 * @return the frequencyMin
	 */
	public Double getFrequencyMin()
	{
		return frequencyMin;
	}

	/**
	 * @param frequencyMin
	 *            the frequencyMin to set
	 */
	public void setFrequencyMin(Double frequencyMin)
	{
		this.frequencyMin = frequencyMin;
	}

	/**
	 * @return the frequencyMax
	 */
	public Double getFrequencyMax()
	{
		return frequencyMax;
	}

	/**
	 * @param frequencyMax
	 *            the frequencyMax to set
	 */
	public void setFrequencyMax(Double frequencyMax)
	{
		this.frequencyMax = frequencyMax;
	}

}
