package com.wwidesigner.modelling;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;

/**
 * Class to record the nominal playing pattern of an instrument
 * (how the player would expect to play each note),
 * and quantify the departure from the nominal pattern
 * (what it actually takes to play the instrument at target pitches).
 *
 * For the nominal playing pattern, we use a linear change in
 * reactance from just below fmax for the lowest note,
 * to somewhat below fmax for the highest note.  
 * This is only *one* possible playing pattern, and has not yet been
 * validated against the playing of real players.
 * 
 * @author Burton Patkau
 */
public class WhistleEvaluator implements EvaluatorInterface
{
	protected WhistleCalculator  calculator;

	// Target reactance of lowest note is BottomFraction of its value at fmin.
	// Target reactance of highest note is TopFraction of its value at fmin.
	protected double BottomFraction = 0.02;
	protected double TopFraction    = 0.35;

	public WhistleEvaluator( WhistleCalculator calculator )
	{
		this.calculator = calculator;
		BottomFraction = 0.02;
		TopFraction    = 0.35;
	}

	public WhistleEvaluator( WhistleCalculator calculator, double bottomFr, double topFr )
	{
		this.calculator = calculator;
		BottomFraction = bottomFr;
		TopFraction    = topFr;
	}

	/**
	 * Calculate the signed percentage difference
	 * between nominal reactance for each fingering,
	 * and the predicted reactance for that fingering.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return percentage difference between target and predicted velocities.
	 * 			length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];
		
		// Get lowest and highest target notes, and estimate a target velocity for each.

		// Target frequencies for lowest and highest note,
		// then the nominal frequency for these notes, used in
		// linear interpolation of reactance.
		double fLow  = 100000.0;
		double fHigh = 0.0;
		// Target reactance for lowest and highest notes.
		double imagLow  = 0.0;
		double imagHigh = 0.0;

		// Find lowest and highest target notes.

		Fingering noteLow = new Fingering();
		noteLow.setNote(new Note());
		Fingering noteHigh = new Fingering();
		noteHigh.setNote(new Note());
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() != null
					&& target.getNote().getFrequency() != null )
			{
				if ( target.getNote().getFrequency() < fLow )
				{
					fLow = target.getNote().getFrequency();
					noteLow.setOpenHole(target.getOpenHole());
				}
				if ( target.getNote().getFrequency() > fHigh )
				{
					fHigh = target.getNote().getFrequency();
					noteHigh.setOpenHole(target.getOpenHole());
				}
			}
		}
		
		noteLow.getNote().setFrequency(fLow);
		noteHigh.getNote().setFrequency(fHigh);
		
		// Locate playing ranges at fLow and fHigh,
		// and calculate nominal reactance at these frequencies from Im(Z(fmin)).
		// This nominal reactance is actually an interpolation between 
		// Im(Z(fmin)) and Im(Z(fmax)), assuming that Im(Z(fmax)) is always zero.
		
		double fmax, fmin;
		Complex z;

		calculator.setFingering(noteLow);
		PlayingRange range = new PlayingRange(calculator);
		fmax = range.findFmax(fLow);
		fmin = range.findFmin(fmax);
		z = calculator.calcZ(fmin);
		imagLow = BottomFraction * z.getImaginary();
		fLow = fmax;	// Nominal frequency for our interpolation.
		
		calculator.setFingering(noteHigh);
		fmax = range.findFmax(fHigh);
		fmin = range.findFmin(fmax);
		z = calculator.calcZ(fmin);
		imagHigh = TopFraction * z.getImaginary();
		fHigh = fmax;	// Nominal frequency for our interpolation.
		
		// Nominal reactance is a linear interpolation between (fLow,imagLow) and (fHigh,imagHigh),
		// imagNom = slope * frequency + intercept.
		double slope = (imagHigh - imagLow)/(fHigh - fLow);
		double intercept = imagLow - slope * fLow;

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() == null || target.getNote().getFrequency() == null )
			{
				errorVector[i++] = 0.0;
			}
			else
			{
				double f = target.getNote().getFrequency();
				double imagNom = slope * f + intercept;
				z = calculator.calcZ(target);
				double imagPred = z.getImaginary();
				double err = ( imagPred - imagNom )/imagNom;
				if (target.getNote().getFrequency() > 2.0 * fLow)
				{
					// Error doesn't matter as much in second octave.
					err *= 0.7;
				}
				errorVector[i++] = err; 
			}
		}
		return errorVector;
	}
}