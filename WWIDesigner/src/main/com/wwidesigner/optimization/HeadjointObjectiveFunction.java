/**
 * Optimization objective function for headjoint length and bore diameters
 * at existing bore points at top of bore.
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

/**
 * Optimization objective function for headjoint length and bore diameters at
 * existing bore points:
 * <ul>
 * <li>Distance from topmost bore point to upper end of embouchure hole.</li>
 * <li>For bore points from the top down, ratio of diameters at this bore point
 * to next bore point.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The diameters of bore points below these are left unchanged.
 * Bore point positions are unchanged. <br>
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to
 * control the direction of taper. If lower bound is 1.0, bore flares out toward
 * top; if upper bound is 1.0, bore tapers inward toward top.
 * 
 * @author Burton Patkau
 * 
 */
public class HeadjointObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Headjoint length and profile optimizer";

	/**
	 * Create an optimization objective function for headjoint length
	 * and bore point diameter at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aChangedBorePoints
	 *            - Number of bore points to optimize, from top.
	 */
	public HeadjointObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator,
			int aChangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new StopperPositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, false);
		this.components[1] = new BoreDiameterFromTopObjectiveFunction(
				aCalculator, tuning, aEvaluator, aChangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 40000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	public HeadjointObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction
				.getLowestPoint(aCalculator.getInstrument(), "Head"));
	}
}
