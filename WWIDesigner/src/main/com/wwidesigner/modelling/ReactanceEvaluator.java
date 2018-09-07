package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;

/**
 * Class to evaluate how well a calculator predicts f for an instrument,
 * using Im(Z(f)) == 0.
 * 
 * @author Burton Patkau
 */
public class ReactanceEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;

	public ReactanceEvaluator( InstrumentCalculator aCalculator )
	{
		this.calculator = aCalculator;
	}

	/**
	 * Return the signed imaginary part of the impedance at
	 * the instrument's target frequency.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return -Im(Z(f)).   length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() == null || target.getNote().getFrequency() == null )
			{
				errorVector[i++] = 0.0;
			}
			else
			{
				// Return negative reactance, so sign is positive for sharp notes
				// and negative for flat notes.
				errorVector[i++] = - calculator.calcZ(target.getNote().getFrequency(), target)
						.getImaginary();
			}
		}
		return errorVector;
	}
}