package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.IdealOpenEndCalculator;
import com.wwidesigner.geometry.calculation.NoOpMouthpieceCalculator;

public class SimpleTestCalculator extends DefaultInstrumentCalculator
{

	public SimpleTestCalculator(Instrument instrument)
	{
		super(instrument, new NoOpMouthpieceCalculator(),
				new IdealOpenEndCalculator(),
				new DefaultHoleCalculator(), new DefaultBoreSectionCalculator());
	}
}
