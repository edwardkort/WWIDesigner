/**
 * Abstract class to generate and display instrument tuning tables.
 */
package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class InstrumentTuner
{

	protected Instrument instrument;
	protected Tuning tuning;
	protected InstrumentCalculator calculator;
	protected PhysicalParameters params;

	/**
	 * 
	 */
	public InstrumentTuner()
	{
	}

	public InstrumentTuner(Instrument instrument, Tuning tuning,
			InstrumentCalculator calculator, PhysicalParameters params)
	{
		setInstrument(instrument);
		setTuning(tuning);
		setCalculator(calculator);
		setParams(params);
	}

	public void showTuning(String title)
	{
		showTuning(title, true);
	}

	public abstract void showTuning(String title, boolean exitOnTableClose);

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
