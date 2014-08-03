package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.IdealOpenEndCalculator;
import com.wwidesigner.geometry.calculation.NoOpMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class SimpleTestCalculator extends DefaultInstrumentCalculator
{

	public SimpleTestCalculator(Instrument instrument, PhysicalParameters physicalParams)
	{
		super(instrument, new NoOpMouthpieceCalculator(),
				new IdealOpenEndCalculator(),
				new DefaultHoleCalculator(), new SimpleBoreSectionCalculator(),
				physicalParams);
	}
}
