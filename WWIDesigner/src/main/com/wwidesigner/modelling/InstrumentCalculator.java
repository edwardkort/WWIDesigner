/**
 * 
 */
package com.wwidesigner.modelling;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.geometry.calculation.BoreSectionCalculator;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.HoleCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.MouthpieceCalculator;
import com.wwidesigner.geometry.calculation.NoOpMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.TerminationCalculator;
import com.wwidesigner.geometry.calculation.IdealOpenEndCalculator;

/**
 * @author kort
 * 
 */
public abstract class InstrumentCalculator
{
	// The instrument being modelled.
	protected Instrument instrument;

	// Calculators used to model the instrument.
	protected MouthpieceCalculator mouthpieceCalculator;
	protected TerminationCalculator terminationCalculator;
	protected HoleCalculator holeCalculator;
	protected BoreSectionCalculator boreSectionCalculator;

	public InstrumentCalculator(Instrument instrument)
	{
		this.instrument = instrument;
		this.instrument.convertToMetres();
		this.instrument.updateComponents();
		this.mouthpieceCalculator = new NoOpMouthpieceCalculator();
		this.terminationCalculator = new IdealOpenEndCalculator();
		this.holeCalculator = new DefaultHoleCalculator();
		this.boreSectionCalculator = new DefaultBoreSectionCalculator();
	}

	public InstrumentCalculator(Instrument instrument,
								MouthpieceCalculator mouthpieceCalculator,
								TerminationCalculator terminationCalculator,
								HoleCalculator holeCalculator,
								BoreSectionCalculator boreSectionCalculator )
	{
		this.instrument = instrument;
		this.instrument.convertToMetres();
		this.instrument.updateComponents();
		this.mouthpieceCalculator = mouthpieceCalculator;
		this.terminationCalculator = terminationCalculator;
		this.holeCalculator = holeCalculator;
		this.boreSectionCalculator = boreSectionCalculator;
	}

	public Complex calcReflectionCoefficient(Fingering fingering,
			PhysicalParameters params)
	{
		double freq = fingering.getNote().getFrequency();

		return calcReflectionCoefficient(freq, fingering, params);
	}

	public abstract Complex calcReflectionCoefficient(double freq,
			Fingering fingering, PhysicalParameters params);

	public Complex calcZ(Fingering fingering,
			PhysicalParameters params)
	{
		double freq = fingering.getNote().getFrequency();

		return calcZ(freq, fingering, params);
	}

	public abstract Complex calcZ(double freq,
			Fingering fingering, PhysicalParameters params);

	public abstract Double getPlayedFrequency(Fingering fingering, double freqRange,
			int numberOfFrequencies, PhysicalParameters params);
}
