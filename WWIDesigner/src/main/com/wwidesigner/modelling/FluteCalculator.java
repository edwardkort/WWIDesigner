/**
 * Class to calculate playing characteristics of transverse flutes.
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

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.FluteMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.UnflangedEndCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class FluteCalculator extends DefaultInstrumentCalculator
{
	public FluteCalculator(Instrument instrument,
			PhysicalParameters physicalParams)
	{
		super(instrument, new FluteMouthpieceCalculator(),
				new UnflangedEndCalculator(), new DefaultHoleCalculator(),
				new SimpleBoreSectionCalculator(), physicalParams);
	}

	public FluteCalculator()
	{
		super(new FluteMouthpieceCalculator(),
				new UnflangedEndCalculator(), new DefaultHoleCalculator(),
				new SimpleBoreSectionCalculator());
	}
}
