package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.SimpleBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.SimpleReedMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.ThickFlangedOpenEndCalculator;
import com.wwidesigner.util.PhysicalParameters;

public class SimpleReedCalculator extends DefaultInstrumentCalculator
{

	public SimpleReedCalculator(Instrument instrument,
			PhysicalParameters physicalParams)
	{
		super(instrument, new SimpleReedMouthpieceCalculator(),
				new ThickFlangedOpenEndCalculator(),
				new DefaultHoleCalculator(),
				new SimpleBoreSectionCalculator(), physicalParams);
	}

	public SimpleReedCalculator()
	{
		super(new SimpleReedMouthpieceCalculator(),
				new ThickFlangedOpenEndCalculator(),
				new DefaultHoleCalculator(), new SimpleBoreSectionCalculator());
	}
}
