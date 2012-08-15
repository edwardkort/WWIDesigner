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

	/** Calculate the reflection coefficient at the nominal frequency
	 * for a specified fingering.
	 * @param fingering
	 * @return reflection coefficient
	 */
	public Complex calcReflectionCoefficient(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcReflectionCoefficient(freq);
	}

	/** Calculate the reflection coefficient at a specified frequency
	 * and fingering.
	 * @param freq
	 * @param fingering
	 * @return reflection coefficient
	 */
	public Complex calcReflectionCoefficient(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcReflectionCoefficient(freq);
	}

	/** Calculate the reflection coefficient at a specified frequency
	 * for the instrument's current fingering.
	 * @param freq
	 * @return reflection coefficient
	 */
	public abstract Complex calcReflectionCoefficient(double freq);

	/** Calculate the overall impedance at the nominal frequency
	 * for a specified fingering.
	 * @param fingering
	 * @return impedance
	 */
	public Complex calcZ(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcZ(freq);
	}

	/** Calculate the overall impedance at a specified frequency
	 * and fingering.
	 * @param freq
	 * @param fingering
	 * @return impedance
	 */
	public Complex calcZ(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcZ(freq);
	}

	/** Calculate the overall impedance at a specified frequency
	 * for the instrument's current fingering.
	 * @param freq
	 * @return impedance
	 */
	public abstract Complex calcZ(double freq);

	/** Calculate the loop gain at the nominal frequency
	 * for a specified fingering.
	 * @param fingering
	 * @return loop gain
	 */
	public double calcGain(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcGain(freq);
	}

	/** Calculate the loop gain at a specified frequency
	 * and fingering.
	 * @param freq
	 * @param fingering
	 * @return loop gain
	 */
	public double calcGain(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcGain(freq);
	}

	/** Calculate the loop gain at a specified frequency
	 * for the instrument's current fingering.
	 * @param freq
	 * @return loop gain
	 */
	public double calcGain(double freq)
	{
		return calcGain( freq, calcZ(freq) );
	}

	/** Calculate the loop gain at a specified frequency
	 * for the instrument's current fingering, given the
	 * overall impedance at the same frequency and fingering.
	 * @param freq
	 * @param impedance
	 * @return loop gain
	 */
	public abstract double calcGain(double freq, Complex Z);

	public abstract Double getPlayedFrequency(Fingering fingering,
			double freqRange, int numberOfFrequencies);
}
