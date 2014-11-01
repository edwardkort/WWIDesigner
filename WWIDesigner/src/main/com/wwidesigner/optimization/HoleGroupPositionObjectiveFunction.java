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
import com.wwidesigner.optimization.Constraint.ConstraintType;
import com.wwidesigner.util.SortedPositionList;

/**
 * Optimization objective function for bore length and hole positions, with
 * holes equally spaced within groups:
 * <ul>
 * <li>Position of end bore point.</li>
 * <li>For each group, spacing within group, then spacing to next group, ending
 * with spacing from last group to end of bore.</li>
 * </ul>
 * Assumes that total spacing is less than the bore length. (In practice, it
 * will be significantly less.)
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class HoleGroupPositionObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Hole position";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;

	protected int[][] holeGroups;
	protected int numberOfHoles;
	protected int numberOfHoleSpaces;
	// For each hole, the geometry dimension that identifies spacing after this
	// hole.
	protected int[] dimensionByHole;
	// For each hole, the number of holes in the hole's dimension.
	// This is used to average existing lengths.
	protected double[] groupSize;

	public HoleGroupPositionObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator,
			int[][] holeGroups) throws Exception
	{
		super(calculator, tuning, evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		setHoleGroups(holeGroups);
	}

	public void setHoleGroups(int[][] groups) throws Exception
	{
		numberOfHoles = getNumberOfHoles();

		if (numberOfHoles > 0)
		{
			validateHoleGroups(groups, numberOfHoles);

			computeDimensionByHole(numberOfHoles);
		}

		if (nrDimensions == 1)
		{
			// BOBYQA doesn't support single dimension.
			optimizerType = OptimizerType.CMAESOptimizer;
		}

		setConstraints();
	}

	private void validateHoleGroups(int[][] groups, int numberOfHoles)
			throws Exception
	{
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
						numberOfHoleSpaces++; // There is a space not in a
												// group
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
			} // for holes in group
		} // for each group

		holeGroups = groups;

		numberOfHoleSpaces++; // The space from last hole to foot of flute

		if ((currentIdx + 1) != numberOfHoles)
		{
			throw new Exception("All holes are not in a group");
		}

		nrDimensions = 1 + numberOfHoleSpaces;
	}

	protected int getNumberOfHoles()
	{
		numberOfHoleSpaces = 0;
		int numberOfHoles = calculator.getInstrument().getHole().size();
		if (numberOfHoles == 0)
		{
			// If there are no holes, assume the list of groups is empty.
			// Only one dimension, the length of the flute.
			nrDimensions = 1;
			holeGroups = new int[0][];
			dimensionByHole = null;
			groupSize = null;
		}
		return numberOfHoles;
	}

	protected void computeDimensionByHole(int numberOfHoles)
	{
		dimensionByHole = new int[numberOfHoles];
		groupSize = new double[numberOfHoles];

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
				for (int j = 0; j < group.length - 1; j++)
				{
					dimensionByHole[group[j]] = dimension;
					groupSize[group[j]] = group.length - 1;
				}
				dimension++;
			}
			if (group.length > 0)
			{
				// Last hole in the group uses the spacing after the group.
				dimensionByHole[group[group.length - 1]] = dimension;
				groupSize[group[group.length - 1]] = 1;
				dimension++;
			}
		}
	}

	protected void setConstraints()
	{
		constraints.clearConstraints(CONSTR_CAT); // Reentrant

		constraints.addConstraint(new Constraint(CONSTR_CAT, "Bore length",
				CONSTR_TYPE));

		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());
		for (int groupIdx = 0; groupIdx < holeGroups.length; groupIdx++)
		{
			boolean isGroup = holeGroups[groupIdx].length > 1;
			String firstGroupName = getGroupName(groupIdx, sortedHoles);
			if (isGroup)
			{
				String constraintName = firstGroupName + " spacing";
				constraints.addConstraint(new Constraint(CONSTR_CAT,
						constraintName, CONSTR_TYPE));
			}
			String firstHoleName = getHoleNameFromGroup(groupIdx, false,
					sortedHoles);
			String secondHoleName = getHoleNameFromGroup(groupIdx + 1, true,
					sortedHoles);
			String constraintName = firstHoleName + " to " + secondHoleName
					+ " distance";
			constraints.addConstraint(new Constraint(CONSTR_CAT,
					constraintName, CONSTR_TYPE));
		}

		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName("Grouped hole-spacing optimizer");
	}

	protected String getHoleNameFromGroup(int groupIdx, boolean firstHole,
			PositionInterface[] sortedHoles)
	{
		String name;

		if (groupIdx >= holeGroups.length)
		{
			name = "bore end";
		}
		else
		{
			int[] group = holeGroups[groupIdx];
			int holeIdx = firstHole ? group[0] : group[group.length - 1];
			int maxHoleIdx = sortedHoles.length;

			name = Constraint.getHoleName((Hole) sortedHoles[holeIdx],
					maxHoleIdx - holeIdx, 1, maxHoleIdx);
		}

		return name;
	}

	protected String getGroupName(int groupIdx, PositionInterface[] sortedHoles)
	{
		String name = "";

		int[] group = holeGroups[groupIdx];
		boolean isGroup = group.length > 1;
		int maxHoleIdx = sortedHoles.length;

		if (isGroup)
		{
			name += "Group " + (groupIdx + 1) + " (";
		}
		for (int i = 0; i < group.length; i++)
		{
			if (i > 0)
			{
				name += ", ";
			}
			int holeIdx = group[i];
			name += Constraint.getHoleName((Hole) sortedHoles[holeIdx],
					maxHoleIdx - holeIdx, 1, maxHoleIdx);
		}
		if (isGroup)
		{
			name += ")";
		}

		return name;
	}

	/**
	 * @return The position of the farthest bore point.
	 */
	protected double getEndOfBore()
	{
		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		double endPosition = boreList.get(0).getBorePosition();

		for (BorePoint borePoint : boreList)
		{
			if (borePoint.getBorePosition() > endPosition)
			{
				endPosition = borePoint.getBorePosition();
			}
		}
		return endPosition;
	}

	@Override
	public double[] getGeometryPoint()
	{
		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());

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
			geometry[dimensionByHole[i]] += (priorHolePosition - hole
					.getBorePosition()) / groupSize[i];
			priorHolePosition = hole.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		setBore(point);
		
		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Position the holes from bottom to top.
		double priorHolePosition = getEndOfBore();

		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition(priorHolePosition - point[dimensionByHole[i]]);
			priorHolePosition = hole.getBorePosition();
		}
		calculator.getInstrument().updateComponents();
	}

	protected void setBore(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		// Ensure that no bore points are beyond the new bottom position. FOR
		// NOW, NO.
		// Find the farthest one out, and update its position.

		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				calculator.getInstrument().getBorePoint());
		BorePoint endPoint = boreList.getLast();

		// Don't let optimizer delete a borePoint
		BorePoint almostEndPoint = boreList.get(boreList.size() - 2);
		double almostEndPosition = almostEndPoint.getBorePosition();
		if (point[0] <= almostEndPosition)
		{
			point[0] = almostEndPosition + 0.01;
		}

		// Extrapolate/interpolate the bore diameter of end point
		double endDiameter = BorePoint.getInterpolatedExtrapolatedBoreDiameter(
				boreList, point[0]);
		endPoint.setBorePosition(point[0]);
		endPoint.setBoreDiameter(endDiameter);
	}

}
