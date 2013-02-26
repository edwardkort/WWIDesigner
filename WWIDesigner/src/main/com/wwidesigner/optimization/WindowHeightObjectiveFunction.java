package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for an instrument's windowHeight.
 * 
 * @author Burton
 * 
 */
public class WindowHeightObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Mouthpiece window";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;

	public WindowHeightObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	private void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Window height",
				CONSTR_TYPE));
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];
		geometry[0] = calculator.getInstrument().getMouthpiece().getFipple()
				.getWindowHeight();
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
				.setWindowHeight(point[0]);
		calculator.getInstrument().updateComponents();
	}

}
