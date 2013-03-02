package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole positions and diameters: - Position
 * of end of bore, - For each hole, spacing to next group, ending with spacing
 * from last hole to end of bore. - For each hole, ratio of hole diameter to
 * bore diameter.
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class HoleObjectiveFunction extends MergedObjectiveFunction
{

	public HoleObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new HolePositionObjectiveFunction(calculator,
				tuning, evaluator);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxIterations = 10000;
		sumDimensions();
		constraints.setObjectiveDisplayName("Hole position and size optimizer");
	}

}
