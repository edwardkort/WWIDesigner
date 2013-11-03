package com.wwidesigner.optimization.multistart;


public class RandomRangeProcessor extends AbstractRangeProcessor
{
	public RandomRangeProcessor(double[] lowerBound, double[] upperBound,
			int[] indicesToVary, int numberOfStarts)
	{
		super(lowerBound, upperBound, indicesToVary, numberOfStarts);
	}

	@Override
	public double[] nextVector()
	{
		int vectorLength = lowVector.length;
		double[] vector = new double[vectorLength];

		for (int i = 0; i < vectorLength; i++)
		{
			if (valuesToVary[i])
			{
				vector[i] = lowVector[i] + range[i] * Math.random();
			}
			else
			{
				vector[i] = lowVector[i];
			}
		}

		return vector;
	}

}
