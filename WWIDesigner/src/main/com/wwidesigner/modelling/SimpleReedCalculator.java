package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.NoOpReedMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.ThickFlangedOpenEndCalculator;

public class SimpleReedCalculator extends DefaultInstrumentCalculator
{

	public SimpleReedCalculator(Instrument instrument)
	{
		super(instrument, new NoOpReedMouthpieceCalculator(),
				new ThickFlangedOpenEndCalculator(),
				new DefaultHoleCalculator(), new DefaultBoreSectionCalculator());
	}
}
