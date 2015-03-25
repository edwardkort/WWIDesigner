package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of contiguous finger hole indices constrained to have the same
 * inter-hole spacing.
 * 
 * 
 * <p>
 * Java class for HoleGroup complex type.
 * 
 */
public class HoleGroup
{
	protected List<Integer> holeIdx;

	public HoleGroup()
	{
		holeIdx = new ArrayList<Integer>();
	}

	public HoleGroup(int[] group)
	{
		this();
		for (int idx : group)
		{
			addHole(idx);
		}
	}

	/**
	 * Gets the value of the holeIdx property.
	 * 
	 */
	public List<Integer> getHoleIdx()
	{
		return holeIdx;
	}

	public void setHoleIdx(List<Integer> holeIdx)
	{
		this.holeIdx = holeIdx;
	}

	public void addHole(Integer hole)
	{
		if (hole != null && hole >= 0)
		{
			holeIdx.add(hole);
		}
	}

	public int[] getHoleGroupArray()
	{
		int numberOfIdx = holeIdx.size();
		int[] idx = new int[numberOfIdx];
		for (int i = 0; i < numberOfIdx; i++)
		{
			Integer idxValue = holeIdx.get(i);
			if (idxValue != null && idxValue >= 0)
			{
				idx[i] = idxValue;
			}
		}

		return idx;
	}

}
