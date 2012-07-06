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
public class FingeringPattern
{
	protected String name;
	protected String comment;
	protected int numberOfHoles;
	protected List<Fingering> fingering;

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
	 * @return the numberOfHoles
	 */
	public int getNumberOfHoles()
	{
		return numberOfHoles;
	}

	/**
	 * @param numberOfHoles
	 *            the numberOfHoles to set
	 */
	public void setNumberOfHoles(int numberOfHoles)
	{
		this.numberOfHoles = numberOfHoles;
	}

	/**
	 * @return the fingering
	 */
	public List<Fingering> getFingering()
	{
		if (fingering == null)
		{
			fingering = new ArrayList<Fingering>();
		}
		return this.fingering;
	}

	/**
	 * @param fingering
	 *            the fingering to set
	 */
	public void setFingering(List<Fingering> fingering)
	{
		this.fingering = fingering;
	}

	public void addFingering(Fingering newFingering)
	{
		getFingering();
		fingering.add(newFingering);
	}

}
