/**
 * Optimization objective function for bore diameters at existing bore points at
 * top of bore.
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
 * Optimization objective function for bore diameters at existing bore points at
 * top of bore. The optimization dimensions are:
 * <ul>
 * <li>For bore points from the top down, ratio of diameters at this bore point
 * to next bore point.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The diameters at bore points below these are left unchanged.
 * Bore point positions are unchanged. <br>
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to
 * control the direction of taper. If lower bound is 1.0, bore flares out toward
 * top; if upper bound is 1.0, bore tapers inward toward top. <br>
 * Do not use with other optimizers that might change the number of bore points.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreDiameterFromTopObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore diameter ratios";
	public static final String DISPLAY_NAME = "Bore Diameter (from top) optimizer";

	/**
	 * Create an optimization objective function for bore diameters at existing
	 * bore points, from the top of the bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aChangedBorePoints
	 *            - Number of bore points to optimize, from top.
	 */
	public BoreDiameterFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int aChangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		int nrBorePoints = aCalculator.getInstrument().getBorePoint().size();
		nrDimensions = aChangedBorePoints;
		if (nrDimensions >= nrBorePoints)
		{
			// At least the bottom bore point is left unchanged.
			nrDimensions = nrBorePoints - 1;
		}
		if (nrDimensions < 1)
		{
			// At least the top bore point is changed.
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
	 * Create an optimization objective function for bore diameters at existing
	 * bore points, from the top of the bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param pointName
	 *            - The highest bore point left unchanged will be the lowest
	 *            bore point with a name that contains pointName.
	 */
	public BoreDiameterFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, String pointName)
	{
		this(aCalculator, tuning, aEvaluator,
				getLowestPoint(aCalculator.getInstrument(), pointName));
	}

	/**
	 * Create an optimization objective function for bore diameters at existing
	 * bore points, from the top of the bore.  The highest bore point left
	 * unchanged will be the lowest bore point with a name that contains "Head".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public BoreDiameterFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				getLowestPoint(aCalculator.getInstrument(), "Head"));
	}

	/**
	 * Find the index of the bore point at the bottom of the headjoint. If
	 * necessary, take a guess: the lowest bore point above the top tonehole, or
	 * the middle of the bore if there are no toneholes.
	 * 
	 * @param instrument
	 * @param pointName - Name of bore point to search for, such as "Head".
	 * @return index of lowest bore point in the headjoint of instrument,
	 *         estimated if necessary.
	 */
	public static int getLowestPoint(Instrument instrument, String pointName)
	{
		// Look for the lowest bore point that mentions pointName.

		int boreIdx = Instrument.positionIndex(instrument.getBorePoint(),
				pointName, false, true);
		if (boreIdx >= 0)
		{
			return boreIdx;
		}

		// Named point not found. Take a guess.

		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		double topHolePosition;
		if (sortedPoints.length <= 2)
		{
			return 0;
		}
		if (sortedHoles.length > 0)
		{
			topHolePosition = sortedHoles[0].getBorePosition();
		}
		else
		{
			// No holes. Use mid-point of bore.
			topHolePosition = 0.5 * (sortedPoints[0].getBorePosition()
					+ sortedPoints[sortedPoints.length - 1].getBorePosition());
		}
		// Check internal bore points for lowest one above the top tonehole.
		// Assume that the bottom bore point is *not* in the headjoint,
		// and the top bore point *is*.
		for (boreIdx = sortedPoints.length - 2; boreIdx > 0; --boreIdx)
		{
			if (sortedPoints[boreIdx].getBorePosition() < topHolePosition)
			{
				return boreIdx;
			}
		}
		return 0;
	}

	protected void setConstraints()
	{
		String name;
		for (int dimension = 0; dimension < nrDimensions; ++dimension)
		{
			name = "Ratio of diameters, bore point "
					+ String.valueOf(dimension + 1) + " / bore point "
					+ String.valueOf(dimension + 2);
			constraints.addConstraint(new Constraint(CONSTR_CAT, name,
					ConstraintType.DIMENSIONLESS));
		}
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	/**
	 * Index of first point with static diameter, used as an initial reference
	 * for the remaining points.
	 */
	protected int referencePointIdx()
	{
		return nrDimensions;
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[referencePointIdx()];
		double nextBoreDia = borePoint.getBoreDiameter();

		for (int dimension = nrDimensions - 1; dimension >= 0; --dimension)
		{
			borePoint = (BorePoint) sortedPoints[dimension];
			if (nextBoreDia < 0.000001)
			{
				nextBoreDia = 0.000001;
			}
			geometry[dimension] = borePoint.getBoreDiameter() / nextBoreDia;
			nextBoreDia = borePoint.getBoreDiameter();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[referencePointIdx()];
		double nextBoreDia = borePoint.getBoreDiameter();
		for (int dimension = nrDimensions - 1; dimension >= 0; --dimension)
		{
			borePoint = (BorePoint) sortedPoints[dimension];
			borePoint.setBoreDiameter(point[dimension] * nextBoreDia);
			nextBoreDia = borePoint.getBoreDiameter();
		}
		calculator.getInstrument().updateComponents();
	}
}
