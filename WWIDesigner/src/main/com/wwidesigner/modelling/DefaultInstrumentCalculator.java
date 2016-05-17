/**
 * Base class for calculating instrument playing characteristics.
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

import com.wwidesigner.geometry.ComponentInterface;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.calculation.BoreSectionCalculator;
import com.wwidesigner.geometry.calculation.HoleCalculator;
import com.wwidesigner.geometry.calculation.MouthpieceCalculator;
import com.wwidesigner.geometry.calculation.TerminationCalculator;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
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

	public DefaultInstrumentCalculator()
	{
	    super();
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
			else
			{
				assert component instanceof Hole;
				tm = holeCalculator.calcTransferMatrix((Hole) component,
						waveNumber, params);
			}
			sv = tm.multiply(sv);
		}
		sv = mouthpieceCalculator.calcStateVector(sv, instrument.getMouthpiece(), waveNumber, params);

		return sv;
		
	}
	
	@Override
	public Complex calcReflectionCoefficient(double frequency)
	{
		StateVector sv = calcInputStateVector(frequency);
		
		double headRadius = instrument.getMouthpiece().getBoreDiameter() / 2.;
		
		Complex reflectance = sv.getReflectance( params.calcZ0(headRadius) );
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
	public Complex calcZ(double freq)
	{
		return calcInputStateVector(freq).getImpedance();
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

}
