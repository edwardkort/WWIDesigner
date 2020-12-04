/**
 * Optimization objective function for the position of the bottom bore point.
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

import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for the position of the end bore point.
 * 
 * @author Burton
 * 
 */
public class LengthObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore length";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;
	public static final String DISPLAY_NAME = "Length optimizer";

	protected BoreLengthAdjuster boreLengthAdjuster;

	/**
	 * Optimization objective function for the position of the end bore point.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 *            - false to leave bore diameter unchanged, true to adjust bore
	 *            diameter to preserve bore taper.
	 */
	public LengthObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator,
			BoreLengthAdjustmentType aLengthAdjustmentMode)
	{
		super(aCalculator, tuning, aEvaluator);
		boreLengthAdjuster = new BoreLengthAdjuster(this, aLengthAdjustmentMode);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Bore length",
				CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];

		// Find the farthest bore point out, and return its position.

		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		BorePoint endPoint = boreList.get(0);

		for (BorePoint borePoint : boreList)
		{
			if (borePoint.getBorePosition() > endPoint.getBorePosition())
			{
				endPoint = borePoint;
			}
		}
		geometry[0] = endPoint.getBorePosition();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		setBore(point);
		

		calculator.getInstrument().updateComponents();
	}

	public void setBore(double[] point)
	{
		boreLengthAdjuster.setBore(point);
	}
}
