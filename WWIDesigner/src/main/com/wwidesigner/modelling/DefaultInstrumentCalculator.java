/**
 * 
 */
package com.wwidesigner.modelling;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.ComponentInterface;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.calculation.BoreSectionCalculator;
import com.wwidesigner.geometry.calculation.HoleCalculator;
import com.wwidesigner.geometry.calculation.MouthpieceCalculator;
import com.wwidesigner.geometry.calculation.TerminationCalculator;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class DefaultInstrumentCalculator extends InstrumentCalculator
{

	public DefaultInstrumentCalculator(Instrument instrument)
	{
		super(instrument);
	}

	public DefaultInstrumentCalculator(Instrument instrument,
			MouthpieceCalculator mouthpieceCalculator,
			TerminationCalculator terminationCalculator,
			HoleCalculator holeCalculator,
			BoreSectionCalculator boreSectionCalculator)
	{
		super(instrument, mouthpieceCalculator, terminationCalculator, holeCalculator,
				boreSectionCalculator);
	}

	@Override
	public Complex calcReflectionCoefficient(double freq, Fingering fingering,
			PhysicalParameters params)
	{
		instrument.setOpenHoles(fingering);

		Complex reflectance = calculateReflectionCoefficient(freq,
				params);

		int reflectanceMultiplier = mouthpieceCalculator.calcReflectanceMultiplier();

		Complex result = reflectance.multiply(reflectanceMultiplier);

		return result;
	}

	public Complex calculateReflectionCoefficient(double frequency,
			PhysicalParameters physicalParams)
	{
		instrument.updateComponents();

		double waveNumber = physicalParams.calcWaveNumber(frequency);

		// Start with the state vector of the termination,
		// and multiply by transfer matrices of each hole and bore segment
		// from the termination up to and including the mouthpiece.

		StateVector sv = terminationCalculator.calcStateVector( instrument.getTermination(), waveNumber, physicalParams );
		TransferMatrix tm;
		for (int componentNr = instrument.getComponents().size() - 1; componentNr >= 0; --componentNr)
		{
			ComponentInterface component = instrument.getComponents().get(componentNr);
			if (component instanceof BoreSection)
			{
				tm = boreSectionCalculator.calcTransferMatrix((BoreSection) component,
						waveNumber, physicalParams);
			}
			else if (component instanceof Hole)
			{
				tm = holeCalculator.calcTransferMatrix((Hole) component,
						waveNumber, physicalParams);
			}
			else
			{
				assert component instanceof Mouthpiece;
				tm = mouthpieceCalculator.calcTransferMatrix((Mouthpiece) component,
						waveNumber, physicalParams);
			}
			sv = tm.multiply(sv);
		}

		// TODO This mouthpiece calculation will change
		double headRadius = instrument.getMouthpiece().getBoreDiameter() / 2.;
		double characteristic_impedance = physicalParams.calcZ0(headRadius);
		Complex reflectance = sv.Reflectance(characteristic_impedance);
		return reflectance;
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
		instrument.setOpenHoles(fingering);

		double waveNumber = params.calcWaveNumber(freq);

		// Start with the state vector of the termination,
		// and multiply by transfer matrices of each hole and bore segment
		// from the termination up to, but not including the mouthpiece.

		StateVector sv = terminationCalculator.calcStateVector( instrument.getTermination(), waveNumber, params );
		TransferMatrix tm;
		for (int componentNr = instrument.getComponents().size() - 1; componentNr > 0; --componentNr)
		{
			ComponentInterface component = instrument.getComponents().get(componentNr);
			if (component instanceof BoreSection)
			{
				tm = boreSectionCalculator.calcTransferMatrix((BoreSection) component,
						waveNumber, params);
			}
			else if (component instanceof Hole)
			{
				tm = holeCalculator.calcTransferMatrix((Hole) component,
						waveNumber, params);
			}
			else
			{
				assert component instanceof Mouthpiece;
				tm = mouthpieceCalculator.calcTransferMatrix((Mouthpiece) component,
						waveNumber, params);
			}
			sv = tm.multiply(sv);
		}
		Complex Zresonator = sv.Impedance();

		Complex Zwindow = mouthpieceCalculator.calcZ(instrument.getMouthpiece(), freq, params);

		return Zresonator.add(Zwindow);
	}

	@Override
	public Double getPlayedFrequency(Fingering fingering, double freqRange,
			int numberOfFrequencies, PhysicalParameters params)
	{
		Double playedFreq = null;
		double targetFreq = fingering.getNote().getFrequency();
		double freqStart = targetFreq / freqRange;
		double freqEnd = targetFreq * freqRange;
		ReflectanceSpectrum spectrum = new ReflectanceSpectrum();

		spectrum.calcReflectance(this.instrument, this, freqStart, freqEnd, numberOfFrequencies,
				fingering, params);
		playedFreq = spectrum.getClosestMinimumFrequency(targetFreq);

		return playedFreq;
	}
}
