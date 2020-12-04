package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

/**
 * Optimization objective function for bore length, hole positions without
 * groups, hole diameters, and a simple one-section taper. The foot diameter
 * remains invariant. A hemispherical bore end is created and maintained
 * throughout the bore-profile changes. This version constrains the top hole
 * position.
 * 
 * @author Edward Kort
 *
 */
public class SingleTaperNoHoleGroupingFromTopHemiHeadObjectiveFunction extends
		MergedObjectiveFunction
{

	public static final String DISPLAY_NAME = "Single taper, hemi-head, no hole grouping";
	public static final String NAME = SingleTaperNoHoleGroupingFromTopHemiHeadObjectiveFunction.class
			.getSimpleName();

	public SingleTaperNoHoleGroupingFromTopHemiHeadObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator) throws Exception
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionFromTopObjectiveFunction(
				aCalculator, tuning, aEvaluator, BoreLengthAdjustmentType.MOVE_BOTTOM);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new SingleTaperSimpleRatioHemiHeadObjectiveFunction(
				aCalculator, tuning, aEvaluator);
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
