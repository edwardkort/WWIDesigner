package com.wwidesigner.optimization;

import java.util.List;

import com.wwidesigner.modelling.PlayingRange;
import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;

public class TuningOptimizationFunction implements
		OptimizationFunctionInterface
{
	private InstrumentOptimizerInterface optimizer;
	private List<Fingering> fingeringTargets;
	protected int iterationsDone;
	protected PlayingRange range;

	public TuningOptimizationFunction(InstrumentOptimizerInterface optimizer,
			TuningInterface tuning)
	{
		this.optimizer = optimizer;
		fingeringTargets = tuning.getFingering();
		range = new PlayingRange(optimizer.getInstrumentCalculator());
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
			optimizer.getInstrumentCalculator().setFingering(target);
			try
			{
				double targetFreq = target.getNote().getFrequency();
				double freqDeviation = range.findXZero(targetFreq);
				double dev = (targetFreq - freqDeviation) / targetFreq;
				norm += dev * dev;
			}
			catch (NoPlayingRange e)
			{
				norm += 1.;
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
