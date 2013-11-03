package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore length, hole positions, in groups,
 * and hole diameters.
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class HoleGroupObjectiveFunction extends MergedObjectiveFunction
{

	public HoleGroupObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator,
			int[][] holeGroups) throws Exception
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new HoleGroupPositionObjectiveFunction(calculator,
				tuning, evaluator, holeGroups);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxEvaluations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints
				.setObjectiveDisplayName("Grouped hole-position and hole size optimizer");
	}

}
