/**
 * Optimization objective function for bore diameters at existing bore points.
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
import com.wwidesigner.geometry.Termination;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for bore diameters at existing bore points.
 * The optimization dimensions are:
 * <ul>
 * <li>Bore diameter at the foot.</li>
 * <li>For interior bore points from bottom to top,
 *  ratio of diameters at next bore point to this bore point.</li>
 * </ul>
 * The diameters at the top <i>N</i> bore points are left unchanged.
 * Bore point positions are invariant.
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to control
 * the direction of taper.  If lower bound is 1.0, bore flares out toward bottom;
 * if upper bound is 1.0, bore tapers inward toward bottom.
 * 
 * Do not use with other optimizers that might change the number of bore points.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreDiameterObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore diameters";
	public static final String DISPLAY_NAME = "Bore Diameter optimizer";
	// First bore point affected by optimization.
	protected final int unchangedBorePoints;
	// Invariant: nrDimensions + unchangedBorePoints = number of bore points.

	/**
	 * Create an optimization objective function for bore diameters at existing
	 * bore points.
	 * @param calculator
	 * @param tuning
	 * @param evaluator
	 * @param unchangedBorePoints - Leave diameter unchanged for this many bore points
	 * from the top of the bore.
	 */
	public BoreDiameterObjectiveFunction(InstrumentCalculator calculator,
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

	public BoreDiameterObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		this(calculator, tuning, evaluator, 1);
	}

	protected void setConstraints()
	{
		String name;
		int dimension;
		int pointNr = borePointNr(0);
		name = "Diameter at bore point " + String.valueOf(pointNr)
				+ " (bottom)";
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				name, ConstraintType.DIMENSIONAL));
		for (dimension = 1; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			name = "Ratio of diameters, bore point " + String.valueOf(pointNr + 1)
					+ " / bore point " + String.valueOf(pointNr);
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
		// Process bore points in reverse order, from bottom to top.
		return nrDimensions + unchangedBorePoints - dimensionIdx;
	}

	/**
	 * Point number used as an initial reference for the remaining points.
	 */
	protected int referencePointNr()
	{
		return nrDimensions + unchangedBorePoints;
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
		double nextBoreDia = borePoint.getBoreDiameter();
		geometry[0] = borePoint.getBoreDiameter();

		borePoint = (BorePoint) sortedPoints[referencePointNr() - 1];
		for (dimension = 1; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			borePoint = (BorePoint) sortedPoints[pointNr - 1];
			if (borePoint.getBoreDiameter() >= 0.000001)
			{
				geometry[dimension] = nextBoreDia/borePoint.getBoreDiameter();
			}
			else
			{
				geometry[dimension] = nextBoreDia/0.000001;
			}
			nextBoreDia = borePoint.getBoreDiameter();
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
		double terminationChange = point[0] - borePoint.getBoreDiameter();
		borePoint.setBoreDiameter(point[0]);
		Termination termination = calculator.getInstrument().getTermination();
		if (termination != null)
		{
			// Change termination flange diameter as well, to preserve the flange width.
			termination.setFlangeDiameter(termination.getFlangeDiameter() + terminationChange);
		}

		borePoint = (BorePoint) sortedPoints[referencePointNr() - 1];
		double nextBoreDia = borePoint.getBoreDiameter();
		for (dimension = 1; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointNr(dimension);
			borePoint = (BorePoint) sortedPoints[pointNr - 1];
			if (point[dimension] >= 0.000001)
			{
				borePoint.setBoreDiameter(nextBoreDia/point[dimension]);
			}
			else
			{
				borePoint.setBoreDiameter(nextBoreDia/0.000001);
			}
			nextBoreDia = borePoint.getBoreDiameter();
		}
		calculator.getInstrument().updateComponents();
	}
}
