package com.wwidesigner.impedance.geometry;

import java.util.List;

import org.apache.commons.math.complex.Complex;

import com.wwidesigner.impedance.note.NominalNote;
import com.wwidesigner.impedance.note.NoteConfig;
import com.wwidesigner.impedance.note.Temperament;

public interface InstrumentInterface
{

	public void validate();

	/**
	 * Set the open and closed states of the holes in order to produce the given
	 * note.
	 */
	public void setNote(NominalNote nominalNote);

	/**
	 * Calculate the impedance at frequency \a freq.
	 */
	public Complex calcZ(double freq);

	public Temperament getTemperament();

	public void setTemperament(Temperament temperament);

	public EmbouchureInterface getEmbouchure();

	public List<BoreSection> getBoreSections();

	public List<Hole> getHoles();

	public TerminationInterface getTermination();

	public List<NoteConfig> getNoteConfigs();

	public void setEmbouchure(Embouchure embouchure);

	public void setTermination(TerminationInterface termination);

	public double getPitchStandard();

	public void setPitchStandard(double pitchStandard);

}