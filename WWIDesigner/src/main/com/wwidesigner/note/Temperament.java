/**
 * 
 */
package com.wwidesigner.note;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kort
 * 
 */
public class Temperament
{
	protected String name;
	protected String comment;
	protected List<Double> ratio;

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
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the ratio
	 */
	public List<Double> getRatio()
	{
		if (ratio == null)
		{
			ratio = new ArrayList<Double>();
		}
		return this.ratio;
	}

	/**
	 * @param ratio
	 *            the ratio to set
	 */
	public void setRatio(List<Double> ratio)
	{
		this.ratio = ratio;
	}

	public void addRatio(Double newRatio)
	{
		getRatio();
		ratio.add(newRatio);
	}

}
