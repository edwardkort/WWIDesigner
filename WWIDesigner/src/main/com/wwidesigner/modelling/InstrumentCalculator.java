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
	// The instrument being modeled.
	protected Instrument instrument;

	// Calculators used to model the instrument.
	protected MouthpieceCalculator mouthpieceCalculator;
	protected TerminationCalculator terminationCalculator;
	protected HoleCalculator holeCalculator;
	protected BoreSectionCalculator boreSectionCalculator;
	
	protected PhysicalParameters params;

	public InstrumentCalculator(Instrument instrument, PhysicalParameters physicalParams)
	{
		this.instrument = instrument;
		this.instrument.convertToMetres();
		this.instrument.updateComponents();
		this.mouthpieceCalculator = new NoOpMouthpieceCalculator();
		this.terminationCalculator = new IdealOpenEndCalculator();
		this.holeCalculator = new DefaultHoleCalculator();
		this.boreSectionCalculator = new DefaultBoreSectionCalculator();
		this.params = physicalParams;
	}

	public InstrumentCalculator(Instrument instrument,
			MouthpieceCalculator mouthpieceCalculator,
			TerminationCalculator terminationCalculator,
			HoleCalculator holeCalculator,
			BoreSectionCalculator boreSectionCalculator,
			PhysicalParameters physicalParams)
	{
		this.instrument = instrument;
		this.instrument.convertToMetres();
		this.instrument.updateComponents();
		this.mouthpieceCalculator = mouthpieceCalculator;
		this.terminationCalculator = terminationCalculator;
		this.holeCalculator = holeCalculator;
		this.boreSectionCalculator = boreSectionCalculator;
		this.params = physicalParams;
	}

	/**
	 * @param instrument
	 *            the instrument to set
	 */
	public void setInstrument(Instrument instrument)
	{
		this.instrument = instrument;
		this.instrument.convertToMetres();
		this.instrument.updateComponents();
	}

	/**
	 * @param mouthpieceCalculator
	 *            the mouthpieceCalculator to set
	 */
	public void setMouthpieceCalculator(
			MouthpieceCalculator mouthpieceCalculator)
	{
		this.mouthpieceCalculator = mouthpieceCalculator;
	}

	/**
	 * @param terminationCalculator
	 *            the terminationCalculator to set
	 */
	public void setTerminationCalculator(
			TerminationCalculator terminationCalculator)
	{
		this.terminationCalculator = terminationCalculator;
	}

	/**
	 * @param holeCalculator
	 *            the holeCalculator to set
	 */
	public void setHoleCalculator(HoleCalculator holeCalculator)
	{
		this.holeCalculator = holeCalculator;
	}

	/**
	 * @param boreSectionCalculator
	 *            the boreSectionCalculator to set
	 */
	public void setBoreSectionCalculator(
			BoreSectionCalculator boreSectionCalculator)
	{
		this.boreSectionCalculator = boreSectionCalculator;
	}

	public PhysicalParameters getPhysicalParameters()
	{
		return this.params;
	}

	public void setPhysicalParameters( PhysicalParameters physicalParams )
	{
		this.params = physicalParams;
	}
	
	public void setFingering(Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
	}

	public Complex calcReflectionCoefficient(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcReflectionCoefficient(freq);
	}

	public Complex calcReflectionCoefficient(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcReflectionCoefficient(freq);
	}

	public abstract Complex calcReflectionCoefficient(double freq);

	public Complex calcZ(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcZ(freq);
	}

	public Complex calcZ(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcZ(freq);
	}

	public abstract Complex calcZ(double freq);

	public abstract Double getPlayedFrequency(Fingering fingering,
			double freqRange, int numberOfFrequencies);
}
