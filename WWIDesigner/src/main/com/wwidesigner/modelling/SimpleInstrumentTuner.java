/**
 * 
 */
package com.wwidesigner.modelling;

import javax.swing.JTable;

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

	public void showTuning(String title)
	{
		InstrumentTuningTable table = getTuning(title);

		table.showTuning();
	}

	public InstrumentTuningTable getTuning(String title)
	{
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);

		double maxFreqRatio = 2.;
		// set accuracy to 0.1 cents
		int numberOfFrequencies = (int) (10. * InstrumentTuningTable
				.getCents(maxFreqRatio));

		InstrumentTuningTable table = new InstrumentTuningTable(title);

		for (Fingering fingering : tuning.getFingering())
		{
			Double playedFrequency = calculator.getPlayedFrequency(fingering,
					maxFreqRatio, numberOfFrequencies);
			table.addTuning(fingering, playedFrequency);
		}

		return table;
	}

	public JTable getTuningTable(String title)
	{
		InstrumentTuningTable table = getTuning(title);

		return table.getTuningTable();
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

	public void setInstrument(String instrumentXmlString) throws Exception
	{
		BindFactory geomFactory = GeometryBindFactory.getInstance();
		Instrument instrument = (Instrument) geomFactory.unmarshalXml(
				instrumentXmlString, true);
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

	public void setTuning(String tuningXmlString) throws Exception
	{
		BindFactory noteFactory = NoteBindFactory.getInstance();
		Tuning tuning = (Tuning) noteFactory
				.unmarshalXml(tuningXmlString, true);
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
