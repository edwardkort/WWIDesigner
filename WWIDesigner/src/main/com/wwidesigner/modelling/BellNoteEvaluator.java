package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;

/**
 * Class to evaluate the tuning of the lowest note, with all holes closed.
 * 
 * @author Burton Patkau
 */
public class BellNoteEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;
	
	// Aim for fmax slightly greater than nominal frequency:
	// fmax = FmaxRatio * fnom.
	protected static final double FmaxRatio = 1.001;

	public BellNoteEvaluator( InstrumentCalculator aCalculator )
	{
		this.calculator = aCalculator;
	}
	
	protected static boolean allHolesClosed(Fingering fingering)
	{
		for (boolean isOpen: fingering.getOpenHole() )
		{
			if (isOpen)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Return the signed reactance at the target fmax,
	 * for notes with all holes closed.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return Im(Z(FmaxRatio * fnom)).   length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( ! allHolesClosed( target )
				|| target.getNote() == null 
				|| target.getNote().getFrequency() == null )
			{
				errorVector[i++] = 0.0;
			}
			else
			{
				double fmax = FmaxRatio * target.getNote().getFrequency();
				errorVector[i++] = calculator.calcZ(fmax, target).getImaginary();
			}
		}
		return errorVector;
	}
}