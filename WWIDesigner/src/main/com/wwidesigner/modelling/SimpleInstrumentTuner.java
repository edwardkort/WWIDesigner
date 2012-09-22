/**
 * 
 */
package com.wwidesigner.modelling;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.InstrumentTuningTable;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class SimpleInstrumentTuner
{

	protected Instrument instrument;
	protected Tuning tuning;
	protected InstrumentCalculator calculator;
	protected PhysicalParameters params;

	/**
	 * 
	 */
	public SimpleInstrumentTuner()
	{
	}

	public SimpleInstrumentTuner(Instrument instrument, Tuning tuning,
			InstrumentCalculator calculator, PhysicalParameters params)
	{
		setInstrument(instrument);
		setTuning(tuning);
		setCalculator(calculator);
		setParams(params);
	}

	public Map<Fingering, Double> getTuning()
	{
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);

		double maxFreqRatio = 2.;
		// set accuracy to 0.1 cents
		int numberOfFrequencies = (int) (10. * InstrumentTuningTable
				.getCents(maxFreqRatio));

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

	public void showTuning(String title)
	{
		Map<Fingering, Double> fingeringMap = getTuning();

		InstrumentTuningTable table = new InstrumentTuningTable(title);

		for (Map.Entry<Fingering, Double> entry : fingeringMap.entrySet())
		{
			table.addTuning(entry.getKey(), entry.getValue());
		}

		table.showTuning();
	}

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument instrument)
	{
		this.instrument = instrument;
	}

	public void setInstrument(String xmlInstrumentFile, boolean fileInClasspath)
			throws Exception
	{
		BindFactory geomFactory = GeometryBindFactory.getInstance();
		Instrument instrument = (Instrument) geomFactory.unmarshalXml(
				xmlInstrumentFile, fileInClasspath, true);
		setInstrument(instrument);
	}

	/**
	 * @param tuning
	 *            the tuning to set
	 */
	public void setTuning(Tuning tuning)
	{
		this.tuning = tuning;
	}

	public void setTuning(String xmlTuningFile, boolean fileInClasspath)
			throws Exception
	{
		BindFactory noteFactory = NoteBindFactory.getInstance();
		Tuning tuning = (Tuning) noteFactory.unmarshalXml(xmlTuningFile,
				fileInClasspath, true);
		setTuning(tuning);
	}

	/**
	 * @param calculator
	 *            the calculator to set
	 */
	public void setCalculator(InstrumentCalculator calculator)
	{
		this.calculator = calculator;
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(PhysicalParameters params)
	{
		this.params = params;
	}

}
