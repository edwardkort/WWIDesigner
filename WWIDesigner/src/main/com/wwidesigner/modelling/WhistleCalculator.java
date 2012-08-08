package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.FlangedEndCalculator;
import com.wwidesigner.geometry.calculation.SimpleFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.WhistleHoleCalculator;

public class WhistleCalculator extends DefaultInstrumentCalculator
{

	public WhistleCalculator(Instrument instrument)
	{
		super(instrument, new SimpleFippleMouthpieceCalculator(),
				new FlangedEndCalculator(),
				new WhistleHoleCalculator(), new DefaultBoreSectionCalculator());
	}
}
