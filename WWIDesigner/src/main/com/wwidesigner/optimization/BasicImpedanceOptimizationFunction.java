/**
 * 
 */
package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;

/**
 * @author kort
 * 
 */
public class BasicImpedanceOptimizationFunction implements
		OptimizationFunctionInterface
{
	private InstrumentOptimizerInterface optimizer;
	private List<Fingering> fingeringTargets;
	protected int iterationsDone;

	public BasicImpedanceOptimizationFunction(
			InstrumentOptimizerInterface optimizer, TuningInterface tuning)
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
			Complex impedance = optimizer.getInstrumentCalculator().calcZ(
					target);
			// we need a way to display this error term during the optimization
			// (one per target)
			// as well as the error norm
			double error = Math.abs(impedance.getImaginary());// impedance.abs();
			norm += error * error;
		}
		return norm;
	}

	@Override
	public int getIterationsDone()
	{
		return iterationsDone;
	}

}
