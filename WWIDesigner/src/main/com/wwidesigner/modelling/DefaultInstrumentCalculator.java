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

	public DefaultInstrumentCalculator(Instrument instrument,
			PhysicalParameters physicalParams)
	{
		super(instrument, physicalParams);
	}

	public DefaultInstrumentCalculator(Instrument instrument,
			MouthpieceCalculator mouthpieceCalculator,
			TerminationCalculator terminationCalculator,
			HoleCalculator holeCalculator,
			BoreSectionCalculator boreSectionCalculator,
			PhysicalParameters physicalParams)
	{
		super(instrument, mouthpieceCalculator, terminationCalculator,
				holeCalculator, boreSectionCalculator, physicalParams);
	}

	public DefaultInstrumentCalculator(
			MouthpieceCalculator mouthpieceCalculator,
			TerminationCalculator terminationCalculator,
			HoleCalculator holeCalculator,
			BoreSectionCalculator boreSectionCalculator)
	{
		super(mouthpieceCalculator, terminationCalculator, holeCalculator,
				boreSectionCalculator);
	}

	private StateVector calcInputStateVector(double freq)
	{
		double waveNumber = params.calcWaveNumber(freq);

		// Start with the state vector of the termination,
		// and multiply by transfer matrices of each hole and bore segment
		// from the termination up to and including the mouthpiece.

		StateVector sv = terminationCalculator.calcStateVector(instrument.getTermination(), waveNumber, params);
		TransferMatrix tm;
		for (int componentNr = instrument.getComponents().size() - 1; componentNr >= 0; --componentNr)
		{
			ComponentInterface component = instrument.getComponents().get(
					componentNr);
			if (component instanceof BoreSection)
			{
				tm = boreSectionCalculator.calcTransferMatrix(
						(BoreSection) component, waveNumber, params);
			}
			else if (component instanceof Hole)
			{
				tm = holeCalculator.calcTransferMatrix((Hole) component,
						waveNumber, params);
			}
			else
			{
				assert component instanceof Mouthpiece;
				tm = mouthpieceCalculator.calcTransferMatrix(
						(Mouthpiece) component, waveNumber, params);
			}
			sv = tm.multiply(sv);
		}

		return sv;
		
	}
	
	@Override
	public Complex calcReflectionCoefficient(double frequency)
	{
		StateVector sv = calcInputStateVector(frequency);
		
		// TODO This mouthpiece calculation will change
		double headRadius = instrument.getMouthpiece().getBoreDiameter() / 2.;
		
		Complex reflectance = sv.Reflectance( params.calcZ0(headRadius) );
		
		int reflectanceMultiplier = mouthpieceCalculator.calcReflectanceMultiplier();

		return reflectance.multiply(reflectanceMultiplier);
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
	public Complex calcZ(double freq)
	{
		return calcInputStateVector(freq).Impedance();
	}

	@Override
	public double calcGain(double freq, Complex Z)
	{
		// Magnitude of loop gain for a given note, after Auvray, 2012.
		// Loop gain G = gainFactor * freq * rho / abs(Z).

		Double G0 = instrument.getMouthpiece().getGainFactor();
		if (G0 == null)
		{
			return 1.0;
		}
		double gain = (G0 * freq * params.getRho()) / Z.abs();
		return gain;
	}

	@Override
	public Double getPlayedFrequency(Fingering fingering, double freqRange,
			int numberOfFrequencies)
	{		Double playedFreq = null;
		double targetFreq = fingering.getNote().getFrequency();
		double freqStart = targetFreq / freqRange;
		double freqEnd = targetFreq * freqRange;
		ReflectanceSpectrum spectrum = new ReflectanceSpectrum();

		spectrum.calcReflectance(this.instrument, this, freqStart, freqEnd,
				numberOfFrequencies, fingering, params);
		playedFreq = spectrum.getClosestMinimumFrequency(targetFreq);

		return playedFreq;
	}
}
