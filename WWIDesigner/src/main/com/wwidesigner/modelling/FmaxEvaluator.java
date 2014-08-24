package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;

/**
 * Class to evaluate how well a calculator predicts fmax for an instrument
 * based on deviation in cents.
 * 
 * @author Burton Patkau
 */
public class FmaxEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;
	protected InstrumentTuner tuner;

	public FmaxEvaluator( InstrumentCalculator calculator )
	{
		this.calculator = calculator;
		setTuner(new LinearVInstrumentTuner());
	}

	public FmaxEvaluator(InstrumentCalculator calculator, InstrumentTuner tuner)
	{
		this.calculator = calculator;
		setTuner(tuner);
	}

	/**
	 * Return an array of cent differences between predicted and actual fmax.
	 * @param fingeringActualData  - Fingerings, with target note for each.
	 * @return - array of cent differences.   length = fingeringTargets.size().
	 */

	@Override
	public double[] calculateErrorVector(List<Fingering> fingeringActualData)
	{
		double[] errorVector = new double[fingeringActualData.size()];

		int i = 0;
		for (Fingering actual: fingeringActualData)
		{
			double centDeviation = 400.0;
			if ( actual.getNote() != null && actual.getNote().getFrequencyMax() != null )
			{
				try
				{
					Note predicted = tuner.predictedNote(actual);
					centDeviation = Note.cents(actual.getNote().getFrequencyMax(),
							predicted.getFrequencyMax());
				}
				catch (RuntimeException e)
				{
				}
			}
			else
			{
				// No actual available for this fingering.
				// Don't include it in optimization.
				centDeviation = 0.0;
			}
			errorVector[i++] = centDeviation;
		}
		return errorVector;
	}

	protected void setTuner(InstrumentTuner tuner)
	{
		this.tuner = tuner;
		this.tuner.setCalculator(calculator);
		this.tuner.setInstrument(calculator.getInstrument());
		this.tuner.setParams(calculator.getPhysicalParameters());
	}
}