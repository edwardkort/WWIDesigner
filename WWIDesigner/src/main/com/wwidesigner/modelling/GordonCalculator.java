package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.GordonBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.GordonFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.GordonHoleCalculator;
import com.wwidesigner.geometry.calculation.GordonTerminationCalculator;

public class GordonCalculator extends GordonInstrumentCalculator
{

	public GordonCalculator(Instrument instrument)
	{
		super(instrument);
		
		setMouthpieceCalculator(new GordonFippleMouthpieceCalculator());
		setTerminationCalculator(new GordonTerminationCalculator());
		setHoleCalculator(new GordonHoleCalculator());
		setBoreSectionCalculator(new GordonBoreSectionCalculator());
	}

}
