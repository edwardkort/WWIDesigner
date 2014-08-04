/**
 * 
 */
package com.wwidesigner.modelling;

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;

/**
 * InstrumentTuner for use with calculators that predict zero reactance
 * at the nominal playing frequency, rather than predicting minimum and
 * maximum frequencies of a playing range.
 * @author kort
 * 
 */
public class SimpleInstrumentTuner extends InstrumentTuner
{
	@Override
	public Double predictedFrequency(Fingering fingering)
	{
		PlayingRange range = new PlayingRange(calculator, fingering);
		try {
			return range.findXZero(fingering.getNote().getFrequency());
		}
		catch (NoPlayingRange e)
		{
			return null;
		}
	}
}
