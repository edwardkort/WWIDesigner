/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.InstrumentConfigurator;

/**
 * @author kort
 * 
 */
public class SimpleTestConfigurator extends InstrumentConfigurator
{

	@Override
	protected void setMouthpieceCalculator()
	{
		this.mouthpieceCalculator = new NoOpMouthpieceCalculator(
				instrument.getMouthpiece());
	}

	@Override
	protected void setTerminationCalculator()
	{
		this.terminationCalculator = new IdealOpenEndCalculator(
				instrument.getTermination());
	}

}
