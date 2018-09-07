package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.IdealOpenEndCalculator;
import com.wwidesigner.geometry.calculation.MouthpieceCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class SimpleTestCalculator extends DefaultInstrumentCalculator
{

	public SimpleTestCalculator(Instrument aInstrument, PhysicalParameters physicalParams)
	{
		super(aInstrument, new MouthpieceCalculator(),
				new IdealOpenEndCalculator(),
				new DefaultHoleCalculator(), new SimpleBoreSectionCalculator(),
				physicalParams);
	}
}
