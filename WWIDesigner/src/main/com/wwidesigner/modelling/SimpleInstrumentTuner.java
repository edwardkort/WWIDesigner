/**
 * 
 */
package com.wwidesigner.modelling;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.InstrumentTuningTable;

/**
 * @author kort
 * 
 */
public class SimpleInstrumentTuner extends InstrumentTuner
{

	public Map<Fingering, Double> getTuning()
	{
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);
		PlayingRange range = new PlayingRange(calculator);

		Map<Fingering, Double> fingeringMap = new TreeMap<Fingering, Double>(
				new Comparator<Fingering>()
				{
					public int compare(Fingering arg1, Fingering arg2)
					{
						double pitch1 = arg1.getNote().getFrequency();
						double pitch2 = arg2.getNote().getFrequency();

						if (pitch1 < pitch2)
						{
							return -1;
						}
						return 1;
					}
				});

		for (Fingering fingering : tuning.getFingering())
		{
			calculator.setFingering(fingering);
			try
			{
				double playedFrequency = range.findXZero(fingering.getNote().getFrequency());
				fingeringMap.put(fingering, playedFrequency);
			}
			catch (NoPlayingRange e)
			{
				fingeringMap.put(fingering, null);
			}
		}

		return fingeringMap;
	}

	public void showTuning(String title, boolean exitOnTableClose)
	{
		InstrumentTuningTable table = makeInstrumentTuningTable(title);

		table.showTuning(exitOnTableClose);
	}

	protected InstrumentTuningTable makeInstrumentTuningTable(String title)
	{
		Map<Fingering, Double> fingeringMap = getTuning();

		InstrumentTuningTable table = new InstrumentTuningTable(title);

		for (Map.Entry<Fingering, Double> entry : fingeringMap.entrySet())
		{
			table.addTuning(entry.getKey(), entry.getValue());
		}

		return table;
	}

}
