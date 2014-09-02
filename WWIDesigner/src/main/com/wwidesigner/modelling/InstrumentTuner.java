/**
 * Abstract class for generating and displaying instrument tuning tables.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		setCalculator(calculator);
		setParams(params);
		setTuning(tuning);
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
	 * Predict the nominal playing frequency that the instrument will produce
	 * for a given target note.
	 * @param fingering - Target note and fingering.
	 * @return Predicted nominal playing frequency for the specified fingering.
	 */
	public abstract Double predictedFrequency(Fingering fingering);

	/**
	 * Predict the played note that the instrument will produce
	 * for a given target note.
	 * predictedNote(fg).getFrequency() = predictedFrequency(fg).
	 * For a derived class, predictedNote(fg) may also supply frequencyMax and frequencyMin.
	 * @param fingering - Target note and fingering.
	 * @return note object with predicted frequencies for the specified fingering
	 */
	public Note predictedNote(Fingering fingering)
	{
		Note predNote = new Note();
		predNote.setName(fingering.getNote().getName());
		Double predicted = predictedFrequency(fingering);
		if (predicted != null)
		{
			// Assign a predicted frequency only if one is available.
			predNote.setFrequency(predicted);
		}
		return predNote;
	}

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
