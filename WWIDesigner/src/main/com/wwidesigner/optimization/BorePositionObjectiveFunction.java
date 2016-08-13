/**
 * Optimization objective function for positioning existing bore points.
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

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for positioning existing bore points.
 * The optimization dimensions are:
 * <ul>
 * <li>Absolute position of bottom bore point.</li>
 * <li>For interior bore points between top and bottom,
 * distance from prior bore point to this bore point,
 * as a fraction of the distance from the prior bore point to the bottom.</li>
 * </ul>
 * The absolute position of the top <i>N</i> bore points and diameters of all bore points
 * are invariant.
 * 
 * Do not use with other optimizers that might change the number of bore points.
 * 
 * @author Burton Patkau
 * 
 */
public class BorePositionObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore point positions";
	public static final String DISPLAY_NAME = "Bore Position optimizer";
	// Number of invariant bore points at the top of the instrument.
	protected final int unchangedBorePoints;
	// Invariant: nrDimensions + unchangedBorePoints = number of bore points.

	public BorePositionObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator, int unchangedBorePoints)
	{
		super(calculator, tuning, evaluator);
		int nrBorePoints = calculator.getInstrument().getBorePoint().size();
		if (unchangedBorePoints >= 1)
		{
			this.unchangedBorePoints = unchangedBorePoints;
		}
		else
		{
			// At a minimum, top bore point is unchanged.
			this.unchangedBorePoints = 1;
		}
		nrDimensions = nrBorePoints - unchangedBorePoints;
		if (nrDimensions > 1)
		{
			optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		}
		else
		{
			optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		}
		maxEvaluations = 10000;
		setConstraints();
	}

	public BorePositionObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		this(calculator, tuning, evaluator, 1);
	}

	protected void setConstraints()
	{
		int nrBorePoints = nrDimensions + unchangedBorePoints;
		String name;
		int dimension;
		int pointNr = borePointNr(0);
		name = "Position of bore point " + String.valueOf(pointNr)
				+ " (bottom)";
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				name, ConstraintType.DIMENSIONAL));
		for (dimension = 1; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			name = "Relative position of bore point " + String.valueOf(pointNr)
					+ " between points " + String.valueOf(pointNr - 1)
					+ " and " + String.valueOf(nrBorePoints);
			constraints.addConstraint(new Constraint(CONSTR_CAT,
					name, ConstraintType.DIMENSIONLESS));
		}
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}
	
	/**
	 * Convert a dimension number in 0 .. nrDimensions-1
	 * to a bore point number in unchangedBorePoints+1 .. nrBorePoints.
	 */
	protected int borePointNr(int dimensionIdx)
	{
		if (dimensionIdx == 0)
		{
			// Bottom bore point.
			return nrDimensions + unchangedBorePoints;
		}
		// Process remaining bore points in order, from top to bottom.
		return unchangedBorePoints + dimensionIdx;
	}

	/**
	 * Point number used as an initial reference for the remaining points.
	 */
	protected int referencePointNr()
	{
		return unchangedBorePoints;
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		int dimension;
		int pointNr = borePointNr(0);
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[pointNr - 1];
		double lastBorePosition = borePoint.getBorePosition();
		geometry[0] = borePoint.getBorePosition();

		borePoint = (BorePoint) sortedPoints[referencePointNr() - 1];
		double priorBorePosition = borePoint.getBorePosition();
		for (dimension = 1; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			borePoint = (BorePoint) sortedPoints[pointNr - 1];
			geometry[dimension] = (borePoint.getBorePosition() - priorBorePosition)
					/ (lastBorePosition - priorBorePosition);
			priorBorePosition = borePoint.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		int dimension;
		int pointNr = borePointNr(0);
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[pointNr - 1];
		borePoint.setBorePosition(point[0]);
		double lastBorePosition = borePoint.getBorePosition();

		borePoint = (BorePoint) sortedPoints[referencePointNr() - 1];
		double priorBorePosition = borePoint.getBorePosition();
		for (dimension = 1; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			borePoint = (BorePoint) sortedPoints[pointNr - 1];
			borePoint.setBorePosition(priorBorePosition
					+ point[dimension] * (lastBorePosition - priorBorePosition));;
			priorBorePosition = borePoint.getBorePosition();
		}
		calculator.getInstrument().updateComponents();
	}

	@Override
	public void setLowerBounds(double[] lowerBounds)
	{
		PositionInterface[] sortedHoles = Instrument.sortList(calculator
				.getInstrument().getHole());
		double bottomHolePosition;
		if (sortedHoles.length > 1)
		{
			bottomHolePosition = sortedHoles[sortedHoles.length - 1].getBorePosition();
		}
		else {
			// No holes.  Use mid-point of bore.
			PositionInterface[] sortedPoints = Instrument.sortList(calculator
					.getInstrument().getBorePoint());
			bottomHolePosition = 0.5 * (sortedPoints[0].getBorePosition()
					+ sortedPoints[sortedPoints.length - 1].getBorePosition());
		}
		if (lowerBounds[0] < bottomHolePosition + 0.012)
		{
			// Bottom bore point must be below the bottom hole.
			lowerBounds[0] = bottomHolePosition + 0.012;
		}
		super.setLowerBounds(lowerBounds);
	}
}
