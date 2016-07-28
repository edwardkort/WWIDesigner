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
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for bore diameters at existing bore points.
 * The optimization dimensions are:
 * <ul>
 * <li>Bore diameter at the foot.</li>
 * <li>For each interior bore point, excluding top and bottom,
 * ratio of diameters at next bore point to this bore point.</li>
 * </ul>
 * The diameter at the top bore point is left invariant.
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to control
 * the direction of taper.  If lower bound is 1.0, bore flares out toward bottom;
 * if upper bound is 1.0, bore tapers inward toward bottom.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreDiameterObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore diameters";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;
	public static final String DISPLAY_NAME = "Bore Diameter optimizer";

	public BoreDiameterObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = calculator.getInstrument().getBorePoint().size() - 1;
		if (nrDimensions > 1)
		{
			optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		}
		else
		{
			optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		}
		setConstraints();
	}

	protected void setConstraints()
	{
		String name;
		name = "Diameter at bore point " + String.valueOf(nrDimensions + 1) + " (bottom)";
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				name, ConstraintType.DIMENSIONAL));
		for (int idx = nrDimensions - 1; idx > 0; idx--)
		{
			name = "Ratio of diameters, bore point " + String.valueOf(idx + 2)
					+ " / bore point " + String.valueOf(idx + 1);
			constraints.addConstraint(new Constraint(CONSTR_CAT,
					name, ConstraintType.DIMENSIONLESS));
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
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[nrDimensions];
		double nextBoreDia = borePoint.getBoreDiameter();
		geometry[0] = borePoint.getBoreDiameter();
		for (int idx = nrDimensions - 1; idx > 0; idx--)
		{
			borePoint = (BorePoint) sortedPoints[idx];
			if (borePoint.getBoreDiameter() >= 0.000001)
			{
				geometry[nrDimensions - idx] = nextBoreDia/borePoint.getBoreDiameter();
			}
			else
			{
				geometry[nrDimensions - idx] = nextBoreDia/0.000001;
			}
			nextBoreDia = borePoint.getBoreDiameter();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint borePoint = (BorePoint) sortedPoints[nrDimensions];
		borePoint.setBoreDiameter(point[0]);
		double nextBoreDia = borePoint.getBoreDiameter();
		for (int idx = nrDimensions - 1; idx > 0; idx--)
		{
			borePoint = (BorePoint) sortedPoints[idx];
			if (point[nrDimensions - idx] >= 0.000001)
			{
				borePoint.setBoreDiameter(nextBoreDia/point[nrDimensions - idx]);
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
