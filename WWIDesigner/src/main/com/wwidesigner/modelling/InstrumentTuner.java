/**
 * Abstract class to generate and display instrument tuning tables.
 */
package com.wwidesigner.modelling;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
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

	public void showTuning(String title, boolean exitOnTableClose)
	{
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);
		Tuning predicted = getPredictedTuning();
		TuningComparisonTable table = new TuningComparisonTable(title);
		table.buildTable(tuning, predicted);
		table.showTuning(exitOnTableClose);
	}
	
	public void plotTuning(String title)
	{
		plotTuning(title, true);
	}

	public void plotTuning(String title, boolean exitOnTableClose)
	{
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);
		Tuning predicted = getPredictedTuning();
		PlotPlayingRanges plot = new PlotPlayingRanges(title);
		plot.buildGraph(calculator, tuning, predicted);
		plot.plotGraph(exitOnTableClose);
	}
	
	/**
	 * For a given target note, extract a frequency to use
	 * as a target frequency.  If no frequency available, return 0.
	 * @param fromNote
	 * @return frequency to use as a target frequency in tuning.
	 */
	protected double getFrequencyTarget(Note fromNote)
	{
		if ( fromNote.getFrequency() != null )
		{
			return fromNote.getFrequency();
		}
		if ( fromNote.getFrequencyMax() != null )
		{
			return fromNote.getFrequencyMax();
		}
		if ( fromNote.getFrequencyMin() != null )
		{
			return fromNote.getFrequencyMin();
		}
		return 0.0;

	}

	/**
	 * Predict the played note that the instrument will produce
	 * for a given target note.
	 * @param fingering
	 * @return note object with predicted frequencies for the specified fingering
	 */
	public abstract Note predictedNote(Fingering fingering);

	public Instrument getInstrument()
	{
		return instrument;
	}

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument instrument)
	{
		this.instrument = instrument;
		if (this.calculator != null)
		{
			this.calculator.setInstrument(this.instrument);
		}
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

	public Tuning getTuning()
	{
		return tuning;
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
		if (this.instrument != null)
		{
			this.calculator.setInstrument(instrument);
		}
		if (this.params != null)
		{
			this.calculator.setPhysicalParameters(this.params);
		}
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(PhysicalParameters params)
	{
		this.params = params;
		if (this.calculator != null)
		{
			this.calculator.setPhysicalParameters(this.params);
		}
	}

	/**
	 * Construct a predicted tuning for the instrument,
	 * with a predicted note for each note in the target tuning.
	 * @return predicted tuning
	 */
	public Tuning getPredictedTuning()
	{
		Tuning predicted = new Tuning();
		predicted.setName(tuning.getName());
		predicted.setComment(tuning.getComment());
		predicted.setNumberOfHoles(tuning.getNumberOfHoles());

		List<Fingering>  noteList = tuning.getFingering();
		List<Fingering>  newNotes = new ArrayList<Fingering>();

		for ( int i = 0; i < noteList.size(); ++ i )
		{
			Fingering fingering = noteList.get(i);
			Fingering predFingering = new Fingering();
			predFingering.setOpenHole(fingering.getOpenHole());
			predFingering.setNote(predictedNote(fingering));
			newNotes.add(predFingering);
		}
		predicted.setFingering(newNotes);
		return predicted;
	}

}
