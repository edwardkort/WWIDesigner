package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore length, hole positions without
 * groups, hole diameters, and a simple one-section taper. The foot diameter
 * remains invariant.
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class SingleTaperNoHoleGroupingObjectiveFunction extends
		MergedObjectiveFunction
{

	public SingleTaperNoHoleGroupingObjectiveFunction(
			InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator) throws Exception
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(calculator,
				tuning, evaluator);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		this.components[2] = new SingleTaperRatioObjectiveFunction(calculator,
				tuning, evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxIterations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints
				.setObjectiveDisplayName("Single taper, no-hole-grouping optimizer");
	}

}
