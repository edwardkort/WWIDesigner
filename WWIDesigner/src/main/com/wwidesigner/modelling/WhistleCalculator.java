/**
 * Class to calculate playing characteristics of whistles and other fipple flutes.
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
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.SimpleFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.UnflangedEndCalculator;
import com.wwidesigner.geometry.calculation.WhistleHoleCalculator;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

public class WhistleCalculator extends DefaultInstrumentCalculator
{
	// Constants used to predict windway velocity.
	protected static final double StrouhalZero = 0.26;		// Strouhal number at Im(Z) == 0.0
	protected static final double StrouhalSlope = -0.037;	// Change in Strouhal number with Im(Z)/Re(Z)
	protected static final double StrouhalMax = 0.50;		// Maximum strouhal number.
															// Below this, we are surely below fmin.
	protected static final double StrouhalMin = 0.01;		// Minimum strouhal number.
															// Below this, we are surely above fmax.

	public WhistleCalculator(Instrument instrument,
			PhysicalParameters physicalParams)
	{
		super(instrument, new SimpleFippleMouthpieceCalculator(),
				new UnflangedEndCalculator(), new WhistleHoleCalculator(),
				new SimpleBoreSectionCalculator(), physicalParams);
	}

	public WhistleCalculator()
	{
		super(new SimpleFippleMouthpieceCalculator(),
				new UnflangedEndCalculator(), new WhistleHoleCalculator(),
				new SimpleBoreSectionCalculator());
	}
	
	/**
	 * Predict the windway velocity required to produce
	 * the nominal frequency for a specified fingering.
	 * Result valid only if the nominal frequency is within a playing range
	 * (not checked).
	 * @param fingering
	 * @return predicted windway velocity
	 */
	public double predictV(Fingering fingering)
	{
		double freq = fingering.getNote().getFrequency();
		instrument.setOpenHoles(fingering);
		return predictV(freq);
	}

	/**
	 * Predict the windway velocity required to produce
	 * a specified frequency for a specified fingering.
	 * Result valid only if the nominal frequency is within a playing range
	 * (not checked).
	 * @param freq
	 * @param fingering
	 * @return predicted windway velocity
	 */
	public double predictV(double freq, Fingering fingering)
	{
		instrument.setOpenHoles(fingering);
		return predictV(freq);
	}

	/**
	 * Predict the windway velocity required to produce
	 * a specified frequency for the instrument's current fingering.
	 * Result valid only if the nominal frequency is within a playing range
	 * (not checked).
	 * @param freq
	 * @return predicted windway velocity
	 */
	public double predictV(double freq)
	{
		return predictV(freq, calcZ(freq));
	}

	/**
	 * Predict the windway velocity required to produce
	 * a specified frequency for the instrument's current fingering,
	 * given the overall impedance at the same frequency and fingering.
	 * Result valid only if the nominal frequency is within a playing range
	 * (not checked).
	 * @param freq
	 * @param impedance
	 * @return predicted windway velocity
	 */
	public double predictV(double freq, Complex Z)
	{
		if ( Z.getReal() == 0.0 )
		{
			return 0.0;
		}
		double windowLength = instrument.getMouthpiece().getAirstreamLength();
		double strouhal = StrouhalZero + StrouhalSlope * Z.getImaginary()/Z.getReal();
		// If strouhal is outside the range StrouhalZero .. StrouhalMax,
		// then we are outside a playing range.  It doesn't much matter
		// what velocity is as long as it is continuous, and
		// monotonic in freq.  This implementation isn't robust for
		// optimization because it doesn't impose steep penalties
		// outside of playing ranges, particularly below.
		if ( strouhal < StrouhalMin )
		{
			return freq * windowLength / StrouhalMin;
		}
		if ( strouhal > StrouhalMax )
		{
			return freq * windowLength / StrouhalMax;
		}
        return freq * windowLength / strouhal;
	}

}
