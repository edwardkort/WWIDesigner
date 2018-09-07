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
		this.note = null;
		this.openHole = null;
		this.openEnd = null;
		this.optimizationWeight = null;
	}

	public Fingering(int numberOfHoles)
	{
		this.note = null;
		this.openHole = null;
		this.openEnd = null;
		this.optimizationWeight = null;
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
			this.openEnd = fingering.getOpenEnd();
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
	 * @param aNote
	 *            the note to set
	 */
	public void setNote(Note aNote)
	{
		this.note = aNote;
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
	 * @param aOpenHole
	 *            the openHole to set
	 */
	public void setOpenHole(List<Boolean> aOpenHole)
	{
		this.openHole = aOpenHole;
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
		for (int i = 0; i < openHole.size(); ++i)
		{
			if (openHole.size() >= 6 && i == openHole.size()/2)
			{
				holeString += " ";
			}
			if (openHole.get(i))
			{
				holeString += "O";
			}
			else
			{
				holeString += "X";
			}
		}
		if (openEnd != null)
		{
			if (openEnd)
			{
				holeString += "_";
			}
			else
			{
				holeString += "]";
			}
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
	 * @param aOpenEnd the openEnd state to set
	 */
	public void setOpenEnd(Boolean aOpenEnd)
	{
		this.openEnd = aOpenEnd;
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

	public void setOptimizationWeight(Integer aOptimizationWeight)
	{
		this.optimizationWeight = aOptimizationWeight;
	}
	
	public static Fingering valueOf(String s)
	{
		if (! s.matches("^[XOxo][XOxo ]*(_|]|)$"))
		{
			throw new ClassCastException("String does not represent a fingering pattern");
		}
		Fingering fingering = new Fingering();
		for (int i = 0; i < s.length(); ++i)
		{
			if (s.charAt(i) == 'O' || s.charAt(i) == 'o')
			{
				fingering.addOpenHole(true);
			}
			else if (s.charAt(i) == 'X' || s.charAt(i) == 'x')
			{
				fingering.addOpenHole(false);
			}
		}
		if (s.charAt(s.length() - 1) == '_')
		{
			fingering.setOpenEnd(true);
		}
		else if (s.charAt(s.length() - 1) == ']')
		{
			fingering.setOpenEnd(false);
		}

		return fingering;
	}

}
