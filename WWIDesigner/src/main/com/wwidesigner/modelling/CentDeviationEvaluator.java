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
		for (Fingering target : fingeringTargets)
		{
			double centDeviation = 400.0;
			if ( target.getNote() != null && target.getNote().getFrequency() != null )
			{
				try
				{
					centDeviation = Note.cents(target.getNote().getFrequency(),
							tuner.predictedFrequency(target));
				}
				catch (RuntimeException e)
				{
				}
				errorValues[index++] = centDeviation;
			}
			else
			{
				// No target available for this fingering.
				// Don't include it in optimization.
				centDeviation = 0.0;
			}
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
