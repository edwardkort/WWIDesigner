/**
 * Optimization objective function for bore diameters at existing bore points
 * at bottom of bore.
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
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.Termination;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for bore diameters at existing bore
 * points at the bottom of the bore.  The optimization dimensions are:
 * <ul>
 * <li>For bore points down to the bottom, ratio of diameters of this
 * bore point to prior bore point upward.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The diameters at bore points above these are left unchanged.
 * Bore point positions are invariant. <br>
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to
 * control the direction of taper.  If lower bound is 1.0, bore flares out
 * toward bottom; if upper bound is 1.0, bore tapers inward toward bottom. <br>
 * Do not use with other optimizers that might change the number of bore points.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreDiameterFromBottomObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore diameter ratios";
	public static final String DISPLAY_NAME = "Bore Diameter (from bottom) optimizer";
	// Index of first bore point affected by optimization.
	protected final int unchangedBorePoints;
	// Invariant: nrDimensions + unchangedBorePoints = number of bore points.

	/**
	 * Create an optimization objective function for bore diameters at existing
	 * bore points, from bottom.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints - Index of first bore point to optimize.
	 *        Leave diameter unchanged for this many bore points from the top of the bore.
	 */
	public BoreDiameterFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, int aUnchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		int nrBorePoints = aCalculator.getInstrument().getBorePoint().size();
		if (aUnchangedBorePoints >= nrBorePoints)
		{
			// At least one bore point is changed.
			this.unchangedBorePoints = nrBorePoints - 1;
		}
		else if (aUnchangedBorePoints >= 1)
		{
			this.unchangedBorePoints = aUnchangedBorePoints;
		}
		else
		{
			// At a minimum, top bore point is unchanged.
			this.unchangedBorePoints = 1;
		}
		nrDimensions = nrBorePoints - this.unchangedBorePoints;
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
	 * bore points, from the bottom of the bore.  The lowest bore point left
	 * unchanged will be the highest bore point with a name that contains "Body".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public BoreDiameterFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator, getTopOfBody(aCalculator.getInstrument()) + 1);
	}

	/**
	 * Find the index of the bore point at the top of the body. Look for
	 * the highest point named "Body", or the lowest point named "Head",
	 * or if that fails, the lowest bore point above the top tonehole, or
	 * the middle of the bore if there are no toneholes.
	 * 
	 * @param instrument
	 * @return index of highest bore point in the body of instrument,
	 *         estimated if necessary.
	 */
	public static int getTopOfBody(Instrument instrument)
	{
		// Look for point by name.
		List<BorePoint> borePoints = instrument.getBorePoint();
		if (borePoints.size() <= 2)
		{
			return 0;
		}
		int boreIdx = Instrument.positionIndex(borePoints, "Body", false, false);
		if (boreIdx >= 0)
		{
			return boreIdx;
		}
		boreIdx = Instrument.positionIndex(borePoints, "Head", false, true);
		if (boreIdx >= 0)
		{
			return boreIdx;
		}

		// Named point not found. Take a guess.

		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());
		PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
		double topHolePosition;
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
		int pointNr = borePointIdx(0);
		for (int dimension = 0; dimension < nrDimensions; ++dimension)
		{
			pointNr = borePointIdx(dimension);
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
	 * to a bore point index in unchangedBorePoints .. nrBorePoints-1.
	 */
	protected int borePointIdx(int dimensionIdx)
	{
		return unchangedBorePoints + dimensionIdx;
	}

	/**
	 * Point index used as an initial reference for the remaining points.
	 */
	protected int referencePointIdx()
	{
		return unchangedBorePoints - 1;
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[referencePointIdx()];
		double priorBoreDia = borePoint.getBoreDiameter();

		for (int dimension = 0; dimension < nrDimensions; ++dimension)
		{
			if (priorBoreDia < 0.000001)
			{
				priorBoreDia = 0.000001;
			}
			borePoint = (BorePoint) sortedPoints[borePointIdx(dimension)];
			geometry[dimension] = borePoint.getBoreDiameter() / priorBoreDia;
			priorBoreDia = borePoint.getBoreDiameter();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[referencePointIdx()];
		double priorBoreDia = borePoint.getBoreDiameter();
		borePoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double terminationDia = borePoint.getBoreDiameter();
		
		for (int dimension = 0; dimension < nrDimensions; ++dimension)
		{
			borePoint = (BorePoint) sortedPoints[borePointIdx(dimension)];
			borePoint.setBoreDiameter(point[dimension] * priorBoreDia);
			priorBoreDia = borePoint.getBoreDiameter();
		}

		double terminationChange = priorBoreDia - terminationDia;
		Termination termination = calculator.getInstrument().getTermination();
		if (termination != null)
		{
			// Change termination flange diameter as well, to preserve the flange width.
			termination.setFlangeDiameter(termination.getFlangeDiameter() + terminationChange);
		}
		calculator.getInstrument().updateComponents();
	}
}
