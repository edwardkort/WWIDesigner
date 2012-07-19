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
		this.terminationCalculator = new ThickFlangedOpenEndCalculator(
				instrument.getTermination());
	}

}
