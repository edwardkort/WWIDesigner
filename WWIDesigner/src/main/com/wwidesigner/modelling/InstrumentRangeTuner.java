/**
 * 
 */
package com.wwidesigner.modelling;

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * @author kort
 * 
 */
public class InstrumentRangeTuner extends InstrumentTuner
{
	WhistleEvaluator  evaluator;
	protected int blowingLevel;

	public InstrumentRangeTuner()
	{
		super();
		blowingLevel = 5;
	}
	
	public InstrumentRangeTuner(int blowingLevel)
	{
		super();
		this.blowingLevel = blowingLevel;
	}
	
	/* (non-Javadoc)
	 * @see com.wwidesigner.modelling.InstrumentTuner#getPredictedTuning()
	 */
	@Override
	public Tuning getPredictedTuning()
	{
		WhistleCalculator newCalc = new WhistleCalculator(instrument, params);
		evaluator = new WhistleEvaluator(newCalc, blowingLevel);
		evaluator.setFingering(tuning.getFingering());

		return super.getPredictedTuning();
	}


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
			fnom = range.findX(target, evaluator.getNominalX(target));
			predNote.setFrequency(fnom);
		}
		catch (NoPlayingRange e)
		{
			// Leave fnom unassigned.
		}
		return predNote;
	}

}
