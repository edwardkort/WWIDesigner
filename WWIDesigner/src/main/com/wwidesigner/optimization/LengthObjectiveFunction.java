package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for the position of the end bore point.
 * 
 * @author Burton
 * 
 */
public class LengthObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Hole position";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONAL;

	public LengthObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1;
		optimizerType = OptimizerType.BrentOptimizer; // UnivariateOptimizer
		setConstraints();
	}

	private void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT, "Bore length",
				CONSTR_TYPE));
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[1];

		// Find the farthest bore point out, and return its position.

		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		BorePoint endPoint = boreList.get(0);

		for (BorePoint borePoint : boreList)
		{
			if (borePoint.getBorePosition() > endPoint.getBorePosition())
			{
				endPoint = borePoint;
			}
		}
		geometry[0] = endPoint.getBorePosition();
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		// Ensure that no bore points are beyond the new bottom position.
		// Find the farthest one out, and update its position.

		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		BorePoint endPoint = boreList.get(0);

		for (BorePoint borePoint : boreList)
		{
			if (borePoint.getBorePosition() > endPoint.getBorePosition())
			{
				endPoint = borePoint;
			}
			if (borePoint.getBorePosition() > point[0])
			{
				borePoint.setBorePosition(point[0]);
			}
		}
		endPoint.setBorePosition(point[0]);
		calculator.getInstrument().updateComponents();
	}

}
