/**
 * Optimization objective function for bore length and hole positions:
 * 
 * Copyright (C) 2016, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.optimization;

import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for bore length and hole positions:
 * <ul>
 * <li>Position of end bore point.</li>
 * <li>For each hole, spacing below the hole to the next hole, or (for the last
 * hole) to end of bore.</li>
 * </ul>
 * Assumes that total spacing is less than the bore length. (In practice, it
 * will be significantly less.)
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class HolePositionObjectiveFunction extends BaseObjectiveFunction implements BoreLengthAdjustmentInterface
{
	public static final String CONSTR_CAT = "Hole position";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;

	protected BoreLengthAdjuster boreLengthAdjuster;

	public HolePositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator,
			BoreLengthAdjustmentType aLengthAdjustmentMode)
	{
		super(aCalculator, tuning, aEvaluator);
		boreLengthAdjuster = new BoreLengthAdjuster(this, aLengthAdjustmentMode);
		nrDimensions = 1 + aCalculator.getInstrument().getHole().size();
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		if (nrDimensions == 1)
		{
			// BOBYQA doesn't support single dimension.
			optimizerType = OptimizerType.CMAESOptimizer;
		}
		setConstraints();
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
		PositionInterface[] sortedHoles = Instrument
				.sortList(calculator.getInstrument().getHole());

		double[] geometry = new double[nrDimensions];

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Measure hole positions from bottom to top, starting with
		// the position of the farthest bore point.

		geometry[0] = getEndOfBore();
		double priorHolePosition = geometry[0];

		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i + 1] = priorHolePosition - hole.getBorePosition();
			priorHolePosition = hole.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		setBore(point);

		PositionInterface[] sortedHoles = Instrument
				.sortList(calculator.getInstrument().getHole());

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Position the holes from bottom to top.
		double priorHolePosition = point[0];

		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition(priorHolePosition - point[i + 1]);
			priorHolePosition = hole.getBorePosition();
		}

		calculator.getInstrument().updateComponents();
	}

	public void setBore(double[] point)
	{
		boreLengthAdjuster.setBore(point);
	}

	protected void setConstraints()
	{
		constraints.addConstraint(
				new Constraint(CONSTR_CAT, "Bore length", CONSTR_TYPE));

		PositionInterface[] sortedHoles = Instrument
				.sortList(calculator.getInstrument().getHole());
		int lastIdx = sortedHoles.length;
		for (int i = lastIdx, idx = 0; i > 0; i--, idx++)
		{
			String name = Constraint.getHoleName((Hole) sortedHoles[idx], i, 1,
					lastIdx);
			String nextName = "";
			if (i == 1)
			{
				nextName = "bore end";
			}
			else
			{
				nextName = Constraint.getHoleName((Hole) sortedHoles[idx + 1],
						i - 1, 1, lastIdx);
			}
			String constraintName = name + " to " + nextName + " distance";

			constraints.addConstraint(
					new Constraint(CONSTR_CAT, constraintName, CONSTR_TYPE));
		}

		constraints
				.setNumberOfHoles(calculator.getInstrument().getHole().size());
		constraints.setObjectiveDisplayName("Hole position optimizer");
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
