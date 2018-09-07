package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;

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
	public static final String DISPLAY_NAME = "Hole size & position";
	public static final String NAME = HoleFromTopObjectiveFunction.class.getSimpleName();

	public HoleFromTopObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, BoreLengthAdjustmentType lengthAdjustmentMode)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new HolePositionFromTopObjectiveFunction(
				aCalculator, tuning, aEvaluator, lengthAdjustmentMode);
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
