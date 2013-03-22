package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for an instrument's fipple factor.
 * 
 * @author Burton Patkau
 * 
 */
public class FippleFactorObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Mouthpiece fipple";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONLESS;

	public FippleFactorObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.CMAESOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];
		geometry[0] = calculator.getInstrument().getMouthpiece().getFipple()
				.getFippleFactor();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		calculator.getInstrument().getMouthpiece().getFipple()
				.setFippleFactor(point[0]);
		calculator.getInstrument().updateComponents();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Fipple factor",
				CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName("Fipple factor optimizer");
	}

}
