package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters,
 * and a two-section bore with taper:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing
 * from last hole to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>Length of head section, as a fraction of total bore length.</li>
 * <li>Taper ratio foot diameter / middle diameter.</li>
 * </ul>
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class HoleAndTaperObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole and taper optimizer";

	public HoleAndTaperObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, BoreLengthAdjustmentType.MOVE_BOTTOM);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new BasicTaperObjectiveFunction(aCalculator, tuning, aEvaluator);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 20000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

}
