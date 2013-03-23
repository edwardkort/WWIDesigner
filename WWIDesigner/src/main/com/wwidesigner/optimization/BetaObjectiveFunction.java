package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for optimization of an instrument's beta
 * factor.
 * 
 * @author Burton Patkau
 * 
 */
public class BetaObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Mouthpiece beta";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONLESS;

	public BetaObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Beta",
				CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName("Beta factor optimizer");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];
		geometry[0] = calculator.getInstrument().getMouthpiece().getBeta();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		calculator.getInstrument().getMouthpiece().setBeta(point[0]);
		calculator.getInstrument().updateComponents();
	}

}
