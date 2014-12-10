package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore length, hole positions in groups,
 * hole diameters, and a simple one-section taper The foot diameter remains
 * invariant.
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class SingleTaperHoleGroupObjectiveFunction extends
		MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Single taper, grouped-hole optimizer";

	public SingleTaperHoleGroupObjectiveFunction(
			InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator, int[][] holeGroups) throws Exception
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HoleGroupPositionObjectiveFunction(calculator,
				tuning, evaluator, holeGroups);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		this.components[2] = new SingleTaperRatioObjectiveFunction(calculator,
				tuning, evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxEvaluations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
