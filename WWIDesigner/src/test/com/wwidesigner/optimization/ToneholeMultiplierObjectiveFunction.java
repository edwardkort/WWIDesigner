package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

//import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
//import com.wwidesigner.geometry.calculation.SimpleHoleCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for the Hole Calculator's hole-size
 * multiplier (fudgeFactor).
 * 
 * @author Edward Kort
 *
 */

public class ToneholeMultiplierObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Tonehole adjustment";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONLESS;
	public static final String DISPLAY_NAME = "Hole multiplier";
	public static final String NAME = ToneholeMultiplierObjectiveFunction.class
			.getSimpleName();

	public ToneholeMultiplierObjectiveFunction(
			InstrumentCalculator aCcalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		super(aCcalculator, tuning, aEvaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(NAME);
		constraints.setConstraintsName("Default");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];
		geometry[0] = ((DefaultHoleCalculator) calculator.getHoleCalculator())
				.getHoleSizeMult();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		((DefaultHoleCalculator) calculator.getHoleCalculator())
				.setHoleSizeMult(point[0]);
	}

	protected void setConstraints()
	{
		constraints.addConstraint(
				new Constraint(CONSTR_CAT, "Hole multiplier", CONSTR_TYPE));
		constraints
				.setNumberOfHoles(calculator.getInstrument().getHole().size());
		constraints.setObjectiveDisplayName("Hole multiplier optimizer");
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
