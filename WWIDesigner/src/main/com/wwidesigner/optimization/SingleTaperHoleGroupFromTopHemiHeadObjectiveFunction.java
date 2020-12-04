package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

/**
 * Optimization objective function for bore length, constrained position of top
 * hole relative to bore length, hole positions in groups, with holes equally
 * spaced within groups, hole diameters, and a simple one-section taper. The
 * foot diameter remains invariant. A hemispherical bore end is created and
 * maintained throughout the bore-profile changes. This version constrains the
 * top hole position.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Edward Kort
 */
public class SingleTaperHoleGroupFromTopHemiHeadObjectiveFunction
		extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Single taper, hemi-head, grouped hole";
	public static final String NAME = SingleTaperHoleGroupFromTopHemiHeadObjectiveFunction.class
			.getSimpleName();

	public SingleTaperHoleGroupFromTopHemiHeadObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int[][] holeGroups,
			BoreLengthAdjustmentType aLengthAdjustmentMode) throws Exception
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HoleGroupPositionFromTopObjectiveFunction(
				aCalculator, tuning, aEvaluator, holeGroups,
				aLengthAdjustmentMode).setAllowBoreSizeInterpolation(false);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new SingleTaperSimpleRatioHemiHeadObjectiveFunction(
				aCalculator, tuning, aEvaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxEvaluations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(NAME);
		constraints.setConstraintsName("Default");
	}

	@Override
	public double getInitialTrustRegionRadius(double[] initial)
	{
		initialTrustRegionRadius = 10.;
		return initialTrustRegionRadius;
	}

	@Override
	public double getStoppingTrustRegionRadius()
	{
		return 1.e-8;
	}

}
