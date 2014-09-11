/**
 * Evaluate an instrument based on deviation between target and predicted playing frequency,
 * with an additional penalty if target frequency is outside of the predicted playing range.
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
 * Class to evaluate an instrument based on deviation of predicted
 * frequency from target, with an additional penalty if the target
 * is outside the bounds of the playing range.
 * 
 * @author Burton Patkau
 */
public class RangeEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator  calculator;
	protected InstrumentTuner tuner;
	
	// Multiplier imposed on deviation of target outside the bounds of a playing range.
	protected double penaltyFactor;
	protected static final double DEFAULT_PENALTY_FACTOR = 4.0;
	
	// Frequency factor to start applying the penalty slightly inside the bounds.
	protected static final double BOUNDS_FACTOR = 1.0023;	// About 4 cents inside.

	/**
	 * Create an evaluator with specified calculator, and default tuner and penalty factor.
	 * @param calculator
	 */
	public RangeEvaluator( InstrumentCalculator calculator )
	{
		this.calculator = calculator;
		setTuner(new LinearVInstrumentTuner());
		this.penaltyFactor = DEFAULT_PENALTY_FACTOR;
	}

	/**
	 * Create an evaluator with specified calculator and tuner,
	 * and default penalty factor.
	 * @param calculator
	 * @param tuner
	 */
	public RangeEvaluator(InstrumentCalculator calculator, InstrumentTuner tuner)
	{
		this.calculator = calculator;
		setTuner(tuner);
		this.penaltyFactor = DEFAULT_PENALTY_FACTOR;
	}

	/**
	 * Create an evaluator with specified parameters.
	 * @param calculator
	 * @param tuner
	 * @param penaltyFactor
	 */
	public RangeEvaluator(InstrumentCalculator calculator, InstrumentTuner tuner, double penaltyFactor)
	{
		this.calculator = calculator;
		setTuner(tuner);
		this.penaltyFactor = penaltyFactor;
	}

	/**
	 * Return an array of cent differences between predicted and actual fmax.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return - array of cent differences.   length = fingeringTargets.size().
	 */

	@Override
	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];

		Tuning targetTuning = new Tuning();
		targetTuning.setFingering(fingeringTargets);
		tuner.setTuning(targetTuning);

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			double centDeviation = 1200.0;
			if ( target.getNote() != null && target.getNote().getFrequency() != null )
			{
				double targetFreq = target.getNote().getFrequency();
				try
				{
					Note predicted = tuner.predictedNote(target);
					if (predicted.getFrequencyMax() != null 
							&& targetFreq > predicted.getFrequencyMax()/BOUNDS_FACTOR)
					{
						// Target is above frequency range.
						// Apply additional penalty for distance above the range.
						double upperBound = predicted.getFrequencyMax()/BOUNDS_FACTOR;
						centDeviation = penaltyFactor * Note.cents(targetFreq, upperBound)
								+ Note.cents(upperBound, predicted.getFrequency());
					}
					else if (predicted.getFrequencyMin() != null 
							&& targetFreq < predicted.getFrequencyMin()*BOUNDS_FACTOR)
					{
						// Target is below frequency range.
						// Apply additional penalty for distance below the range.
						double lowerBound = predicted.getFrequencyMin()*BOUNDS_FACTOR;
						centDeviation = penaltyFactor * Note.cents(targetFreq, lowerBound)
								+ Note.cents(lowerBound, predicted.getFrequency());
					}
					else
					{
						// No bounds, or target is within the bounds.
						centDeviation = Note.cents(targetFreq, predicted.getFrequency());
					}
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