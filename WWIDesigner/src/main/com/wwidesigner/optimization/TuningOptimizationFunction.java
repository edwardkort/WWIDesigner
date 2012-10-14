package com.wwidesigner.optimization;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;

public class TuningOptimizationFunction implements
		OptimizationFunctionInterface
{
	private InstrumentOptimizerInterface optimizer;
	private List<Fingering> fingeringTargets;
	protected int iterationsDone;

	public TuningOptimizationFunction(InstrumentOptimizerInterface optimizer,
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
			Double freqDeviation = optimizer.getInstrumentCalculator()
					.getPlayedFrequency(target, 2., 500);
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

	@Override
	public int getIterationsDone()
	{
		return iterationsDone;
	}

}
