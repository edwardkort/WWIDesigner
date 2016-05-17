package com.wwidesigner.modelling;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;

/**
 * Class to evaluate how well a calculator predicts f for an instrument,
 * using the phase of the reflection coefficient == 0.
 * 
 * @author Burton Patkau
 */
public class ReflectionEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;

	public ReflectionEvaluator( InstrumentCalculator calculator )
	{
		this.calculator = calculator;
	}

	/**
	 * Return the signed phase angle of the complex reflection coefficient at
	 * the instrument's target frequency.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return phase of reflection coefficient at target frequency.
	 *         length = fingeringTargets.size().
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
				// Multiply reflectance by -1, so that reflectance of -1 has phase angle of zero.
				Complex reflectionCoeff = calculator.calcReflectionCoefficient(target).multiply(-1.0);
				errorVector[i++] = reflectionCoeff.getArgument();
			}
		}
		return errorVector;
	}
}