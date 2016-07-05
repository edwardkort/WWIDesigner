/**
 * Abstract class for calculating instrument playing characteristics.
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

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.geometry.calculation.BoreSectionCalculator;
import com.wwidesigner.geometry.calculation.HoleCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.MouthpieceCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.TerminationCalculator;
import com.wwidesigner.geometry.calculation.IdealOpenEndCalculator;

/**
 * Calculates attributes of the instrument body as seen by the driving source.<br/>
 * 
 * For flow-node mouthpieces (flutes and fipple flutes):<br/>
 * 
 * - calcZ() returns impedance seen by driving source.  Expect resonance
 *   when imaginary part is zero or phase angle is zero.<br/>
 * 
 * - calcReflectionCoefficient() returns coefficient of pressure reflection
 *   seen by driving source.  Expect resonance when coefficient is -1
 *   or phase angle is pi.<br/>
 *
 * For pressure-node mouthpieces (cane reeds, lip reeds, brass):<br/>
 * 
 * - calcZ() returns normalized admittance seen by driving source: Z0/Z.
 *   Expect resonance when imaginary part is zero or phase angle is zero.<br/>
 * 
 * - calcReflectionCoefficient() returns negative coefficient of pressure
 *   reflection (coefficient of flow reflection) seen by driving source.
 *   Expect resonance when coefficient is -1 or phase angle is pi.<br/>
 *
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

	public InstrumentCalculator()
	{
	}

	public InstrumentCalculator(Instrument instrument,
			PhysicalParameters physicalParams)
	{
		this.instrument = instrument;
		this.instrument.convertToMetres();
		this.instrument.updateComponents();
		this.mouthpieceCalculator = new MouthpieceCalculator();
		this.terminationCalculator = new IdealOpenEndCalculator();
		this.holeCalculator = new DefaultHoleCalculator();
		this.boreSectionCalculator = new SimpleBoreSectionCalculator();
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

	public InstrumentCalculator(MouthpieceCalculator mouthpieceCalculator,
			TerminationCalculator terminationCalculator,
			HoleCalculator holeCalculator,
			BoreSectionCalculator boreSectionCalculator)
	{
		this.mouthpieceCalculator = mouthpieceCalculator;
		this.terminationCalculator = terminationCalculator;
		this.holeCalculator = holeCalculator;
		this.boreSectionCalculator = boreSectionCalculator;
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

	public Instrument getInstrument()
	{
		return instrument;
	}

	public PhysicalParameters getPhysicalParameters()
	{
		return this.params;
	}

	public void setPhysicalParameters(PhysicalParameters physicalParams)
	{
		this.params = physicalParams;
	}

	public void setFingering(Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
	}

	/**
	 * Calculate the reflection coefficient at the nominal frequency for a
	 * specified fingering.
	 * 
	 * @param fingering
	 * @return coefficient of pressure reflection
	 */
	public Complex calcReflectionCoefficient(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcReflectionCoefficient(freq);
	}

	/**
	 * Calculate the reflection coefficient at a specified frequency and
	 * fingering.
	 * 
	 * @param freq
	 * @param fingering
	 * @return coefficient of pressure reflection
	 */
	public Complex calcReflectionCoefficient(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcReflectionCoefficient(freq);
	}

	/**
	 * Calculate the reflection coefficient at a specified frequency for the
	 * instrument's current fingering.
	 * 
	 * @param freq
	 * @return coefficient of pressure reflection
	 */
	public abstract Complex calcReflectionCoefficient(double freq);

	/**
	 * Calculate the overall impedance at the nominal frequency for a specified
	 * fingering.
	 * 
	 * @param fingering
	 * @return impedance
	 */
	public Complex calcZ(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcZ(freq);
	}

	/**
	 * Calculate the overall impedance at a specified frequency and fingering.
	 * 
	 * @param freq
	 * @param fingering
	 * @return impedance
	 */
	public Complex calcZ(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcZ(freq);
	}

	/**
	 * Calculate the overall impedance at a specified frequency for the
	 * instrument's current fingering.
	 * 
	 * @param freq
	 * @return impedance
	 */
	public abstract Complex calcZ(double freq);

	/**
	 * Calculate the loop gain at the nominal frequency for a specified
	 * fingering.
	 * 
	 * @param fingering
	 * @return loop gain
	 */
	public double calcGain(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return calcGain(freq);
	}

	/**
	 * Calculate the loop gain at a specified frequency and fingering.
	 * 
	 * @param freq
	 * @param fingering
	 * @return loop gain
	 */
	public double calcGain(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return calcGain(freq);
	}

	/**
	 * Calculate the loop gain at a specified frequency for the instrument's
	 * current fingering.
	 * 
	 * @param freq
	 * @return loop gain
	 */
	public double calcGain(double freq)
	{
		return calcGain(freq, calcZ(freq));
	}

	/**
	 * Calculate the loop gain at a specified frequency for the instrument's
	 * current fingering, given the overall impedance at the same frequency and
	 * fingering.
	 * 
	 * @param freq
	 * @param impedance
	 * @return loop gain
	 */
	public abstract double calcGain(double freq, Complex Z);

}
