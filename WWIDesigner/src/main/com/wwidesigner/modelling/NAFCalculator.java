package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultFippleMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.ThickFlangedOpenEndCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class NAFCalculator extends DefaultInstrumentCalculator
{
	public NAFCalculator(Instrument instrument, PhysicalParameters physicalParams)
	{
		super(instrument, physicalParams);

		setMouthpieceCalculator(new DefaultFippleMouthpieceCalculator());
		setTerminationCalculator(new ThickFlangedOpenEndCalculator());
		setHoleCalculator(new DefaultHoleCalculator(0.9427));
		setBoreSectionCalculator(new SimpleBoreSectionCalculator());
	}

	public NAFCalculator()
	{
		super();
		
		setMouthpieceCalculator(new DefaultFippleMouthpieceCalculator());
		setTerminationCalculator(new ThickFlangedOpenEndCalculator());
		setHoleCalculator(new DefaultHoleCalculator(0.9427));
		setBoreSectionCalculator(new SimpleBoreSectionCalculator());
	}
}

