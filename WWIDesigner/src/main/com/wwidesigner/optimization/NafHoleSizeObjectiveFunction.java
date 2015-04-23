package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

public class NafHoleSizeObjectiveFunction extends HoleSizeObjectiveFunction
{
	public static final String NAME = NafHoleSizeObjectiveFunction.class
			.getSimpleName();

	public NafHoleSizeObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		constraints.setObjectiveFunctionName(NAME);
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
