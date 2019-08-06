/**
 * 
 */
package com.wwidesigner.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.wwidesigner.geometry.PositionInterface;

/**
 * @author kort
 * 
 */
public class SortedPositionList<T extends PositionInterface> extends
		LinkedList<T>
{
	public SortedPositionList()
	{
	}

	public SortedPositionList(Collection<? extends T> collection)
	{
		addAll(collection);
	}

	@Override
	public boolean add(T element)
	{
		boolean success = super.add(element);
		if (success)
		{
			sort();
		}

		return success;
	}

	@Override
	public boolean addAll(Collection<? extends T> collection)
	{
		boolean success = super.addAll(collection);
		if (success)
		{
			sort();
		}

		return success;
	}

	public SortedPositionList<T> headList(double maxExclusivePosition)
	{
		SortedPositionList<T> head = new SortedPositionList<T>();
		LinkedList<T> tempHead = new LinkedList<T>();
		for (T current : this)
		{
			if (current.getBorePosition() < maxExclusivePosition)
			{
				tempHead.add(current);
			}
			else
			{
				break;
			}
		}

		if (tempHead.size() > 0)
		{
			head.addAll(tempHead);
		}

		return head;
	}

	public void sort()
	{
		Collections.sort(this, new PositionComparator());
	}

	public class PositionComparator implements Comparator<T>
	{

		@Override
		public int compare(T arg0, T arg1)
		{
			double firstPosition = arg0.getBorePosition();
			double secondPosition = arg1.getBorePosition();

			if (firstPosition < secondPosition)
			{
				return -1;
			}

			// Honor order if the positions are the same.
			return 1;
		}

	}
}
