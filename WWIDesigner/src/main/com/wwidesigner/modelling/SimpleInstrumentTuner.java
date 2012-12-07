/**
 * 
 */
package com.wwidesigner.modelling;

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;

/**
 * @author kort
 * 
 */
public class SimpleInstrumentTuner extends InstrumentTuner
{
	@Override
	public Note predictedNote(Fingering fingering)
	{
		Note targetNote = fingering.getNote();
		Note predNote = new Note();
		predNote.setName(targetNote.getName());
		double target = getFrequencyTarget(targetNote);
		
		if (target == 0.0)
		{
			// No target frequency.
			// Return note without prediction, because we can't make a prediction.
			return predNote;
		}

		PlayingRange range = new PlayingRange(calculator, fingering);
		try {
			double playedFrequency = range.findXZero(fingering.getNote().getFrequency());
			predNote.setFrequency(playedFrequency);
		}
		catch (NoPlayingRange e)
		{
			// Leave frequency unassigned.
		}
		return predNote;
	}

}
