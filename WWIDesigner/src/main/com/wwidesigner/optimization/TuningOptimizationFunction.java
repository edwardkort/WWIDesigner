package com.wwidesigner.optimization;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.PhysicalParameters;

public class TuningOptimizationFunction implements
		OptimizationFunctionInterface
{
	private InstrumentOptimizerInterface optimizer;
	private List<Fingering> fingeringTargets;
	private PhysicalParameters physicalParams;

	public TuningOptimizationFunction(InstrumentOptimizerInterface optimizer,
			TuningInterface tuning, PhysicalParameters physicalParameters)
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
			Double freqDeviation = optimizer.getInstrumentCalculator()
					.getPlayedFrequency(target, 2., 500, physicalParams);
			if (freqDeviation == null)
			{
				norm += 1.;
			}
			else
			{
				double targetFreq = target.getNote().getFrequency();
				double dev = (targetFreq - freqDeviation) / targetFreq;
				norm += dev * dev;
			}
		}

		return norm;
	}

}
