package com.wwidesigner.optimization;

import java.util.Arrays;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore length and hole positions, with
 * holes equally spaced within groups:
 * <ul>
 * <li>Position of end bore point.</li>
 * <li>Position of top hole as fraction of bore length.</li>
 * <li>For each group, spacing within group, then spacing to next group.</li>
 * </ul>
 * Assumes that total spacing is less than the bore length. (In practice, it
 * will be significantly less.)
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class HoleGroupPositionFromTopObjectiveFunction extends
		HoleGroupPositionObjectiveFunction
{

	public HoleGroupPositionFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int[][] aHoleGroups) throws Exception
	{
		super(aCalculator, tuning, aEvaluator, aHoleGroups);
	}

	@Override
	public double[] getGeometryPoint()
	{
		// Geometry dimensions are distances between holes.
		// First dimension is bore length.
		// Second dimension is ratio between first hole and top of bore,
		// expressed as a fraction of the bore length (both measured from
		// mouthpiece position).

		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());

		// First just extract bore length and hole positions.
		double[] dimensions = new double[numberOfHoles + 1];

		dimensions[0] = getEndOfBore();

		for (int i = 0; i < sortedHoles.length; i++)
		{
			Hole hole = (Hole) sortedHoles[i];
			dimensions[i + 1] = hole.getBorePosition();
		}

		return convertDimensionsToGeometry(dimensions);
	}

	private double[] convertDimensionsToGeometry(double[] dimensions)
	{
		double[] geometry = new double[nrDimensions];
		// Set dimensions to zero, so we can accumulate group averages.
		Arrays.fill(geometry, 0.0);

		geometry[0] = dimensions[0];
		geometry[1] = getTopRatio(dimensions[0], dimensions[1]);

		double priorPosition = dimensions[1];
		for (int i = 2; i <= numberOfHoles; i++)
		{
			geometry[dimensionByHole[i - 2] + 1] += (dimensions[i] - priorPosition)
					/ groupSize[i - 2];
			priorPosition = dimensions[i];
		}

		return geometry;
	}

	private double[] convertGeometryToDimensions(double[] geometry)
	{
		// geometry: index 0 is bore length
		// Index 1 is ratio of top hole to bore length, measured from splitting
		// edge
		// Index 2 ... are spacings within hole groups (one each) and spacing
		// between groups.
		// dimensions: index 0 is bore length
		// index 1 .. numberOfHoles are hole positions (from top).
		double[] dimensions = new double[numberOfHoles + 1];

		dimensions[0] = geometry[0]; // bore length
		dimensions[1] = getTopPosition(geometry[0], geometry[1]);

		double priorPosition = dimensions[1];
		for (int i = 2; i <= numberOfHoles; i++)
		{
			dimensions[i] = priorPosition
					+ geometry[dimensionByHole[i - 2] + 1];
			priorPosition = dimensions[i];
		}

		return dimensions;
	}

	/**
	 * Calculates to top hole position as a ratio to the bore length. Top hole
	 * ratio is measured from the splitting edge for both numerator and
	 * denominator.
	 * 
	 * @param boreLength
	 * @param topHolePosition
	 * @return ratio
	 */
	private double getTopRatio(double boreLength, double topHolePosition)
	{
		double realOrigin = calculator.getInstrument().getMouthpiece()
				.getPosition();

		return (topHolePosition - realOrigin) / (boreLength - realOrigin);
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		setBore(point);
		double[] dimensions = convertGeometryToDimensions(point);

		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());

		for (int i = 0; i < sortedHoles.length; i++)
		{
			Hole hole = (Hole) sortedHoles[i];
			double holePosition = dimensions[i + 1];
			hole.setBorePosition(holePosition);
		}

		calculator.getInstrument().updateComponents();
	}

	/**
	 * 
	 * @param boreLength
	 *            Measured from arbitrary origin
	 * @param topHoleRatio
	 *            Ratio of top hole position to bore length, both measured from
	 *            splitting edge
	 * @return Top hole position, measured from arbitrary origin
	 */
	private double getTopPosition(double boreLength, double topHoleRatio)
	{
		double realOrigin = calculator.getInstrument().getMouthpiece()
				.getPosition();

		double boreLengthFromEdge = boreLength - realOrigin;
		double topHolePosition = topHoleRatio * boreLengthFromEdge + realOrigin;

		return topHolePosition;
	}

	@Override
	protected void setConstraints()
	{
		constraints.clearConstraints(CONSTR_CAT); // Reentrant

		constraints.addConstraint(new Constraint(CONSTR_CAT, "Bore length",
				CONSTR_TYPE));

		String constraintName = "";
		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());
		for (int groupIdx = 0; groupIdx < holeGroups.length; groupIdx++)
		{
			if (groupIdx == 0)
			{
				constraintName = "Ratio, from splitting edge, of top-hole position to bore length";
				constraints
						.addConstraint(new Constraint(CONSTR_CAT,
								constraintName,
								Constraint.ConstraintType.DIMENSIONLESS));
			}
			boolean isGroup = holeGroups[groupIdx].length > 1;
			String firstGroupName = getGroupName(groupIdx, sortedHoles);
			if (isGroup)
			{
				constraintName = firstGroupName + " spacing";
				constraints.addConstraint(new Constraint(CONSTR_CAT,
						constraintName, CONSTR_TYPE));
			}
			if ((groupIdx + 1) < holeGroups.length) // Not last group
			{
				String firstHoleName = getHoleNameFromGroup(groupIdx, false,
						sortedHoles);
				String secondHoleName = getHoleNameFromGroup(groupIdx + 1,
						true, sortedHoles);
				constraintName = firstHoleName + " to " + secondHoleName
						+ " distance";
				constraints.addConstraint(new Constraint(CONSTR_CAT,
						constraintName, CONSTR_TYPE));
			}
		}

		constraints.setNumberOfHoles(sortedHoles.length);
		constraints.setObjectiveDisplayName("Grouped hole-spacing optimizer");

		constraints.setHoleGroups(holeGroups);
	}

}
