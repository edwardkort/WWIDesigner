package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Class to evaluate how well a calculator predicts f for an instrument,
 * using the difference between reactance at the target pitch,
 * and a nominal reactance from a linear interpolation.
 * 
 * @author Burton Patkau
 */
public class WhistleEvaluator implements EvaluatorInterface
{
	protected WhistleCalculator  calculator;
	protected InstrumentTuner  tuner;

	public WhistleEvaluator( WhistleCalculator calculator, int blowingLevel )
	{
		this.calculator = calculator;
		tuner = new LinearXInstrumentTuner(blowingLevel);
		tuner.setCalculator(calculator);
		tuner.setInstrument(calculator.getInstrument());
		tuner.setParams(calculator.getPhysicalParameters());
	}

	/**
	 * Calculate the signed difference in cents
	 * between target and predicted frequency for each fingering.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return difference between target and predicted frequency.
	 * 			length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];
		double centDeviation;
		
		Tuning targetTuning = new Tuning();
		targetTuning.setFingering(fingeringTargets);
		tuner.setTuning(targetTuning);

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() == null || target.getNote().getFrequency() == null )
			{
				errorVector[i++] = 100.0;
			}
			else
			{
				Note predictedNote = tuner.predictedNote(target);
				if (predictedNote == null || predictedNote.getFrequency() == null )
				{
					centDeviation = 100.0;
				}
				else {
					centDeviation = Note.cents(target.getNote().getFrequency(),
											   predictedNote.getFrequency());
				}
				errorVector[i++] = centDeviation; 
			}
		}
		return errorVector;
	}
}