/**
 * Evaluate an instrument based on deviation between measured and predicted frequencies,
 * either min and max frequencies or nominal playing frequency.
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

import org.apache.commons.math3.util.FastMath;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Evaluates a calculator based on deviation from measured frequencies,
 * expressed in cents. Uses measured min and max frequencies if they are
 * available. For notes without min and max frequency, the nominal frequency
 * must be the actual measured playing frequency, not the target frequency.
 * 
 * @author kort
 * 
 */
public class FminmaxEvaluator implements EvaluatorInterface
{
	protected InstrumentCalculator calculator;
	protected InstrumentTuner tuner;
	// Weighting factors to apply to each class of frequencies.
	// Use playing frequency only if actual min and max are not available.
	protected static double FMAX_WEIGHT = 4.0;
	protected static double FMIN_WEIGHT = 1.0;
	protected static double FPLAYING_WEIGHT = 1.0;

	public FminmaxEvaluator(InstrumentCalculator aCalculator)
	{
		this.calculator = aCalculator;
		setTuner(new SimpleInstrumentTuner());
	}

	public FminmaxEvaluator(InstrumentCalculator aCalculator,
			InstrumentTuner aTuner)
	{
		this.calculator = aCalculator;
		setTuner(aTuner);
	}

	/**
	 * Return an array of <i>weighted</i> cent differences between predicted and
	 * actual frequencies.
	 * 
	 * @param fingeringActualData
	 *            - Fingerings, with target note for each.
	 * @return - array of weighted cent differences. length =
	 *         fingeringTargets.size().
	 * 
	 * @see com.wwidesigner.modelling.EvaluatorInterface#calculateErrorVector(java
	 *      .util.List)
	 */
	@Override
	public double[] calculateErrorVector(List<Fingering> fingeringActualData)
	{
		double[] errorValues = new double[fingeringActualData.size()];

		Tuning targetTuning = new Tuning();
		targetTuning.setFingering(fingeringActualData);
		tuner.setTuning(targetTuning);

		int index = 0;
		for (Fingering actual : fingeringActualData)
		{
			double centDeviation = 1200.0;
			if (actual.getNote() != null)
			{
				try
				{
					Note predicted = tuner.predictedNote(actual);
					if (actual.getNote().getFrequencyMax() != null)
					{
						centDeviation = FMAX_WEIGHT
								* Note.cents(
										actual.getNote().getFrequencyMax(),
										predicted.getFrequencyMax());
						if (actual.getNote().getFrequencyMin() != null)
						{
							double centDeviationMin = FMIN_WEIGHT
									* Note.cents(actual.getNote()
											.getFrequencyMin(), predicted
											.getFrequencyMin());
							centDeviation = FastMath.sqrt(centDeviation
									* centDeviation + centDeviationMin
									* centDeviationMin);
						}
					}
					else if (actual.getNote().getFrequencyMin() != null)
					{
						centDeviation = FMIN_WEIGHT
								* Note.cents(
										actual.getNote().getFrequencyMin(),
										predicted.getFrequencyMin());
					}
					else if (actual.getNote().getFrequency() != null)
					{
						centDeviation = FPLAYING_WEIGHT
								* Note.cents(actual.getNote().getFrequency(),
										predicted.getFrequency());
					}
					else
					{
						// No target available for this fingering.
						// Don't include it in optimization.
						centDeviation = 0.0;
					}
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

	protected void setTuner(InstrumentTuner aTuner)
	{
		this.tuner = aTuner;
		this.tuner.setCalculator(calculator);
		this.tuner.setInstrument(calculator.getInstrument());
		this.tuner.setParams(calculator.getPhysicalParameters());
	}

}
