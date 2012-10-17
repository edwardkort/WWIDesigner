package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.DefaultFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.ThickFlangedOpenEndCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class NAFCalculator extends DefaultInstrumentCalculator
{
	public NAFCalculator(Instrument instrument, PhysicalParameters physicalParams)
	{
		super(instrument, physicalParams);

		setMouthpieceCalculator(new DefaultFippleMouthpieceCalculator());
		setTerminationCalculator(new ThickFlangedOpenEndCalculator());
		setHoleCalculator(new DefaultHoleCalculator(0.912));
		setBoreSectionCalculator(new DefaultBoreSectionCalculator());
	}

	public NAFCalculator()
	{
		super();
		
		setMouthpieceCalculator(new DefaultFippleMouthpieceCalculator());
		setTerminationCalculator(new ThickFlangedOpenEndCalculator());
		setHoleCalculator(new DefaultHoleCalculator(0.92));
		setBoreSectionCalculator(new DefaultBoreSectionCalculator());
	}
}

