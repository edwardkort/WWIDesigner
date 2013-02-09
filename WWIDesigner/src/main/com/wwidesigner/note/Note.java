/**
 * 
 */
package com.wwidesigner.note;

import java.io.Serializable;

import com.wwidesigner.util.Constants;

/**
 * @author kort
 * 
 */
public class Note implements Serializable
{
	protected String name;
	protected Double frequency;
	protected Double frequencyMin;
	protected Double frequencyMax;

	public Note()
	{
	}

	public Note(Note note)
	{
		if (note != null)
		{
			setName(note.getName());
			Double dbl = note.getFrequency();
			if (dbl != null)
			{
				setFrequency(new Double(dbl));
			}
			dbl = note.getFrequencyMin();
			if (dbl != null)
			{
				setFrequencyMin(new Double(dbl));
			}
			dbl = note.getFrequencyMax();
			if (dbl != null)
			{
				setFrequencyMax(new Double(dbl));
			}
		}
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

	public static double cents(double f1, double f2)
	{
		return Math.log(f2 / f1) / Constants.LOG2 * Constants.CENTS_IN_OCTAVE;
	}

}
