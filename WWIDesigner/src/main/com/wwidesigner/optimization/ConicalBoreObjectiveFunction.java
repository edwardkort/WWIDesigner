/**
 * Optimization objective function for a simple conical bore
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
 * Optimization objective function for a simple conical bore.
 * The single optimization dimension is the diameter at the foot.
 * All bore positions are invariant.
 * At present, this function varies only the geometry of the bottom bore section;
 * the diameters at all other bore points are left invariant.
 * 
 * @author Burton Patkau
 * 
 */
public class ConicalBoreObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Bore size";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;
	public static final String DISPLAY_NAME = "Conical bore optimizer";
	public static final double AFFECTED_BORE_FRACTION = 0.5;

	public ConicalBoreObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Foot diameter", CONSTR_TYPE));
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
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		geometry[0] = bottomPoint.getBoreDiameter();

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double topPosition = sortedPoints[0].getBorePosition();
		double totalLength = bottomPoint.getBorePosition() - topPosition;
		// Change the diameter of the lowest part of the bore, proportional
		// to the change at the foot.
		double fractionalChange = point[0]/bottomPoint.getBoreDiameter();
		for (PositionInterface borePoint : sortedPoints)
		{
			double fractionalPosition = (borePoint.getBorePosition() - topPosition)/totalLength;
			if (fractionalPosition >= AFFECTED_BORE_FRACTION)
			{
				BorePoint thisPoint = (BorePoint) borePoint;
				thisPoint.setBoreDiameter(thisPoint.getBoreDiameter()
						* fractionalChange);
			}
		}
		bottomPoint.setBoreDiameter(point[0]);
		calculator.getInstrument().updateComponents();
	}

}
