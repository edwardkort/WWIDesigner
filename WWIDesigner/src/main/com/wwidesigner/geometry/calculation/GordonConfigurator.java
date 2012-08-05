/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.InstrumentConfigurator;

/**
 * @author kort
 * 
 */
public class GordonConfigurator extends InstrumentConfigurator
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
		this.mouthpieceCalculator = new GordonFippleMouthpieceCalculator(
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
		this.holeCalculatorClass = GordonHoleCalculator.class;
	}

	@Override
	protected void setBoreSectionCalculator()
	{
		this.boreSectionCalculatorClass = GordonBoreSectionCalculator.class;
	}

	@Override
	protected void setInstrumentCalculator()
	{
		this.instrumentCalculator = new DefaultReflectanceInstrumentCalculator(
				instrument);
	}

}
