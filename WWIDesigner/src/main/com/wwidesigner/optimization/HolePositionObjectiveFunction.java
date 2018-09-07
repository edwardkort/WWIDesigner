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
public class HolePositionObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Hole position";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;
	public enum BoreLengthAdjustmentType 
	{
		/**
		 * Change the position of the bottom bore point, adjusting the diameter
		 * to keep the taper angle unchanged.
		 */
		PRESERVE_TAPER,
		/**
		 * Change position of all bore points below longest bore segment,
		 * leaving bore diameters unchanged.
		 */
		PRESERVE_BELL,
		/**
		 * Change position of the bottom bore point,
		 * leaving bore diameters unchanged.
		 */
		MOVE_BOTTOM
	}
	protected BoreLengthAdjustmentType lengthAdjustmentMode;
	protected static final double MINIMUM_BORE_POINT_SPACING = 0.00001d;
	protected int bellIndex;

	public HolePositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, 
			BoreLengthAdjustmentType aLengthAdjustmentMode)
	{
		super(aCalculator, tuning, aEvaluator);
		this.lengthAdjustmentMode = aLengthAdjustmentMode;
		nrDimensions = 1 + aCalculator.getInstrument().getHole().size();
		if (aLengthAdjustmentMode == BoreLengthAdjustmentType.PRESERVE_BELL)
		{
			bellIndex = findBell(aCalculator.getInstrument());
		}
		else
		{
			bellIndex = aCalculator.getInstrument().getBorePoint().size() - 1;
		}
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
		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());

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

		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());

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

	protected void setBore(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				calculator.getInstrument().getBorePoint());
		BorePoint endPoint = boreList.getLast();

		if (lengthAdjustmentMode == BoreLengthAdjustmentType.PRESERVE_BELL)
		{
			double netChange = point[0] - endPoint.getBorePosition();
			double priorBorePoint = boreList.get(bellIndex - 1).getBorePosition();
			for (int i = bellIndex; i < boreList.size(); ++i)
			{
				BorePoint borePoint = boreList.get(i);
				double oldPosition = borePoint.getBorePosition(); 
				if (oldPosition + netChange <= priorBorePoint + MINIMUM_BORE_POINT_SPACING)
				{
					// Squeeze bore points together if necessary.
					borePoint.setBorePosition(priorBorePoint + MINIMUM_BORE_POINT_SPACING);
				}
				else
				{
					borePoint.setBorePosition(oldPosition + netChange);
				}
				priorBorePoint = borePoint.getBorePosition();
			}
		}
		else
		{
			// Don't let optimizer delete a borePoint.
			// Instead, move them up the bore a bit.
			double newEndPosition = point[0];
			int lastPointIndex = boreList.size() - 1;
			for (int i = lastPointIndex - 1; i >= 0; i--)
			{
				BorePoint borePoint = boreList.get(i);
				double currentPosition = borePoint.getBorePosition();
				if (currentPosition >= newEndPosition)
				{
					newEndPosition -= MINIMUM_BORE_POINT_SPACING;
					borePoint.setBorePosition(newEndPosition);
				}
				else
				{
					break;
				}
			}
	
			// Extrapolate/interpolate the bore diameter of end point
			if (lengthAdjustmentMode == BoreLengthAdjustmentType.PRESERVE_TAPER)
			{
				double endDiameter = BorePoint
						.getInterpolatedExtrapolatedBoreDiameter(boreList, point[0]);
				endPoint.setBoreDiameter(endDiameter);
			}
			endPoint.setBorePosition(point[0]);
		}
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Bore length",
				CONSTR_TYPE));

		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());
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

			constraints.addConstraint(new Constraint(CONSTR_CAT,
					constraintName, CONSTR_TYPE));
		}

		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName("Hole position optimizer");
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}
	
	/**
	 * Find the beginning of the instrument bell, for use with PRESERVE_BELL.
	 * Chooses the bore point that follows the longest bore segment.
	 * @param boreList
	 * @return index of bore point at start of bell.
	 */
	protected static int findBell(Instrument instrument)
	{
		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				instrument.getBorePoint());
		double longestSegment = 0;
		double lastPosition = boreList.get(0).getBorePosition();
		int bellIndex = boreList.size() - 1;
		for (int idx = 1; idx < boreList.size(); ++idx)
		{
			if (boreList.get(idx).getBorePosition() - lastPosition >= longestSegment)
			{
				bellIndex = idx;
				longestSegment = boreList.get(idx).getBorePosition();
			}
			lastPosition = boreList.get(idx).getBorePosition();
		}
		return bellIndex;
	}

}
