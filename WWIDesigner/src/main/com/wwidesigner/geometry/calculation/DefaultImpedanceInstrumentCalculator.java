/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.InstrumentCalculator;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class DefaultImpedanceInstrumentCalculator extends InstrumentCalculator
{

	/**
	 * @param instrument
	 */
	public DefaultImpedanceInstrumentCalculator(Instrument instrument)
	{
		super(instrument);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentCalculator#calcRefOrImpCoefficent(
	 * double, com.wwidesigner.note.Fingering,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public Complex calcRefOrImpCoefficent(double freq, Fingering fingering,
			PhysicalParameters params)
	{
		instrument.setOpenHoles(fingering);

		double waveNumber = params.calcWaveNumber(freq);

		// Start with the state vector of the termination,
		// and multiply by transfer matrices of each hole and bore segment
		// from the termination up to, but not including the mouthpiece.

		StateVector sv = instrument.getTermination().calcStateVector(
				waveNumber, params);
		TransferMatrix tm;
		Complex Zresonator = sv.Impedance();
		for (int componentNr = instrument.getComponents().size() - 1; componentNr > 0; --componentNr)
		{
			tm = instrument.getComponents().get(componentNr)
					.calcTransferMatrix(waveNumber, params);
			sv = tm.multiply(sv);
			Zresonator = sv.Impedance();
		}

		Complex Zwindow = instrument.getMouthpiece().calcZ(freq, params);

		return Zresonator.add(Zwindow);
	}

}
