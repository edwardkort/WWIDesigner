/**
 * 
 */
package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class BasicImpedanceOptimizationFunction implements
		OptimizationFunctionInterface
{
	private InstrumentOptimizerInterface optimizer;
	private List<Fingering> fingeringTargets;
	private PhysicalParameters physicalParams;

	public BasicImpedanceOptimizationFunction(
			InstrumentOptimizerInterface optimizer, TuningInterface tuning,
			PhysicalParameters physicalParameters)
	{
		this.optimizer = optimizer;
		fingeringTargets = tuning.getFingering();
		physicalParams = physicalParameters;
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
		System.out.println(error);
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
					target, physicalParams);
			// we need a way to display this error term during the optimization
			// (one per target)
			// as well as the error norm
			double error = Math.abs(impedance.getImaginary());//impedance.abs();
			norm += error;
		}
		return norm;
	}

}
