package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.FlangedEndCalculator;
import com.wwidesigner.geometry.calculation.SimpleFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.WhistleHoleCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class WhistleCalculator extends DefaultInstrumentCalculator
{

	public WhistleCalculator(Instrument instrument, PhysicalParameters physicalParams)
	{
		super(instrument, new SimpleFippleMouthpieceCalculator(),
				new FlangedEndCalculator(),
				new WhistleHoleCalculator(), new DefaultBoreSectionCalculator(),
				physicalParams);
	}
}
