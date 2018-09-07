/**
 * Evaluate an instrument based on raw data from an instrument tuner.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

	public WhistleEvaluator( WhistleCalculator aCalculator, InstrumentTuner aTuner )
	{
		this.calculator = aCalculator;
		this.tuner = aTuner;
		this.tuner.setCalculator(aCalculator);
		this.tuner.setInstrument(aCalculator.getInstrument());
		this.tuner.setParams(aCalculator.getPhysicalParameters());
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
			deviation = 0.0;
			if ( target.getNote() != null && target.getNote().getFrequency() != null )
			{
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
					double windowLength = calculator.getInstrument().getMouthpiece().getAirstreamLength();
					double targetVelocity = ((LinearVInstrumentTuner)tuner).getNominalV(f);
					double calcVelocity = LinearVInstrumentTuner.velocity(f, windowLength, 
							calculator.calcZ(f, target));
					deviation = 100.0*(targetVelocity/calcVelocity) - 100.0;
				}
				else
				{
					Double predicted = tuner.predictedFrequency(target);
					if (predicted == null )
					{
						deviation = 400.0;
					}
					else {
						deviation = Note.cents(f, predicted);
//							deviation = 100.0*(f/predicted) - 100.0;
					}
				}
			}
			errorVector[i++] = deviation; 
		}
		return errorVector;
	}
}