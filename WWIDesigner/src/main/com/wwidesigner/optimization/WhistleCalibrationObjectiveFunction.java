/**
 * Optimization objective function to vary whistle calibration parameters,
 * window height and beta.
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

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function to vary whistle calibration parameters,
 * window height and beta.
 * 
 * @author Burton
 * 
 */
public class WhistleCalibrationObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Mouthpiece calibration";
	public static final String DISPLAY_NAME = "Whistle calibrator";

	public WhistleCalibrationObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		super(aCalculator, tuning, aEvaluator);
		nrDimensions = 2;
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Window height",
				ConstraintType.DIMENSIONAL));
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Beta",
				ConstraintType.DIMENSIONLESS));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[2];
		geometry[0] = calculator.getInstrument().getMouthpiece().getFipple()
				.getWindowHeight();
		geometry[1] = calculator.getInstrument().getMouthpiece().getBeta();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		calculator.getInstrument().getMouthpiece().getFipple()
				.setWindowHeight(point[0]);
		calculator.getInstrument().getMouthpiece().setBeta(point[1]);
		calculator.getInstrument().updateComponents();
	}

}
