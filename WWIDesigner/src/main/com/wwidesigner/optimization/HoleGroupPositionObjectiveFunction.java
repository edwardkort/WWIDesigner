package com.wwidesigner.optimization;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore length and hole positions,
 * with holes equally spaced within groups:
 * - Position of end bore point.
 * - For each group, spacing within group, then spacing to next group,
 *   ending with spacing from last group to end of bore.
 * Assumes that total spacing is less than the bore length.
 * (In practice, it will be significantly less.)
 * @author Edward Kort, Burton Patkau
 *
 */
public class HoleGroupPositionObjectiveFunction extends BaseObjectiveFunction
{
	protected int[][] holeGroups;
	protected int numberOfHoleSpaces;
	// For each hole, the geometry dimension that identifies spacing after this hole.
	protected int[] dimensionByHole;
	// For each hole, the number of holes in the hole's dimension.
	// This is used to average existing lengths.
	protected double[] groupSize;

	public HoleGroupPositionObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator, int[][] holeGroups) throws Exception
	{
		super(calculator, tuning, evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer;		// MultivariateOptimizer
		setHoleGroups(holeGroups);
		
		// Length cannot be shorter than position of lowest hole.
		// (Use 1 mm past the lower edge of the lowest hole.)

		PositionInterface[] holeList = Instrument.sortList(calculator.getInstrument().getHole());
		if (holeList.length > 0)
		{
			Hole endHole = (Hole) holeList[holeList.length-1];
			lowerBounds = new double[nrDimensions];
			lowerBounds[0] = endHole.getBorePosition() + endHole.getDiameter()/2.0 + 0.001;
		}
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
		this.nrDimensions = 1 + numberOfHoleSpaces;
		
		// Compute dimensionByHole.
		
		dimensionByHole = new int[numberOfHoles];
		groupSize       = new double[numberOfHoles];

		// Dimension 0 is the position of the end bore point.
		// Dimension 1 is the spacing after the first hole.
		int dimension = 1;
		for (int i = 0; i < holeGroups.length; i++)
		{
			int[] group = holeGroups[i];
			if (group.length > 1)
			{
				// All holes but the last use the current dimension,
				// inter-hole spacing.
				for ( int j = 0; j < group.length - 1; j++ )
				{
					dimensionByHole[group[j]] = dimension;
					groupSize[group[j]] = group.length - 1;
				}
				dimension++;
			}
			if (group.length > 0)
			{
				// Last hole in the group uses the spacing after the group.
				dimensionByHole[group[group.length-1]] = dimension;
				groupSize[group[group.length-1]] = 1;
				dimension++;
			}
		}
	}

	/**
	 * @return The position of the farthest bore point.
	 */
	protected double getEndOfBore()
	{
		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		double endPosition = boreList.get(0).getBorePosition();

		for (BorePoint borePoint: boreList)
		{
			if ( borePoint.getBorePosition() > endPosition )
			{
				endPosition = borePoint.getBorePosition();
			}
		}
		return endPosition;
	}

	@Override
	public double[] getGeometryPoint()
	{
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		double[] geometry = new double[nrDimensions];
		// Set dimensions to zero, so we can accumulate group averages.
		Arrays.fill(geometry, 0.0);

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Measure hole positions from bottom to top, starting with
		// the position of the farthest bore point.

		geometry[0] = getEndOfBore();
		double priorHolePosition = geometry[0];
		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[dimensionByHole[i]] += (priorHolePosition - hole.getBorePosition()) / groupSize[i];
			priorHolePosition = hole.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		// Ensure that no bore points are beyond the new bottom position.
		// Find the farthest one out, and update its position.

		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		BorePoint endPoint = boreList.get(0);
		
		for (BorePoint borePoint: boreList)
		{
			if ( borePoint.getBorePosition() > endPoint.getBorePosition() )
			{
				endPoint = borePoint;
			}
			if ( borePoint.getBorePosition() > point[0] )
			{
				borePoint.setBorePosition(point[0]);
			}
		}
		endPoint.setBorePosition(point[0]);

		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Position the holes from bottom to top.
		double priorHolePosition = getEndOfBore();

		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition( priorHolePosition - point[dimensionByHole[i]] );
			priorHolePosition = hole.getBorePosition();
		}
		calculator.getInstrument().updateComponents();
	}

}
