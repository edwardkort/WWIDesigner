package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for the length of the airstream
 * in a fipple or transverse flute.
 * 
 * @author Burton
 * 
 */
public class AirstreamLengthObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Mouthpiece window";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;
	public static final String DISPLAY_NAME = "Airstream Length calibrator";

	public AirstreamLengthObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Airstream length",
				CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];
		if (calculator.getInstrument().getMouthpiece().getFipple() != null)
		{
			geometry[0] = calculator.getInstrument().getMouthpiece().getFipple()
					.getWindowLength();
		}
		else if (calculator.getInstrument().getMouthpiece().getEmbouchureHole() != null)
		{
			geometry[0] = calculator.getInstrument().getMouthpiece().getEmbouchureHole()
					.getAirstreamLength();
		}
		else
		{
			geometry[0] = 0.0;
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		if (calculator.getInstrument().getMouthpiece().getFipple() != null)
		{
			calculator.getInstrument().getMouthpiece().getFipple()
				.setWindowLength(point[0]);
		}
		else if (calculator.getInstrument().getMouthpiece().getEmbouchureHole() != null)
		{
			calculator.getInstrument().getMouthpiece().getEmbouchureHole()
				.setAirstreamLength(point[0]);
		}
		calculator.getInstrument().updateComponents();
	}

}
