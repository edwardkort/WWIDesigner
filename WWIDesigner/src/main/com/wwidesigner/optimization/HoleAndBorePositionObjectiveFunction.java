package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters,
 * and positions of existing bore points:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing
 * from last hole to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>For each interior bore point between top and bottom,
 * distance from prior bore point to this bore point,
 * as a fraction of the distance from the prior bore point to the bottom.</li>
 * </ul>
 * The diameter at the top bore point is left invariant.
 * 
 * @author Burton Patkau
 * 
 */
public class HoleAndBorePositionObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole, plus bore-point position, optimizer";

	public HoleAndBorePositionObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator, 
			BoreLengthAdjustmentType lengthAdjustmentMode)
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(calculator,
				tuning, evaluator, lengthAdjustmentMode);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		this.components[2] = new BorePositionObjectiveFunction(calculator, tuning, evaluator, 1, true);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 30000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
