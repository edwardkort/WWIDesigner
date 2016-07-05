package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for optimization of an instrument's mouthpiece
 * alpha factor.
 * 
 * @author Burton Patkau
 * 
 */
public class ReedCalibratorObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Mouthpiece parameters";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONLESS;
	public static final String DISPLAY_NAME = "Reed calibrator";

	public ReedCalibratorObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 2;
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Alpha",
				CONSTR_TYPE));
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Beta",
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
		double[] geometry = new double[2];
		Mouthpiece mouthpiece = calculator.getInstrument().getMouthpiece(); 
		double alpha = 1.0;
		double beta = 0.1;
		if (mouthpiece.getSingleReed() != null)
		{
			alpha = mouthpiece.getSingleReed().getAlpha();
		}
		else if (mouthpiece.getDoubleReed() != null)
		{
			alpha = mouthpiece.getDoubleReed().getAlpha();
		}
		else if (mouthpiece.getLipReed() != null)
		{
			alpha = mouthpiece.getLipReed().getAlpha();
		}
		if (mouthpiece.getBeta() != null)
		{
			beta = mouthpiece.getBeta();
		}
		geometry[0] = alpha;
		geometry[1] = beta;
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		Mouthpiece mouthpiece = calculator.getInstrument().getMouthpiece(); 
		if (mouthpiece.getSingleReed() != null)
		{
			mouthpiece.getSingleReed().setAlpha(point[0]);
		}
		else if (mouthpiece.getDoubleReed() != null)
		{
			mouthpiece.getDoubleReed().setAlpha(point[0]);
		}
		else if (mouthpiece.getLipReed() != null)
		{
			mouthpiece.getLipReed().setAlpha(point[0]);
		}
		mouthpiece.setBeta(point[1]);
		calculator.getInstrument().updateComponents();
	}

}
