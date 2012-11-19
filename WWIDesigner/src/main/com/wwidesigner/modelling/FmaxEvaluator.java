package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;

/**
 * Class to evaluate how well a calculator predicts fmax for an instrument,
 * using Im(Z(fmax)) == 0.
 * 
 * @author Burton Patkau
 */
public class FmaxEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;

	public FmaxEvaluator( InstrumentCalculator calculator )
	{
		this.calculator = calculator;
	}

	/**
	 * Return the signed reactance at the instrument's fmax.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return - Im(Z(fmax)).   length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() == null || target.getNote().getFrequencyMax() == null )
			{
				errorVector[i++] = 0.0;
			}
			else
			{
				// Return negative reactance, so sign is positive for sharp notes
				// and negative for flat notes.
				errorVector[i++] = - calculator.calcZ(target.getNote().getFrequencyMax(), target)
						.getImaginary();
			}
		}
		return errorVector;
	}
}