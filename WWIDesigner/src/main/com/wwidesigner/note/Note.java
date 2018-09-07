/**
 * 
 */
package com.wwidesigner.note;

import java.io.Serializable;

import org.apache.commons.math3.util.FastMath;

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
	 * @param aName
	 *            the name to set
	 */
	public void setName(String aName)
	{
		this.name = aName;
	}

	/**
	 * @return the frequency
	 */
	public Double getFrequency()
	{
		return frequency;
	}

	/**
	 * @param aFrequency
	 *            the frequency to set
	 */
	public void setFrequency(Double aFrequency)
	{
		this.frequency = aFrequency;
	}

	/**
	 * @return the frequencyMin
	 */
	public Double getFrequencyMin()
	{
		return frequencyMin;
	}

	/**
	 * @param aFrequencyMin
	 *            the frequencyMin to set
	 */
	public void setFrequencyMin(Double aFrequencyMin)
	{
		this.frequencyMin = aFrequencyMin;
	}

	/**
	 * @return the frequencyMax
	 */
	public Double getFrequencyMax()
	{
		return frequencyMax;
	}

	/**
	 * @param aFrequencyMax
	 *            the frequencyMax to set
	 */
	public void setFrequencyMax(Double aFrequencyMax)
	{
		this.frequencyMax = aFrequencyMax;
	}

	public static double cents(double f1, double f2)
	{
		return FastMath.log(f2 / f1) / Constants.LOG2 * Constants.CENTS_IN_OCTAVE;
	}

}
