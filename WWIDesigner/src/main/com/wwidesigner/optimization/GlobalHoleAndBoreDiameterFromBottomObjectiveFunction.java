/**
 * Global optimization objective function for holes and bore point diameters,
 * from the bottom of the bore.
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
 * Optimization objective function for hole position and diameter, and bore
 * point diameter from bottom of bore, using DIRECT global optimizer.
 * 
 * @author Burton Patkau
 */
public class GlobalHoleAndBoreDiameterFromBottomObjectiveFunction extends
		HoleAndBoreDiameterFromBottomObjectiveFunction
{
	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point diameters at existing bore points from the
	 * bottom of the bore, for use with DIRECT global optimizer.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints
	 *            - Index of first bore point to optimize. Leave diameter
	 *            unchanged for this many bore points from the top of the bore.
	 */
	public GlobalHoleAndBoreDiameterFromBottomObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int unchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator, unchangedBorePoints);
		optimizerType = OptimizerType.DIRECTOptimizer;
		maxEvaluations = 60000;
		constraints
				.setObjectiveDisplayName("Hole, plus bore diameter from bottom, global optimizer");
	}

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point diameter at existing bore points, for use with
	 * DIRECT global optimizer. The lowest bore point left unchanged will be the
	 * highest bore point with a name that contains "Body".
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public GlobalHoleAndBoreDiameterFromBottomObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		super(aCalculator, tuning, aEvaluator);
		optimizerType = OptimizerType.DIRECTOptimizer;
		maxEvaluations = 60000;
		constraints
				.setObjectiveDisplayName("Hole, plus bore diameter from bottom, global optimizer");
	}
}
