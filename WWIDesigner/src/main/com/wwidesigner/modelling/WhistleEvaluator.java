package com.wwidesigner.modelling;

import java.util.List;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Class to evaluate how well a calculator predicts f for an instrument,
 * using the raw data from a specialized instrument tuner.
 * 
 * @author Burton Patkau
 */
public class WhistleEvaluator implements EvaluatorInterface
{
	protected WhistleCalculator  calculator;
	protected InstrumentTuner  tuner;

	public WhistleEvaluator( WhistleCalculator calculator, InstrumentTuner tuner )
	{
		this.calculator = calculator;
		this.tuner = tuner;
		this.tuner.setCalculator(calculator);
		this.tuner.setInstrument(calculator.getInstrument());
		this.tuner.setParams(calculator.getPhysicalParameters());
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
		double deviation;

		Tuning targetTuning = new Tuning();
		targetTuning.setFingering(fingeringTargets);
		tuner.setTuning(targetTuning);

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			deviation = 400.0;
			if ( target.getNote() != null && target.getNote().getFrequency() != null )
			{
				Double predicted = tuner.predictedFrequency(target);
				if (predicted == null )
				{
					deviation = 400.0;
				}
				else {
					double f = target.getNote().getFrequency();
					if (tuner instanceof LinearXInstrumentTuner)
					{
						// Reactances will be negative; subtract actual from target,
						// so sign is positive for sharp notes and negative for flat notes.
						double targetReactance = ((LinearXInstrumentTuner)tuner).getNominalX(f);
						double calcReactance = calculator.calcZ(f, target).getImaginary();
						deviation = (targetReactance - calcReactance)/f;
					}
					else if (tuner instanceof LinearVInstrumentTuner)
					{
						// For velocity deviation, use percentage difference.
						double windowLength = calculator.getInstrument().getMouthpiece().getFipple().getWindowLength();
						double targetVelocity = ((LinearVInstrumentTuner)tuner).getNominalV(f);
						double calcVelocity = LinearVInstrumentTuner.velocity(f, windowLength, 
								calculator.calcZ(f, target));
						deviation = 100.0*(targetVelocity/calcVelocity) - 100.0;
					}
					else
					{
						deviation = Note.cents(f, predicted);
//						deviation = 100.0*(f/predicted) - 100.0;
					}
				}
			}
			errorVector[i++] = deviation; 
		}
		return errorVector;
	}
}