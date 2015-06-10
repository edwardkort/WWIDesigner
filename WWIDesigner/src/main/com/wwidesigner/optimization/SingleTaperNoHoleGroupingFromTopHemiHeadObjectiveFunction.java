package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

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
			InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator) throws Exception
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionFromTopObjectiveFunction(
				calculator, tuning, evaluator)
				.setAllowBoreSizeInterpolation(false);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		this.components[2] = new SingleTaperSimpleRatioHemiHeadObjectiveFunction(
				calculator, tuning, evaluator);
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
