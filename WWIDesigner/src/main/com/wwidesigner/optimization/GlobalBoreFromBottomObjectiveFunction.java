/**
 * Global optimization objective function for bore point diameter and position
 * from bottom of bore.
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
 * Optimization objective function for bore point diameter and position
 * from bottom of bore, using DIRECT global optimizer.
 * 
 * @author Burton Patkau
 */
public class GlobalBoreFromBottomObjectiveFunction extends BoreFromBottomObjectiveFunction
{

	/**
	 * Create an optimization objective function for bore point position and
	 * diameter at existing bore points at the bottom of the bore,
	 * for use with DIRECT global optimizer.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aUnchangedBorePoints - Index of first bore point to optimize.
	 *        Leave position and diameter unchanged for this many bore points
	 *        from the top of the bore.
	 */
	public GlobalBoreFromBottomObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, 
			int aUnchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator, aUnchangedBorePoints);
		optimizerType = OptimizerType.DIRECTOptimizer;
		maxEvaluations = 40000;
		constraints.setObjectiveDisplayName("Bore point, from bottom, global optimizer");
	}
}
