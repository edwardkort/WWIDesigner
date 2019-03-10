/**
 * Optimization objective function for diameter and relative position
 * of existing bore points at bottom of bore.
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

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for diameter and relative position
 * of existing bore points at bottom of bore.
 * <ul>
 * <li>Absolute position of bottom bore point.</li>
 * <li>For interior bore points down to the bottom,
 * distance from prior bore point to this bore point,
 * as a fraction of the distance from the prior bore point to the bottom.</li>
 * <li>For bore points down to the bottom, ratio of diameters of this
 * bore point to prior bore point upward.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The diameters and positions of bore points above these are
 * left unchanged. <br>
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to control
 * the direction of taper.  If lower bound is 1.0, bore flares out toward bottom;
 * if upper bound is 1.0, bore tapers inward toward bottom.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreFromBottomObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME
			= "Bore point position and diameter, from bottom, optimizer";

	/**
	 * Create an optimization objective function for bore point position and
	 * diameter at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints - Index of first bore point to optimize.
	 *        Leave position and diameter unchanged for this many bore points
	 *        from the top of the bore.
	 */
	public BoreFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, 
			int aUnchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new BorePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, aUnchangedBorePoints, false);
		this.components[1] = new BoreDiameterFromBottomObjectiveFunction(aCalculator,
				tuning, aEvaluator, aUnchangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 40000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	/**
	 * Create an optimization objective function for bore point position and
	 * diameter at existing bore points.  The lowest bore point left unchanged
	 * will be the highest bore point with a name that contains "Body".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public BoreFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromBottomObjectiveFunction
						.getTopOfBody(aCalculator.getInstrument()) + 1);
	}

	@Override
	public double getStoppingTrustRegionRadius()
	{
		return 0.8e-6;
	}
}
