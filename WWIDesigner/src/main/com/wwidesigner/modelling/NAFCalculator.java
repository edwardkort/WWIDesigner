/**
 * Class to calculate NAF playing characteristics.
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
import com.wwidesigner.geometry.calculation.DefaultFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
//import com.wwidesigner.geometry.calculation.SimpleHoleCalculator;
import com.wwidesigner.geometry.calculation.ThickFlangedOpenEndCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class NAFCalculator extends DefaultInstrumentCalculator
{
	public NAFCalculator(Instrument aInstrument,
			PhysicalParameters physicalParams)
	{
		super(aInstrument, physicalParams);

		setMouthpieceCalculator(new DefaultFippleMouthpieceCalculator());
		setTerminationCalculator(new ThickFlangedOpenEndCalculator());

		// Mike Prairie 6-hole, min. tuning deviation
		// setHoleCalculator(new DefaultHoleCalculator(0.8294));

		// version 2.2.0 and earlier
		// setHoleCalculator(new DefaultHoleCalculator(0.9457));

		// based on 6/11/2019 validation runs
		setHoleCalculator(new DefaultHoleCalculator(0.9605));

		// invoke a simplified hole calculator
		// setHoleCalculator(new SimpleHoleCalculator(0.9457));

		setBoreSectionCalculator(new SimpleBoreSectionCalculator());
	}

	public NAFCalculator()
	{
		super();

		setMouthpieceCalculator(new DefaultFippleMouthpieceCalculator());
		setTerminationCalculator(new ThickFlangedOpenEndCalculator());

		// Mike Prairie 6-hole, min. tuning deviation
		// setHoleCalculator(new DefaultHoleCalculator(0.8294));

		// version 2.2.0 and earlier
		// setHoleCalculator(new DefaultHoleCalculator(0.9457));

		// based on 6/11/2019 validation runs
		setHoleCalculator(new DefaultHoleCalculator(0.9605));

		// invoke a simplified hole calculator
		// setHoleCalculator(new SimpleHoleCalculator(0.9457));

		setBoreSectionCalculator(new SimpleBoreSectionCalculator());
	}

	@Override
	public boolean isCompatible(Instrument aInstrument)
	{
		return aInstrument != null && aInstrument.getMouthpiece() != null
				&& aInstrument.getMouthpiece().getFipple() != null;
	}

}
