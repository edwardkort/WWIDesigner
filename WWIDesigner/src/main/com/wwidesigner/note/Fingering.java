/**
 * 
 */
package com.wwidesigner.note;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kort
 * 
 */
public class Fingering implements Serializable
{
	protected Note note;
	protected List<Boolean> openHole;
	protected Boolean openEnd;
	protected Integer optimizationWeight;

	public Fingering()
	{
		this.openEnd = null;
	}

	public Fingering(int numberOfHoles)
	{
		for (int i = 0; i < numberOfHoles; i++)
		{
			addOpenHole(true);
		}
	}

	public Fingering(Fingering fingering)
	{
		if (fingering != null)
		{
			setNote(new Note(fingering.getNote()));
			List<Boolean> holes = fingering.getOpenHole();
			for (Boolean hole : holes)
			{
				addOpenHole(new Boolean(hole));
			}
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
	
	public String toString()
	{
		String holeString = "";
		for (boolean holeOpen : openHole)
		{
			if (holeOpen)
			{
				holeString += "O";
			}
			else
			{
				holeString += "X";
			}
		}
		if (openEnd != null && ! openEnd)
		{
			holeString += "]";
		}
		return holeString;
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

	
	/**
	 * Change the number of holes in this fingering; add open holes
	 * at the bottom to increase number of holes, or remove holes
	 * from the bottom to decrease number of holes.
	 * @param numberOfHoles
	 */
	public void setNumberOfHoles(int numberOfHoles)
	{
		boolean[] newOpenHoles = new boolean[numberOfHoles];
		Arrays.fill(newOpenHoles, true);
		if (openHole != null)
		{
			for (int i = 0; i < openHole.size() && i < numberOfHoles; ++i)
			{
				if (openHole.get(i) != null && !openHole.get(i))
				{
					newOpenHoles[i] = false;
				}
			}
		}
		setOpenHoles(newOpenHoles);
	}

	/**
	 * @return the openEnd state in the fingering
	 */
	public Boolean getOpenEnd()
	{
		return openEnd;
	}

	/**
	 * @param openEnd the openEnd state to set
	 */
	public void setOpenEnd(Boolean openEnd)
	{
		this.openEnd = openEnd;
	}

	/**
	 * Returns the optimization weight.
	 * 
	 * @return The weight. If unset, returns 1; if less than 0, returns 0. Does
	 *         NOT set the underlying weight in these circumstances.
	 */
	public Integer getOptimizationWeight()
	{
		if (optimizationWeight == null)
		{
			return 1;
		}
		if (optimizationWeight < 0)
		{
			return 0;
		}

		return optimizationWeight;
	}

	public void setOptimizationWeight(Integer optimizationWeight)
	{
		this.optimizationWeight = optimizationWeight;
	}

}
