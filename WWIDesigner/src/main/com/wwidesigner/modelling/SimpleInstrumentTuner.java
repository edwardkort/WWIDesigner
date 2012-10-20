/**
 * 
 */
package com.wwidesigner.modelling;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.InstrumentTuningTable;
import com.wwidesigner.note.Note;

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

		double maxFreqRatio = 2.;
		// set accuracy to 0.1 cents
		int numberOfFrequencies = (int) (10. * Note.cents(1.0,maxFreqRatio));

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
			Double playedFrequency = calculator.getPlayedFrequency(fingering,
					maxFreqRatio, numberOfFrequencies);
			fingeringMap.put(fingering, playedFrequency);
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
