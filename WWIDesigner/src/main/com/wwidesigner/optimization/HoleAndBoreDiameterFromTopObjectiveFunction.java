/**
 * Optimization objective function for hole positions and diameters, and
 * diameters of existing bore points at the top of the bore.
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
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters, and
 * positions of existing bore points:
 * <ul>
 * <li>Position of end of bore.</li>
 * <li>For each hole, spacing to next hole, ending with spacing from last hole
 * to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>For bore points from the top down, ratio of diameters at this bore point
 * to next bore point.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The diameters of bore points below these are left unchanged.
 * Bore point positions are unchanged.
 * 
 * @author Burton Patkau
 * 
 */
public class HoleAndBoreDiameterFromTopObjectiveFunction extends
		MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole, plus bore-point diameter from top, optimizer";

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point spacing at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aChangedBorePoints
	 *            - Number of bore points to optimize, from top.
	 */
	public HoleAndBoreDiameterFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int aChangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new BoreDiameterFromTopObjectiveFunction(aCalculator,
				tuning, aEvaluator, aChangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 50000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point spacing at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param pointName
	 *            - The lowest bore point moved will be the lowest
	 *            bore point with a name that contains pointName.
	 */
	public HoleAndBoreDiameterFromTopObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, String pointName)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction.getLowestPoint(
						aCalculator.getInstrument(), pointName));
	}

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point diameter at existing bore points
	 * from the top of the bore.  The highest bore point left unchanged
	 * will be the lowest bore point with a name that contains "Head".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public HoleAndBoreDiameterFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction.getLowestPoint(
						aCalculator.getInstrument(), "Head"));
	}
}
