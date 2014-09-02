package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;

@Deprecated
public class BasicOptimizationFunction implements OptimizationFunctionInterface
{
	private InstrumentOptimizerInterface optimizer;
	private List<Fingering> fingeringTargets;
	protected int iterationsDone;

	public BasicOptimizationFunction(InstrumentOptimizerInterface optimizer,
			TuningInterface tuning)
	{
		this.optimizer = optimizer;
		fingeringTargets = tuning.getFingering();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.OptimizationFunctionInterface#value(double
	 * [])
	 */
	@Override
	public double value(double[] state_vector)
	{
		optimizer.updateGeometry(state_vector);
		double error = calculateErrorNorm();
		iterationsDone++;
		return error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.OptimizationFunctionInterface#calculateErrorNorm
	 * ()
	 */
	@Override
	public double calculateErrorNorm()
	{
		double norm = 0.;
		for (Fingering target : fingeringTargets)
		{
			Complex reflectionCoeff = optimizer.getInstrumentCalculator()
					.calcReflectionCoefficient(target);
			double reflectance_angle = reflectionCoeff.getArgument();
			// we need a way to display this error term during the optimization
			// (one per target)
			// as well as the error norm
			double error = reflectance_angle * reflectance_angle;
			norm += error;
		}
		return norm;
	}

	@Override
	public int getIterationsDone()
	{
		return iterationsDone;
	}

}
