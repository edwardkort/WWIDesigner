package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.InstrumentConfigurator;

public class SimpleReedConfigurator extends InstrumentConfigurator
{

	@Override
	protected void setMouthpieceCalculator()
	{
		this.mouthpieceCalculator = new NoOpReedMouthpieceCalculator(
				instrument.getMouthpiece());
	}

	@Override
	protected void setTerminationCalculator()
	{
		this.terminationCalculator = new ThickFlangedOpenEndCalculator(
				instrument.getTermination());

	}

	@Override
	protected void setHoleCalculator()
	{
		this.holeCalculatorClass = DefaultHoleCalculator.class;
	}

	@Override
	protected void setBoreSectionCalculator()
	{
		this.boreSectionCalculatorClass = DefaultBoreSectionCalculator.class;
	}

	@Override
	protected void setInstrumentCalculator()
	{
		this.instrumentCalculator = new DefaultReflectanceInstrumentCalculator(
				instrument);
	}

}
