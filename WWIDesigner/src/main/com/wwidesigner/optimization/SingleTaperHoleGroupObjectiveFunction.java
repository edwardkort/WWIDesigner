package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

/**
 * Optimization objective function for bore length, hole positions in groups,
 * hole diameters, and a simple one-section taper The foot diameter remains
 * invariant.
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class SingleTaperHoleGroupObjectiveFunction
		extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Single taper, grouped-hole optimizer";

	public SingleTaperHoleGroupObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int[][] holeGroups,
			BoreLengthAdjustmentType aLengthAdjustmentMode) throws Exception
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HoleGroupPositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, holeGroups, aLengthAdjustmentMode)
						.setAllowBoreSizeInterpolation(false);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		// this.components[2] = new
		// SingleTaperRatioObjectiveFunction(calculator,
		// tuning, evaluator);
		this.components[2] = new SingleTaperSimpleRatioObjectiveFunction(
				aCalculator, tuning, aEvaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxEvaluations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
