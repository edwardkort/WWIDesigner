package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole positions and diameters:
 * - Distance from top of bore to first group,
 * - For each group, spacing within group and spacing to next group,
 *   ending with spacing from last group to end of bore.
 * - For each hole, ratio of hole diameter to bore diameter.
 * @author Edward Kort, Burton Patkau
 *
 */
public class HoleGroupObjectiveFunction extends BaseObjectiveFunction
{
	protected int[][] holeGroups;
	protected int numberOfHoleSpaces;
	// For each hole, the geometry dimension that identifies spacing before this hole.
	protected int[] dimensionByHole;
	// For each hole, the number of holes in the hole's dimension.
	// This is used to average existing lengths.
	protected double[] groupSize;

	public HoleGroupObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator, int[][] holeGroups) throws Exception
	{
		super(calculator, tuning, evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer;		// MultivariateOptimizer
		maxIterations = 5000;
		setHoleGroups(holeGroups);
		nrInterpolations = 2 * nrDimensions;
	}

	public void setHoleGroups(int[][] groups) throws Exception
	{
		// Check list of groups, and count number of hole spaces.

		numberOfHoleSpaces = 0;
		int numberOfHoles = calculator.getInstrument().getHole().size();
		boolean first = true;
		int currentIdx = 0;
		for (int[] group : groups)
		{
			if (group.length > 1)
			{
				numberOfHoleSpaces++; // One for each group
			}
			boolean firstInGroup = true;
			for (int holeIdx : group)
			{
				if (first)
				{
					if (holeIdx != 0)
					{
						throw new Exception(
								"Groups must start with the first hole (index 0)");
					}
					first = false;
				}
				if (firstInGroup)
				{
					firstInGroup = false;
					if (currentIdx != holeIdx)
					{
						if (holeIdx != (currentIdx + 1))
						{
							throw new Exception("A hole is missing from groups");
						}
						numberOfHoleSpaces++; // There is a space not in a group
					}
				}
				else
				{
					if (currentIdx != holeIdx)
					{
						if (holeIdx != (currentIdx + 1))
						{
							throw new Exception(
									"A hole is missing within a group");
						}
					}
				}
				currentIdx = holeIdx;
			}
		}
		numberOfHoleSpaces++; // The space from last hole to foot of flute

		if ((currentIdx + 1) != numberOfHoles)
		{
			throw new Exception("All holes are not in a group");
		}

		this.holeGroups = groups;
		this.nrDimensions = 1 + numberOfHoleSpaces + numberOfHoles;
		
		// Compute dimensionByHole.
		
		dimensionByHole = new int[numberOfHoles];
		groupSize       = new double[numberOfHoles];

		int dimension = 0;	// Dimension 0 is spacing before first hole.
		for (int i = 0; i < holeGroups.length; i++)
		{
			int[] group = holeGroups[i];
			if (group.length > 0)
			{
				// First hole in the group uses the spacing before the group.
				dimensionByHole[group[0]] = dimension;
				groupSize[group[0]] = 1;
				dimension++;
			}
			// All holes but the first use the current dimension.
			for ( int j = 1; j < group.length; j++ )
			{
				dimensionByHole[group[j]] = dimension;
				groupSize[group[j]] = group.length - 1;
			}
			dimension++;
		}
	}

	@Override
	public double[] getGeometryPoint()
	{
		PositionInterface[] sortedPoints
				= Instrument.sortList(calculator.getInstrument().getBorePoint());
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		double[] geometry = new double[nrDimensions];
		// Set dimensions to zero, so we can accumulate group averages.
		for (int d = 0; d < nrDimensions; d++ )
		{
			geometry[d] = 0;
		}
		// First dimension is first hole position relative to bore origin.
		double priorHolePosition = 0; 

		int i = 0;	// Hole number
		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[dimensionByHole[i]] 
					+= (hole.getBorePosition() - priorHolePosition)/groupSize[i];
			priorHolePosition = hole.getBorePosition();
		}
		// Final length is the position of the end of the bore
		// relative to the last hole.
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double endOfBore = lastPoint.getBorePosition();
		geometry[numberOfHoleSpaces] = endOfBore - priorHolePosition;

		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i+numberOfHoleSpaces+1] = hole.getRatio();
		}

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		PositionInterface[] sortedPoints
				= Instrument.sortList(calculator.getInstrument().getBorePoint());
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		// First dimension is first hole position relative to bore origin.
		double priorHolePosition = 0; 

		int i = 0;		// Hole number.
		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition( priorHolePosition + point[dimensionByHole[i]] );
			priorHolePosition = hole.getBorePosition();
		}

		// Final length is the position of the end of the bore
		// relative to the last hole.
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		lastPoint.setBorePosition( priorHolePosition + point[numberOfHoleSpaces]);

		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setRatio( point[i+numberOfHoleSpaces+1] );
		}

		calculator.getInstrument().updateComponents();
	}

}
