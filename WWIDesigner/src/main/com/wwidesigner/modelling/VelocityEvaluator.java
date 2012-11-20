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
 * For the nominal playing pattern, we use a linear increase in
 * windway velocity from just below fmax for the lowest note,
 * to just above fmin for the highest note.  
 * This is only *one* possible playing pattern, and has not yet been
 * validated against the playing of real players.
 * 
 * This evaluator is not very robust for use in optimizations,
 * because the simplistic velocity prediction does not impose
 * penalties for frequencies outside of a playing range.
 * 
 * @author Burton Patkau
 */
public class VelocityEvaluator implements EvaluatorInterface
{
	protected WhistleCalculator  calculator;
	
	// Target velocity of lowest note is BottomMult of maximum velocity at target frequency.
	// Target velocity of highest note is either actual frequency, if gain >= 1,
	// or TopMult of estimated velocity at gain = 1.
	protected static final double BottomMult = 0.99;
	protected static final double TopMult = 1.10;

	public VelocityEvaluator( WhistleCalculator calculator )
	{
		this.calculator = calculator;
	}

	/**
	 * Calculate the signed percentage difference
	 * between nominal windway velocity for each fingering,
	 * and the predicted windway velocity for that fingering.
	 * @param fingeringTargets  - Fingerings, with target note for each.
	 * @return percentage difference between target and predicted velocities.
	 * 			length = fingeringTargets.size().
	 */

	public double[] calculateErrorVector(List<Fingering> fingeringTargets)
	{
		double[] errorVector = new double[fingeringTargets.size()];
		
		// Get lowest and highest target notes, and estimate a target velocity for each.

		Fingering noteLow = new Fingering();
		noteLow.setNote(new Note());
		noteLow.getNote().setFrequency(100000.0);		// Minimum target frequency.
		Fingering noteHigh = new Fingering();
		noteHigh.setNote(new Note());
		noteHigh.getNote().setFrequency(0.0);			// Maximum target frequency.
		for (Fingering target: fingeringTargets)
		{
			if ( target.getNote() != null
					&& target.getNote().getFrequency() != null )
			{
				if ( target.getNote().getFrequency() < noteLow.getNote().getFrequency() )
				{
					noteLow.getNote().setFrequency(target.getNote().getFrequency());
					noteLow.setOpenHole(target.getOpenHole());
				}
				if ( target.getNote().getFrequency() > noteHigh.getNote().getFrequency() )
				{
					noteHigh.getNote().setFrequency(target.getNote().getFrequency());
					noteHigh.setOpenHole(target.getOpenHole());
				}
			}
		}
		
		double fLow  = noteLow.getNote().getFrequency();		// Target frequency of lowest note.
		double fHigh = noteHigh.getNote().getFrequency();		// Target frequency of highest note.
		double vLow  = 0.0;
		double vHigh = 0.0;
		
		calculator.setFingering(noteLow);
		vLow = BottomMult * calculator.predictV(fLow, Complex.ONE);
		calculator.setFingering(noteHigh);
		Complex zHigh = calculator.calcZ(fHigh);
		double gainHigh = calculator.calcGain(fHigh,zHigh);
		if ( gainHigh >= 1 )
		{
			vHigh = calculator.predictV(fHigh,zHigh);
		}
		else {
			// Gain < 1.  Estimate impedance necessary to make gain = 1,
			// assuming Re(Z) doesn't change much when Gain < 1.
			double newAbs = calculator.calcGain(fHigh,Complex.ONE); 
			double newIm = - Math.sqrt( newAbs*newAbs - zHigh.getReal()*zHigh.getReal() );
			zHigh = new Complex(zHigh.getReal(), newIm );
			vHigh = TopMult * calculator.predictV(fHigh,zHigh);
		}
		
		// Nominal velocity is a linear interpolation between (fLow,vLow) and (fHigh,vHigh),
		// vnom = slope * frequency + intercept.
		double slope = (vHigh - vLow)/(fHigh - fLow);
		double intercept = vLow - slope * fLow;

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
				double vnom = slope * target.getNote().getFrequency() + intercept;
				double vpred = 0.0;
				Complex z = calculator.calcZ(target);
				vpred = calculator.predictV(f,z);
				double err = 100.0 * ( vpred - vnom )/vnom;
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