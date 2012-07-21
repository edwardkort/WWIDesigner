/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;

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
		
		setHoleCalculator();

		setTerminationCalculator();
		instrument.getTermination().setCalculator(terminationCalculator);
	}

	protected abstract void setMouthpieceCalculator();

	protected abstract void setTerminationCalculator();

	/**
	 * Override this method in concrete configurator to set a different
	 * HoleCalculator
	 */
	protected void setHoleCalculator()
	{
		for (Hole currentHole : instrument.getHole())
		{
			HoleCalculator holeCalculator = new DefaultHoleCalculator(
					currentHole);
			currentHole.setCalculator(holeCalculator);
		}
	}
}
