package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.GordonBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.GordonFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.GordonHoleCalculator;
import com.wwidesigner.geometry.calculation.IdealOpenEndCalculator;

public class GordonCalculator extends DefaultInstrumentCalculator
{

	public GordonCalculator(Instrument instrument)
	{
		super(instrument, new GordonFippleMouthpieceCalculator(),
				new IdealOpenEndCalculator(),
				new GordonHoleCalculator(), new GordonBoreSectionCalculator());
	}
}
