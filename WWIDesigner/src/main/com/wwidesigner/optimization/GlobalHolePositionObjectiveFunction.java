/**
 * Global optimization objective function for bore length and hole positions.
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
 * Optimization objective function for bore length and hole positions,
 * using DIRECT global optimizer.
 * 
 * @author Burton Patkau
 */
public class GlobalHolePositionObjectiveFunction extends HolePositionObjectiveFunction
{

	public GlobalHolePositionObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, 
			BoreLengthAdjustmentType aLengthAdjustmentMode)
	{
		super(aCalculator, tuning, aEvaluator, aLengthAdjustmentMode);
		optimizerType = OptimizerType.DIRECTOptimizer;
		maxEvaluations = 30000;
	}

	protected void setConstraints()
	{
		super.setConstraints();
		constraints.setObjectiveDisplayName("Hole position global optimizer");
	}
}
