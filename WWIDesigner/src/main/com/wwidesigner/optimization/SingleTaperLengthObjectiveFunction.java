package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for a
 * simple one-section taper The foot diameter remains invariant.
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class SingleTaperLengthObjectiveFunction extends MergedObjectiveFunction
{

	public SingleTaperLengthObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
			throws Exception
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new LengthObjectiveFunction(calculator, tuning,
				evaluator);
		this.components[1] = new SingleTaperRatioObjectiveFunction(calculator,
				tuning, evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxEvaluations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints.setObjectiveDisplayName("Single taper length optimizer");
	}

}
