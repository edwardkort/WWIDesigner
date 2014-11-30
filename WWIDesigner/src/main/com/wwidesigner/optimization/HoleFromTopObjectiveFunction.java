package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole positions and diameters:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing below the hole to the next hole, or (for the first
 * hole) to top of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * </ul>
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class HoleFromTopObjectiveFunction extends MergedObjectiveFunction
{

	public HoleFromTopObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new HolePositionFromTopObjectiveFunction(calculator,
				tuning, evaluator);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		sumDimensions();
		maxEvaluations = 20000 + (getNrDimensions() - 1) * 5000;
		constraints.setObjectiveDisplayName("Hole position and size optimizer");
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
