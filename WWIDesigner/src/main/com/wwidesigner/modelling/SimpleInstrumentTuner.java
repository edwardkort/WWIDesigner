/**
 * Class to generate and display instrument tuning tables using simple instrument calculators.
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

import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;

/**
 * InstrumentTuner for use with calculators that predict zero reactance
 * at the nominal playing frequency, rather than predicting minimum and
 * maximum frequencies of a playing range.
 * @author kort
 * 
 */
public class SimpleInstrumentTuner extends InstrumentTuner
{
	@Override
	public Double predictedFrequency(Fingering fingering)
	{
		if (fingering.getNote() == null || fingering.getNote().getFrequency() == null)
		{
			return null;
		}
		PlayingRange range = new PlayingRange(calculator, fingering);
		try {
			return range.findXZero(fingering.getNote().getFrequency());
		}
		catch (NoPlayingRange e)
		{
			return null;
		}
	}
}
