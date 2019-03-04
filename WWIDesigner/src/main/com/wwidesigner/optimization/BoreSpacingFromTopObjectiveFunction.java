/**
 * Optimization objective function for positioning existing bore points
 * based on absolute spacing.
 * 
 * Copyright (C) 2019, Edward Kort, Antoine Lefebvre, Burton Patkau.
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
 * Optimization objective function for positioning existing bore points at
 * top of bore.  The optimization dimensions are:
 * <ul>
 * <li>For bore points from the top down, spacing from this bore point
 * to next bore point.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The positions of bore points below these are left unchanged.
 * Bore point diameters are unchanged. <br>
 * 
 * Do not use with other optimizers that might change the number of bore points.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreSpacingFromTopObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore point positions";
	public static final String DISPLAY_NAME = "Bore Spacing (from top) optimizer";

	/**
	 * Create an optimization objective function for bore point spacing,
	 * from the top of the bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aChangedBorePoints
	 *            - Number of bore points to optimize, from top.
	 */
	public BoreSpacingFromTopObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, int aChangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		int nrBorePoints = aCalculator.getInstrument().getBorePoint().size();
		nrDimensions = aChangedBorePoints;
		if (nrDimensions >= nrBorePoints)
		{
			// At least the top bore point is left unchanged.
			nrDimensions = nrBorePoints - 1;
		}
		if (nrDimensions < 1)
		{
			// At least one bore point is changed.
			nrDimensions = 1;
		}
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

	/**
	 * Create an optimization objective function for bore point spacing,
	 * from the top of the bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param pointName
	 *            - The lowest bore point moved will be the lowest
	 *            bore point with a name that contains pointName.
	 */
	public BoreSpacingFromTopObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, String pointName)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction.getLowestPoint(
						aCalculator.getInstrument(), pointName));
	}

	/**
	 * Create an optimization objective function for bore point spacing,
	 * from the top of the bore.  The lowest bore point moved will be
	 * the lowest bore point with a name that contains "Head".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public BoreSpacingFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction.getLowestPoint(
						aCalculator.getInstrument(), "Head"));
	}

	protected void setConstraints()
	{
		String name;
		int dimension;
		for (dimension = 0; dimension < nrDimensions; ++dimension)
		{
			name = "Distance from bore point " + String.valueOf(dimension + 1)
					+ " to point " + String.valueOf(dimension + 2);
			constraints.addConstraint(new Constraint(CONSTR_CAT,
					name, ConstraintType.DIMENSIONAL));
		}
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}
	
	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		int dimension;
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint  = (BorePoint) sortedPoints[0];
		double priorBorePosition = borePoint.getBorePosition();
		for (dimension = 0; dimension < nrDimensions; ++dimension)
		{
			borePoint = (BorePoint) sortedPoints[dimension + 1];
			geometry[dimension] = (borePoint.getBorePosition() - priorBorePosition);
			priorBorePosition = borePoint.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		int dimension;
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[0];
		double priorBorePosition = borePoint.getBorePosition();
		for (dimension = 0; dimension < nrDimensions; ++dimension)
		{
			borePoint = (BorePoint) sortedPoints[dimension + 1];
			borePoint.setBorePosition(priorBorePosition + point[dimension]);
			priorBorePosition = borePoint.getBorePosition();
		}
		calculator.getInstrument().updateComponents();
	}

	@Override
	public void setUpperBounds(double[] aUpperBounds)
	{
		// If necessary, adjust upper bounds to prevent changing order of bore points.
		if (nrDimensions + 1 < calculator.getInstrument().getBorePoint().size() )
		{
			int dimension;
			PositionInterface[] sortedPoints = Instrument.sortList(calculator
					.getInstrument().getBorePoint());
			BorePoint borePoint = (BorePoint) sortedPoints[0];
			double topPosition = borePoint.getBorePosition();
			borePoint = (BorePoint) sortedPoints[nrDimensions + 1];
			double unchangedPosition = borePoint.getBorePosition();
			double availableSpace = unchangedPosition - topPosition;
			double upperBound = 0.0;
			for (dimension = 0; dimension < nrDimensions; ++dimension)
			{
				upperBound += aUpperBounds[dimension];
			}
			if (upperBound + 0.0001 > availableSpace)
			{
				double reduction = availableSpace / (upperBound + 0.0001);
				for (dimension = 0; dimension < nrDimensions; ++dimension)
				{
					aUpperBounds[dimension] = aUpperBounds[dimension] * reduction;
				}
			}
		}
		super.setUpperBounds(aUpperBounds);
	}
}
