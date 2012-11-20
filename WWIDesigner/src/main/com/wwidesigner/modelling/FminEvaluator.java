package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;

/**
 * Class to evaluate how well a calculator predicts fmin for an instrument,
 * using Gain(fmin) == 1.
 * 
 * @author Burton Patkau
 */
public class FminEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;

	public FminEvaluator( InstrumentCalculator calculator )
	{
		this.calculator = calculator;
	}

	/**
	 * Return one less than the gain at the instrument's fmin.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return Gain(fmin) - 1.   length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() == null || target.getNote().getFrequencyMin() == null )
			{
				errorVector[i++] = 0.0;
			}
			else
			{
				errorVector[i++] = calculator.calcGain(target.getNote().getFrequencyMin(), target)
						- 1.0;
			}
		}
		return errorVector;
	}
}