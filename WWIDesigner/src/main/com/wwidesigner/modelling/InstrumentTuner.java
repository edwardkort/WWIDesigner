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

	public InstrumentTuner(Instrument aInstrument, Tuning aTuning,
			InstrumentCalculator aCalculator, PhysicalParameters aParams)
	{
		setInstrument(aInstrument);
		setCalculator(aCalculator);
		setParams(aParams);
		setTuning(aTuning);
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
	 * @param aInstrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument aInstrument)
	{
		this.instrument = aInstrument;
		if (this.calculator != null)
		{
			this.calculator.setInstrument(this.instrument);
		}
	}

	public void setInstrument(String xmlInstrumentFile, boolean fileInClasspath)
			throws Exception
	{
		BindFactory geomFactory = GeometryBindFactory.getInstance();
		Instrument thisInstrument = (Instrument) geomFactory.unmarshalXml(
				xmlInstrumentFile, fileInClasspath, true);
		setInstrument(thisInstrument);
	}

	public void setInstrument(String instrumentXmlString) throws Exception
	{
		BindFactory geomFactory = GeometryBindFactory.getInstance();
		Instrument thisInstrument = (Instrument) geomFactory.unmarshalXml(
				instrumentXmlString, true);
		setInstrument(thisInstrument);
	}

	public Tuning getTuning()
	{
		return tuning;
	}

	/**
	 * @param aTuning
	 *            the tuning to set
	 */
	public void setTuning(Tuning aTuning)
	{
		this.tuning = aTuning;
	}

	public void setTuning(String xmlTuningFile, boolean fileInClasspath)
			throws Exception
	{
		BindFactory noteFactory = NoteBindFactory.getInstance();
		Tuning thisTuning = (Tuning) noteFactory.unmarshalXml(xmlTuningFile,
				fileInClasspath, true);
		setTuning(thisTuning);
	}

	public void setTuning(String tuningXmlString) throws Exception
	{
		BindFactory noteFactory = NoteBindFactory.getInstance();
		Tuning thisTuning = (Tuning) noteFactory
				.unmarshalXml(tuningXmlString, true);
		setTuning(thisTuning);
	}
	
	public InstrumentCalculator getCalculator()
	{
		return this.calculator;
	}

	/**
	 * @param aCalculator
	 *            the calculator to set
	 */
	public void setCalculator(InstrumentCalculator aCalculator)
	{
		this.calculator = aCalculator;
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
	 * @param aParams
	 *            the params to set
	 */
	public void setParams(PhysicalParameters aParams)
	{
		this.params = aParams;
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
