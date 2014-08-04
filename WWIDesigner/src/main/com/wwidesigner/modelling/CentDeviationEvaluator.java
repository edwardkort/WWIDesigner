/**
 * 
 */
package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

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
	protected InstrumentTuner tuner;

	public CentDeviationEvaluator(InstrumentCalculator calculator)
	{
		this.calculator = calculator;
		setTuner(new SimpleInstrumentTuner());
	}

	public CentDeviationEvaluator(InstrumentCalculator calculator, InstrumentTuner tuner)
	{
		this.calculator = calculator;
		setTuner(tuner);
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

		Tuning targetTuning = new Tuning();
		targetTuning.setFingering(fingeringTargets);
		tuner.setTuning(targetTuning);

		int index = 0;
		for (Fingering targetFingering : fingeringTargets)
		{
			double centDeviation = 400.0;
			try
			{
				centDeviation = Note.cents(tuner.predictedFrequency(targetFingering),
						targetFingering.getNote().getFrequency());
			}
			catch (RuntimeException e)
			{
			}
			errorValues[index++] = centDeviation;
		}

		return errorValues;
	}

	protected void setTuner(InstrumentTuner tuner)
	{
		this.tuner = tuner;
		this.tuner.setCalculator(calculator);
		this.tuner.setInstrument(calculator.getInstrument());
		this.tuner.setParams(calculator.getPhysicalParameters());
	}

}
