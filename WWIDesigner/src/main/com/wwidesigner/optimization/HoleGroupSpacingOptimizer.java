/**
 * 
 */
package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * @author kort
 * 
 */
@Deprecated
public class HoleGroupSpacingOptimizer extends InstrumentOptimizer
{
	protected static int defaultNumberOfInterpolationPoints = 30;

	protected int[][] holeGroups;
	protected int numberOfHoleSpaces;
	protected int[] vectorIdxByHole;

	/**
	 * @param inst
	 * @param calculator
	 * @param aTuning
	 */
	public HoleGroupSpacingOptimizer(Instrument inst,
			InstrumentCalculator calculator, TuningInterface aTuning)
	{
		super(defaultNumberOfInterpolationPoints, inst, calculator, aTuning);
	}

	/**
	 * 
	 * @param groups
	 *            A group represents contiguous holes having equal spacing. Each
	 *            element in {@link HoleGroups} is the indices (0 based) of
	 *            contiguous holes that make up a group, starting from the top
	 *            hole.
	 * @throws Exception
	 *             When the indices do not contain all of the holes, are not
	 *             contiguous, or are not in order.
	 */
	public void setHoleGroups(int[][] groups) throws Exception
	{
		numberOfHoleSpaces = 0;
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

		if ((currentIdx + 1) != instrument.getHole().size())
		{
			throw new Exception("All holes are not in a group");
		}

		this.holeGroups = groups;
	}

	/**
	 * stateVector has the following values:<br/>
	 * total bore length<br/>
	 * for each hole group, from the top, the spacing<br/>
	 * distance from bottom hole to foot of flute<br/>
	 * the hole/bore diameter ratio.
	 * 
	 * @see com.wwidesigner.optimization.InstrumentOptimizerInterface#getStateVector
	 *      ()
	 */
	@Override
	public double[] getStateVector()
	{
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		int len = 1 + numberOfHoleSpaces + sortedHoles.length;

		double[] stateVector = new double[len];
		vectorIdxByHole = new int[sortedHoles.length];

		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		stateVector[0] = lastPoint.getBorePosition();

		int vectorIdx = 1;
		for (int i = 0; i < holeGroups.length; i++)
		{
			int[] group = holeGroups[i];

			// Add intergroup spacing, if necessary
			if (i > 0)
			{
				int[] priorGroup = holeGroups[i - 1];
				int priorIdx = priorGroup[priorGroup.length - 1];
				int thisIdx = group[0];
				if (priorIdx != thisIdx)
				{
					vectorIdxByHole[priorIdx] = vectorIdx;
					double interspace = sortedHoles[thisIdx].getBorePosition()
							- sortedHoles[priorIdx].getBorePosition();
					stateVector[vectorIdx++] = interspace;
				}
			}

			// Set the group's spacing equal to the average existing spacing
			if (group.length > 1)
			{
				double groupSpacing = 0.;
				for (int j = 1; j < group.length; j++)
				{
					int thisIdx = group[j - 1];
					int nextIdx = group[j];
					vectorIdxByHole[thisIdx] = vectorIdx;
					groupSpacing += sortedHoles[nextIdx].getBorePosition()
							- sortedHoles[thisIdx].getBorePosition();
				}
				groupSpacing /= group.length;
				stateVector[vectorIdx++] = groupSpacing;
			}
		}

		// Add the spacing from the last hole to the bore end
		vectorIdxByHole[sortedHoles.length - 1] = vectorIdx;
		double endSpacing = lastPoint.getBorePosition()
				- sortedHoles[sortedHoles.length - 1].getBorePosition();
		stateVector[vectorIdx++] = endSpacing;

		for (int i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			stateVector[vectorIdx++] = hole.getRatio();
		}

		return stateVector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.InstrumentOptimizerInterface#updateGeometry
	 * (double[])
	 */
	@Override
	public void updateGeometry(double[] stateVector)
	{
		prepareGeometryUpdate(stateVector);

		instrument.updateComponents();
	}

	protected void prepareGeometryUpdate(double[] stateVector)
	{
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		lastPoint.setBorePosition(stateVector[0]);

		double accumulatedDistance = 0.;
		for (int i = sortedHoles.length; i > 0; --i)
		{
			int holeIdx = i - 1;
			Hole hole = (Hole) sortedHoles[holeIdx];
			int vectorIdx = vectorIdxByHole[holeIdx];
			double stateVectorValue = stateVector[vectorIdx];
			double holePosition = stateVector[0] - stateVectorValue
					- accumulatedDistance;
			hole.setBorePosition(holePosition);
			accumulatedDistance += stateVectorValue;
		}

		for (int i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setRatio(stateVector[1 + numberOfHoleSpaces + i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.InstrumentOptimizer#setOptimizationFunction
	 * ()
	 */
	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicImpedanceOptimizationFunction(this,
				tuning);
	}

}
