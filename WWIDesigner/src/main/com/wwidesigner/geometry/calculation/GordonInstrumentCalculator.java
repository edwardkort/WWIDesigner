/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.ComponentInterface;
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
public class GordonInstrumentCalculator extends InstrumentCalculator
{

	/**
	 * @param instrument
	 */
	public GordonInstrumentCalculator(Instrument instrument)
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
		TransferMatrix fluteTM = new TransferMatrix(Complex.ONE, Complex.ZERO,
				Complex.ZERO, Complex.ONE);

		instrument.setOpenHoles(fingering);

		for (ComponentInterface component : instrument.getComponents())
		{
			TransferMatrix compTM = component.calcTransferMatrix(freq, params);
			fluteTM = fluteTM.multiply(compTM);
		}

		StateVector sv = instrument.getTermination().calcStateVector(freq,
				params);
		Complex termImp = sv.Impedance();

		Complex result = termImp.multiply(fluteTM.getPP()).add(fluteTM.getPU())
				.divide(termImp.multiply(fluteTM.getUP()).add(fluteTM.getUU()));
		return result;
	}

}
