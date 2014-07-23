/**
 * 
 */
package com.wwidesigner.modelling;

import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.PhysicalParameters;

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
public class LinearXInstrumentTuner extends InstrumentTuner
{
	// Target reactance of lowest note is BottomFraction of its value at fmin.
	// Target reactance of highest note is TopFraction of its value at fmin.
	protected double BottomFraction;
	protected double TopFraction;

	// Standard ranges for BottomFraction and TopFraction.
	// Evaluator uses blowing level, 0 .. 10, to interpolate between these ranges,
	// using the Lo value at blowing level 0 and the Hi value at blowing level 10.
	// Default fractions are the average of Hi and Lo values, blowing level 5.
	protected static final double BottomLo = 0.30;
	protected static final double BottomHi = 0.02;
	protected static final double TopLo = 0.95;
	protected static final double TopHi = 0.20;

	protected double fLow;		// Lowest frequency in target range.
	protected double fHigh;		// Highest frequency in target range.
	// Linear equation parameters for calculating nominal impedance:
	// Xnom = slope * f + intercept.
	protected double slope;
	protected double intercept;

	public LinearXInstrumentTuner()
	{
		this(5);
	}
	
	public LinearXInstrumentTuner(int blowingLevel)
	{
		this(BottomHi - (double)((10-blowingLevel)*(10-blowingLevel)) * 0.01 * (BottomHi - BottomLo),
			 TopLo + (double)blowingLevel * 0.1 * (TopHi - TopLo));
	}
	
	public LinearXInstrumentTuner(double bottomFr, double topFr)
	{
		super();
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

	/* (non-Javadoc)
	 * @see com.wwidesigner.modelling.InstrumentTuner#setTuning(com.wwidesigner.note.Tuning)
	 */
	@Override
	public void setTuning(Tuning tuning)
	{
		super.setTuning(tuning);
		if (tuning != null && calculator != null)
		{
			setFingering(tuning.getFingering());
		}
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.modelling.InstrumentTuner#setCalculator(com.wwidesigner.modelling.InstrumentCalculator)
	 */
	@Override
	public void setCalculator(InstrumentCalculator calculator)
	{
		super.setCalculator(calculator);
		if (tuning != null && calculator != null)
		{
			setFingering(tuning.getFingering());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.wwidesigner.modelling.InstrumentTuner#setParams(com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public void setParams(PhysicalParameters params)
	{
		super.setParams(params);
		if (tuning != null && calculator != null)
		{
			setFingering(tuning.getFingering());
		}
	}
	

	/* (non-Javadoc)
	 * @see com.wwidesigner.modelling.InstrumentTuner#predictedNote(com.wwidesigner.note.Fingering)
	 */
	@Override
	public Note predictedNote(Fingering fingering)
	{
		Note targetNote = fingering.getNote();
		Note predNote = new Note();
		predNote.setName(targetNote.getName());
		if ( targetNote.getFrequency() != null )
		{
			predNote.setFrequency(targetNote.getFrequency());
		}
		double target = getFrequencyTarget(targetNote);
		
		if (target == 0.0)
		{
			// No target frequency.
			// Return note without prediction, because we can't make a prediction.
			return predNote;
		}

		// Predict playing range.
		PlayingRange range = new PlayingRange(calculator, fingering);
		double fmax, fmin, fnom;
		try {
			fmax = range.findXZero(target);
			predNote.setFrequencyMax(fmax);
			fmin = range.findFmin(fmax);
			predNote.setFrequencyMin(fmin);
		}
		catch (NoPlayingRange e)
		{
			// Leave fmax and fmin unassigned.
		}
		try {
			fnom = range.findX(target, getNominalX(target));
			predNote.setFrequency(fnom);
		}
		catch (NoPlayingRange e)
		{
			// Leave fnom unassigned.
		}
		return predNote;
	}

}
