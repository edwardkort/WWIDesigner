/**
 * 
 */
package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;

/**
 * Evaluates a calculator based on deviation from expected tuning frequencies,
 * expressed in cents.
 * 
 * @author kort
 * 
 */
public class CentDeviationEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator calculator;

	public CentDeviationEvaluator(InstrumentCalculator calculator)
	{
		this.calculator = calculator;
	}

	/**
	 * Returns an array of cents deviation for each fingering target
	 * 
	 * @see com.wwidesigner.modelling.EvaluatorInterface#calculateErrorVector(java
	 *      .util.List)
	 */
	@Override
	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorValues = new double[fingeringTargets.size()];
		InstrumentTuner tuner = configureTuner();

		int index = 0;
		for (Fingering targetFingering : fingeringTargets)
		{
			double centDeviation = Double.MAX_VALUE;
			try
			{
				Note predictedNote = tuner.predictedNote(targetFingering);
				Note targetNote = targetFingering.getNote();
				centDeviation = Note.cents(predictedNote.getFrequency(),
						targetNote.getFrequency());
			}
			catch (RuntimeException e)
			{
			}
			errorValues[index++] = centDeviation;
		}

		return errorValues;
	}

	protected InstrumentTuner configureTuner()
	{
		InstrumentTuner tuner = new SimpleInstrumentTuner();
		tuner.setCalculator(calculator);
		tuner.setInstrument(calculator.getInstrument());
		tuner.setParams(calculator.getPhysicalParameters());

		return tuner;
	}

}
