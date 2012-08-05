/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.InstrumentConfigurator;

/**
 * @author kort
 * 
 */
public class SimpleFippleMouthpieceConfigurator extends InstrumentConfigurator
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentConfigurator#setMouthpieceCalculator()
	 */
	@Override
	protected void setMouthpieceCalculator()
	{
		this.mouthpieceCalculator = new SimpleFippleMouthpieceCalculator(
				instrument.getMouthpiece());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.InstrumentConfigurator#setTerminationCalculator
	 * ()
	 */
	@Override
	protected void setTerminationCalculator()
	{
		this.terminationCalculator = new IdealOpenEndCalculator(
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
