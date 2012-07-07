/**
 * 
 */
package com.wwidesigner.geometry;

/**
 * @author kort
 * 
 */
public abstract class InstrumentConfigurator
{
	protected Instrument instrument;
	protected MouthpieceCalculator mouthpieceCalculator;
	protected TerminationCalculator terminationCalculator;

	public void configureInstrument(Instrument instrument)
	{
		this.instrument = instrument;
		setMouthpieceCalculator();
		instrument.getMouthpiece().setCalculator(mouthpieceCalculator);
		
		setTerminationCalculator();
		instrument.getTermination().setCalculator(terminationCalculator);
	}

	protected abstract void setMouthpieceCalculator();

	protected abstract void setTerminationCalculator();
}
