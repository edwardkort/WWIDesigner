package com.wwidesigner.optimization.multistart;

import java.util.Arrays;

public abstract class AbstractRangeProcessor
{
	protected double[] lowVector;
	protected double[] range;
	protected boolean[] valuesToVary;
	protected int numberOfSetsToGenerate;
	protected int numberOfValuesToVary;

	public AbstractRangeProcessor(double[] lowerBound, double[] upperBound,
			int[] indicesToVary, int numberOfStarts)
	{
		numberOfSetsToGenerate = numberOfStarts;
		numberOfValuesToVary = indicesToVary == null ? lowerBound.length : indicesToVary.length;
		lowVector = lowerBound;

		int vectorLength = lowVector.length;
		range = new double[vectorLength];

		for (int i = 0; i < vectorLength; i++)
		{
			range[i] = upperBound[i] - lowerBound[i];
		}

		valuesToVary = new boolean[vectorLength];
		if (indicesToVary == null)
		{
			Arrays.fill(valuesToVary, true);
		}
		else
		{
			Arrays.fill(valuesToVary, false);
			for (int idx : indicesToVary)
			{
				valuesToVary[idx] = true;
			}
		}
	}

	public abstract double[] nextVector(double[] startValues);

	public int getNumberOfStarts()
	{
		return numberOfSetsToGenerate;
	}

}
