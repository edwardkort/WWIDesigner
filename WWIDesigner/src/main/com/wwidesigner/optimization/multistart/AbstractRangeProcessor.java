package com.wwidesigner.optimization.multistart;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomVectorGenerator;

public abstract class AbstractRangeProcessor implements RandomVectorGenerator
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
		lowVector = lowerBound.clone();

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
	
	/**
	 * For dimensions that are not varying, use values given in startValues.
	 * @param startValues
	 */
	public void setStaticValues(double[] startValues)
	{
		for ( int i = 0; i < valuesToVary.length; ++ i )
		{
			if (! valuesToVary[i])
			{
				lowVector[i] = startValues[i];
			}
		}
	}

	public abstract double[] nextVector();

	public int getNumberOfStarts()
	{
		return numberOfSetsToGenerate;
	}

}
