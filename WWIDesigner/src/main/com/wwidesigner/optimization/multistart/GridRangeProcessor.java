package com.wwidesigner.optimization.multistart;

import java.util.Arrays;

public class GridRangeProcessor extends AbstractRangeProcessor
{
	int gridPointsPerDimension;
	int[] gridPoint;

	public GridRangeProcessor(double[] lowerBound, double[] upperBound,
			int[] indicesToVary, int numberOfStarts)
	{
		super(lowerBound, upperBound, indicesToVary, numberOfStarts);

		gridPointsPerDimension = determineGranularity();
		resetRangesToGrid();
		initializeGrid();
	}

	private void initializeGrid()
	{
		gridPoint = new int[lowVector.length];
		Arrays.fill(gridPoint, 1);
	}

	private void resetRangesToGrid()
	{
		for (int i = 0; i < range.length; i++)
		{
			range[i] /= gridPointsPerDimension + 1;
		}
	}

	private int determineGranularity()
	{
		double pointsPerDimension = Math.pow(numberOfSetsToGenerate,
				1. / numberOfValuesToVary);

		return (int) Math.ceil(pointsPerDimension);
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
				vector[i] = lowVector[i] + range[i] * gridPoint[i];
			}
			else
			{
				vector[i] = lowVector[i];
			}
		}

		incrementGrid();

		return vector;
	}

	private void incrementGrid()
	{
		boolean priorReset = true;
		for (int i = 0; i < lowVector.length; i++)
		{
			if (valuesToVary[i] && priorReset)
			{
				if (gridPoint[i] == gridPointsPerDimension)
				{
					gridPoint[i] = 1;
					priorReset = true;
				}
				else
				{
					gridPoint[i]++;
					priorReset = false;
				}
			}
		}
	}

}
