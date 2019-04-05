package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole positions and diameters:
 * <ul>
 * <li>Fipple factor,</li>
 * <li>Tonehole diameter multiplier.</li>
 * </ul>
 * 
 * @author Edward Kort
 * 
 */
public class FippleFactorHoleSizeMultObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Fipple factor & hole-size multiplier";
	public static final String NAME = HoleFromTopObjectiveFunction.class.getSimpleName();

	public FippleFactorHoleSizeMultObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new FippleFactorObjectiveFunction(
				aCalculator, tuning, aEvaluator);
		this.components[1] = new ToneholeMultiplierObjectiveFunction(aCalculator, tuning,
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
