/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.InstrumentConfigurator;
import com.wwidesigner.geometry.Termination;

/**
 * @author kort
 * 
 */
public class WhistleConfigurator extends InstrumentConfigurator
{

	@Override
	protected void setMouthpieceCalculator()
	{
		this.mouthpieceCalculator = new SimpleFippleMouthpieceCalculator(
				instrument.getMouthpiece());
	}

	@Override
	protected void setTerminationCalculator()
	{
		Termination termination = instrument.getTermination();
		this.terminationCalculator = new FlangedEndCalculator(termination);
	}

	@Override
	protected void setHoleCalculator()
	{
		this.holeCalculatorClass = WhistleHoleCalculator.class;
	}

	@Override
	protected void setBoreSectionCalculator()
	{
		this.boreSectionCalculatorClass = DefaultBoreSectionCalculator.class;
	}

	@Override
	protected void setInstrumentCalculator()
	{
		this.instrumentCalculator = new DefaultImpedanceInstrumentCalculator(
				instrument);
	}
}
