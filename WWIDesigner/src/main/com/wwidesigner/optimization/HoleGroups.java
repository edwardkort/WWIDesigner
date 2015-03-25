package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of contiguous finger holes groups constrained to have the same
 * inter-hole spacing.
 * 
 */
public class HoleGroups
{
	protected List<HoleGroup> holeGroup;

	public HoleGroups()
	{
		holeGroup = new ArrayList<HoleGroup>();
	}

	public HoleGroups(int[][] groups)
	{
		this();
		for (int[] group : groups)
		{
			holeGroup.add(new HoleGroup(group));
		}
	}

	/**
	 * Gets the value of the holeGroup property.
	 * 
	 */
	public List<HoleGroup> getHoleGroup()
	{
		return holeGroup;
	}

	public void setHoleGroup(List<HoleGroup> holeGroup)
	{
		this.holeGroup = holeGroup;
	}

	public void addHoleGroup(HoleGroup holeGroup)
	{
		this.holeGroup.add(holeGroup);
	}

	public int[][] getHoleGroupsArray()
	{
		int numGroups = holeGroup.size();
		int[][] holeGroupsArray = new int[numGroups][];
		for (int i = 0; i < numGroups; i++)
		{
			HoleGroup group = holeGroup.get(i);
			int[] holeGroupArray = group.getHoleGroupArray();
			holeGroupsArray[i] = holeGroupArray;
		}

		return holeGroupsArray;

	}

	public static String printGroups(int[][] groups)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("{ ");
		for (int[] group : groups)
		{
			buf.append("{ ");
			for (int holeIdx : group)
			{
				buf.append(holeIdx + " ");
			}
			buf.append("} ");
		}
		buf.append("}");

		return buf.toString();
	}

}
