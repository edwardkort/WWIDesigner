package com.wwidesigner.modelling;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
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
	protected double BottomFraction;
	protected double TopFraction;

	// Standard ranges for BottomFraction and TopFraction.
	// Evaluator uses blowing level, 0 .. 10, to interpolate between these ranges,
	// using the Lo value at blowin level 0 and the Hi value at blowing level 10.
	// Default fractions are the average of Hi and Lo values, blowing level 5.
	protected static final double BottomLo = 0.08;
	protected static final double BottomHi = 0.02;
	protected static final double TopLo = 0.9;
	protected static final double TopHi = 0.1;

	protected double fLow;		// Lowest frequency in target range.
	protected double fHigh;		// Highest frequency in target range.
	// Linear equation parameters for calculating nominal impedance:
	// Xnom = slope * f + intercept.
	protected double slope;
	protected double intercept;

	public WhistleEvaluator( WhistleCalculator calculator )
	{
		this(calculator,5);
	}
	
	public WhistleEvaluator( WhistleCalculator calculator, int blowingLevel )
	{
		this(calculator,
				BottomLo + (double)blowingLevel * 0.1 * (BottomHi - BottomLo),
				TopLo + (double)blowingLevel * 0.1 * (TopHi - TopLo));
	}

	public WhistleEvaluator( WhistleCalculator calculator, double bottomFr, double topFr )
	{
		this.calculator = calculator;
		BottomFraction = bottomFr;
		TopFraction    = topFr;
		fLow           = 100.0;
		fHigh          = 100.0;
		slope          = 0.0;
		intercept      = 0.0;
	}
	
	/**
	 * Set interpolation parameters to interpolate reactance
	 * for a specified set of fingering targets.
	 * Following this call, use getNominalX() to return interpolated reactance.
	 * @param fingeringTargets
	 */
	public void setFingering(List<Fingering> fingeringTargets)
	{
		// Get lowest and highest target notes, and estimate a target reactance for each.

		// Target frequencies for lowest and highest note,
		// then the nominal frequency for these notes, used in
		// linear interpolation of reactance.
		fLow  = 100000.0;
		fHigh = 0.0;
		// Target reactance for lowest and highest notes.
		double xLow  = 0.0;
		double xHigh = 0.0;

		// Find lowest and highest target notes.

		Fingering noteLow = new Fingering();
		noteLow.setNote(new Note());
		Fingering noteHigh = new Fingering();
		noteHigh.setNote(new Note());
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() != null )
			{
				if( target.getNote().getFrequency() != null )
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
				else if( target.getNote().getFrequencyMax() != null )
				{
					// If we don't have a nominal frequency, look for fmax.
					if ( target.getNote().getFrequencyMax() < fLow )
					{
						fLow = target.getNote().getFrequencyMax();
						noteLow.setOpenHole(target.getOpenHole());
					}
					if ( target.getNote().getFrequencyMax() > fHigh )
					{
						fHigh = target.getNote().getFrequencyMax();
						noteHigh.setOpenHole(target.getOpenHole());
					}
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
		try 
		{
			fmax = range.findXZero(fLow);
			fmin = range.findFmin(fmax);
			z = calculator.calcZ(fmin);
			xLow = BottomFraction * z.getImaginary();
		}
		catch ( NoPlayingRange e )
		{
			fmax = fmin = fLow;
			xLow = 0.0;
		}
		fLow = fmax;	// Nominal frequency for our interpolation.
		
		calculator.setFingering(noteHigh);
		try
		{
			fmax = range.findXZero(fHigh);
			fmin = range.findFmin(fmax);
			z = calculator.calcZ(fmin);
			xHigh = TopFraction * z.getImaginary();
		}
		catch ( NoPlayingRange e )
		{
			fmax = fmin = fHigh;
			xHigh = -1.0e6;		// Arbitrary line-in-the-sand.
		}
		fHigh = fmax;	// Nominal frequency for our interpolation.
		
		// Nominal reactance is a linear interpolation between (fLow,imagLow) and (fHigh,imagHigh),
		// imagNom = slope * frequency + intercept.
		slope = (xHigh - xLow)/(fHigh - fLow);
		intercept = xLow - slope * fLow;
	}
	
	/**
	 * Following a call to setFingering(), return interpolated reactance.
	 * @param f - frequency
	 * @return nominal reactance at specified frequency
	 */
	public double getNominalX(double f)
	{
		return slope * f + intercept;
	}

	/**
	 * Calculate the signed difference
	 * between nominal reactance for each fingering,
	 * and the predicted reactance for that fingering.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return difference between target and predicted reactance,
	 * 			divided by square of target frequency.
	 * 			length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];
		double f, xNom, xPred, err;
		Complex z;

		setFingering(fingeringTargets);

		int i = 0;
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() == null || target.getNote().getFrequency() == null )
			{
				errorVector[i++] = 0.0;
			}
			else
			{
				f = target.getNote().getFrequency();
				xNom = getNominalX(f);
				z = calculator.calcZ(target);
				xPred = z.getImaginary();
				err = ( xPred - xNom )/(f*f);
				errorVector[i++] = err; 
			}
		}
		return errorVector;
	}
}