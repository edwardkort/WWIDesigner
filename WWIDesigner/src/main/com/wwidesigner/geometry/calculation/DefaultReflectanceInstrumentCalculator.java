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
public class DefaultReflectanceInstrumentCalculator extends
		InstrumentCalculator
{

	/**
	 * @param instrument
	 */
	public DefaultReflectanceInstrumentCalculator(Instrument instrument)
	{
		super(instrument);
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.geometry.InstrumentCalculator#calcRefOrImpCoefficent(double, com.wwidesigner.note.Fingering, com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public Complex calcRefOrImpCoefficent(double freq, Fingering fingering,
			PhysicalParameters params)
	{
		instrument.setOpenHoles(fingering);

		Complex reflectance = calculateReflectionCoefficient(freq,
				params);

		int reflectanceMultiplier = instrument.getMouthpiece().calcReflectanceMultiplier();

		Complex result = reflectance.multiply(reflectanceMultiplier);

		return result;
	}

	public Complex calculateReflectionCoefficient(double frequency,
			PhysicalParameters physicalParams)
	{
		double waveNumber = 2 * Math.PI * frequency
				/ physicalParams.getSpeedOfSound();

		instrument.updateComponents();

		TransferMatrix transferMatrix = TransferMatrix.makeIdentity();

		for (ComponentInterface component : instrument.getComponents())
		{
			transferMatrix = TransferMatrix.multiply(transferMatrix,
					component.calcTransferMatrix(waveNumber, physicalParams));
		}

		StateVector sv = TransferMatrix.multiply(transferMatrix,
				instrument.getTermination().calcStateVector(waveNumber, physicalParams));

		// TODO This mouthpiece calculation will change
		double headRadius = instrument.getMouthpiece().getBoreDiameter() / 2.;
		double characteristic_impedance = physicalParams.calcZ0(headRadius);
		Complex reflectance = sv.Reflectance(characteristic_impedance);
		return reflectance;
	}

}
