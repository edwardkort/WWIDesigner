/**
 * 
 */
package com.wwidesigner.modelling;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.ComponentInterface;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.modelling.InstrumentCalculator;
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
	public Complex calcZ(double freq, Fingering fingering,
			PhysicalParameters params)
	{
		TransferMatrix fluteTM = new TransferMatrix(Complex.ONE, Complex.ZERO,
				Complex.ZERO, Complex.ONE);

		double waveNumber = params.calcWaveNumber(freq);
		instrument.setOpenHoles(fingering);

		for (ComponentInterface component : instrument.getComponents())
		{
			TransferMatrix compTM = null;
			if (component instanceof BoreSection)
			{
				compTM = boreSectionCalculator.calcTransferMatrix((BoreSection) component,
						waveNumber, params);
			}
			else if (component instanceof Hole)
			{
				compTM = holeCalculator.calcTransferMatrix((Hole) component,
						waveNumber, params);
			}
			else if (component instanceof Mouthpiece)
			{
				compTM = mouthpieceCalculator.calcTransferMatrix((Mouthpiece) component,
						waveNumber, params);
			}
			
			fluteTM = fluteTM.multiply(compTM);
		}

		StateVector sv = terminationCalculator.calcStateVector(instrument.getTermination(), waveNumber, params);
		Complex termImp = sv.Impedance();

		Complex result = termImp.multiply(fluteTM.getPP()).add(fluteTM.getPU())
				.divide(termImp.multiply(fluteTM.getUP()).add(fluteTM.getUU()));
		return result;
	}

	@Override
	public Complex calcReflectionCoefficient(double freq, Fingering fingering,
			PhysicalParameters params)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getPlayedFrequency(Fingering fingering, double freqRange,
			int numberOfFrequencies, PhysicalParameters params)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
