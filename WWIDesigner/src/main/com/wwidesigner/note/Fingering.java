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
public class Fingering
{
	protected Note note;
	protected List<Boolean> openHole;

	public Fingering()
	{

	}

	public Fingering(int numberOfHoles)
	{
		for (int i = 0; i < numberOfHoles; i++)
		{
			addOpenHole(true);
		}
	}

	/**
	 * @return the note
	 */
	public Note getNote()
	{
		return note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(Note note)
	{
		this.note = note;
	}

	/**
	 * @return the openHole
	 */
	public List<Boolean> getOpenHole()
	{
		if (openHole == null)
		{
			openHole = new ArrayList<Boolean>();
		}
		return this.openHole;
	}

	/**
	 * @param openHole
	 *            the openHole to set
	 */
	public void setOpenHole(List<Boolean> openHole)
	{
		this.openHole = openHole;
	}

	public void setOpenHoles(boolean[] openHoles)
	{
		openHole = null;
		for (boolean newOpenHole : openHoles)
		{
			addOpenHole(newOpenHole);
		}
	}

	public void addOpenHole(Boolean newOpenHole)
	{
		getOpenHole();
		openHole.add(newOpenHole);
	}

	public int getNumberOfHoles()
	{
		int num = 0;
		if (openHole != null)
		{
			num = openHole.size();
		}

		return num;
	}

}
