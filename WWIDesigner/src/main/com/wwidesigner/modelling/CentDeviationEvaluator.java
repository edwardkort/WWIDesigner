/**
 * Evaluate an instrument based on deviation between target and predicted playing frequency.
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
