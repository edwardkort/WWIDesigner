/**
 * Optimization objective function for hole positions and diameters, and
 * diameters of existing bore points at the bottom of the bore.
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

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters,
 * and bore diameters at existing bore points at the bottom of the bore:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing
 * from last hole to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>For bore points down to the bottom, ratio of diameters of this
 * bore point to prior bore point upward.</li>
 * </ul>
 * The diameter at upper bore points is left invariant.
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to control
 * the direction of taper.  If lower bound is 1.0, bore flares out toward bottom;
 * if upper bound is 1.0, bore tapers inward toward bottom.
 * 
 * @author Burton Patkau
 * 
 */
public class HoleAndBoreDiameterFromBottomObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole, plus bore diameter from bottom, optimizer";

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore diameters at existing bore points at bottom of bore.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints - Index of first bore point to optimize.
	 *        Leave diameter unchanged for this many bore points from the top of the bore.
	 */
	public HoleAndBoreDiameterFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, 
			int unchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, BoreLengthAdjustmentType.MOVE_BOTTOM);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new BoreDiameterFromBottomObjectiveFunction(aCalculator, tuning,
				aEvaluator, unchangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 50000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore diameters at existing bore points at bottom of bore.
	 * The lowest bore point left unchanged will be the highest bore point
	 * with a name that contains "Body".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public HoleAndBoreDiameterFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromBottomObjectiveFunction.getTopOfBody(aCalculator.getInstrument()) + 1);
	}
}
