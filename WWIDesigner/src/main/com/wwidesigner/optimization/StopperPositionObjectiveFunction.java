/**
 * Optimization objective function for the position of the flute stopper.
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

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;
import com.wwidesigner.util.SortedPositionList;

/**
 * Optimization objective function for position of the flute stopper, the
 * overall headjoint length.
 * <ul>
 * <li>Distance from topmost bore point to upper end of embouchure hole.</li>
 * </ul>
 * 
 * @author Burton Patkau
 * 
 */
public class StopperPositionObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Stopper distance";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;

	public enum BoreLengthAdjustmentType
	{
		/**
		 * Change the position of the bottom bore point, adjusting the diameter
		 * to keep the taper angle unchanged.
		 */
		PRESERVE_TAPER,
		/**
		 * Change position of the bottom bore point, leaving bore diameters
		 * unchanged.
		 */
		MOVE_BOTTOM
	}

	protected BoreLengthAdjustmentType lengthAdjustmentMode;
	protected static final double MINIMUM_BORE_POINT_SPACING = 0.00001d;

	public StopperPositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator,
			BoreLengthAdjustmentType aLengthAdjustmentMode)
	{
		super(aCalculator, tuning, aEvaluator);
		this.lengthAdjustmentMode = aLengthAdjustmentMode;
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer;
		setConstraints();
	}

	/**
	 * @return The position of the top bore point.
	 */
	protected double getTopOfBore()
	{
		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		double topPosition = boreList.get(0).getBorePosition();

		for (BorePoint borePoint : boreList)
		{
			if (borePoint.getBorePosition() < topPosition)
			{
				topPosition = borePoint.getBorePosition();
			}
		}
		return topPosition;
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];

		Mouthpiece mouthpiece = calculator.getInstrument().getMouthpiece();
		geometry[0] = mouthpiece.getPosition() - getTopOfBore();
		if (mouthpiece.getEmbouchureHole() != null)
		{
			geometry[0] -= 0.5 * mouthpiece.getEmbouchureHole().getLength();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		Mouthpiece mouthpiece = calculator.getInstrument().getMouthpiece();
		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				calculator.getInstrument().getBorePoint());
		double newTopPosition = mouthpiece.getPosition() - point[0];
		if (mouthpiece.getEmbouchureHole() != null)
		{
			newTopPosition -= 0.5 * mouthpiece.getEmbouchureHole().getLength();
		}

		if (lengthAdjustmentMode == BoreLengthAdjustmentType.PRESERVE_TAPER)
		{
			// Extrapolate/interpolate the bore diameter of end point
			double topDiameter = BorePoint
					.getInterpolatedExtrapolatedBoreDiameter(boreList,
							newTopPosition);
			boreList.get(0).setBoreDiameter(topDiameter);
		}
		boreList.get(0).setBorePosition(newTopPosition);

		// Don't let optimizer delete or re-arrange borePoints.
		// Instead, move them down the bore a bit.
		for (int i = 1; i < boreList.size(); ++i)
		{
			BorePoint borePoint = boreList.get(i);
			double currentPosition = borePoint.getBorePosition();
			if (currentPosition <= newTopPosition)
			{
				newTopPosition += MINIMUM_BORE_POINT_SPACING;
				if (lengthAdjustmentMode == BoreLengthAdjustmentType.PRESERVE_TAPER)
				{
					// Extrapolate/interpolate the bore diameter
					double diameter = BorePoint
							.getInterpolatedExtrapolatedBoreDiameter(boreList,
									newTopPosition);
					borePoint.setBoreDiameter(diameter);
				}
				borePoint.setBorePosition(newTopPosition);
			}
			else
			{
				break;
			}
		}

		calculator.getInstrument().updateComponents();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Stopper Distance", CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName("Stopper position optimizer");
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
