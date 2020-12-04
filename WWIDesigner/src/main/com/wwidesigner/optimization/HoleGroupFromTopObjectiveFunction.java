package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

public class HoleGroupFromTopObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Grouped-hole position & size";
	public static final String NAME = HoleGroupFromTopObjectiveFunction.class
			.getSimpleName();

	public HoleGroupFromTopObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator,
			int[][] holeGroups, BoreLengthAdjustmentType aLengthAdjustmentMode)
			throws Exception
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new HoleGroupPositionFromTopObjectiveFunction(
				aCalculator, tuning, aEvaluator, holeGroups,
				aLengthAdjustmentMode);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
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
