/**
 * 
 */
package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class InstrumentCalculator
{
	protected Instrument instrument;

	public InstrumentCalculator(Instrument instrument)
	{
		this.instrument = instrument;
	}

	public Complex calcRefOrImpCoefficient(Fingering fingering,
			PhysicalParameters params)
	{
		double freq = fingering.getNote().getFrequency();

		return calcRefOrImpCoefficent(freq, fingering, params);
	}

	public abstract Complex calcRefOrImpCoefficent(double freq,
			Fingering fingering, PhysicalParameters params);
}
