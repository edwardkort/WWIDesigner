/**
 * 
 */
package com.wwidesigner.geometry;

import java.lang.reflect.Constructor;

/**
 * @author kort
 * 
 */
public abstract class InstrumentConfigurator
{
	protected Instrument instrument;
	protected InstrumentCalculator instrumentCalculator;
	protected MouthpieceCalculator mouthpieceCalculator;
	protected TerminationCalculator terminationCalculator;
	protected Class<? extends HoleCalculator> holeCalculatorClass;
	protected Class<? extends BoreSectionCalculator> boreSectionCalculatorClass;

	public void configureInstrument(Instrument instrument)
	{
		this.instrument = instrument;

		configureInstrument();

		configureMouthpiece();

		configureHoles();

		configureTermination();

		setBoreSectionCalculator();
	}

	public void configureBoreSectionCalculator(BoreSection section)
	{
		try
		{
			Constructor<? extends BoreSectionCalculator> constructor = boreSectionCalculatorClass
					.getConstructor(BoreSection.class);
			BoreSectionCalculator calculator = constructor.newInstance(section);
			section.setBoreSectionCalculator(calculator);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	protected abstract void setInstrumentCalculator();

	protected abstract void setMouthpieceCalculator();

	protected abstract void setTerminationCalculator();

	protected abstract void setHoleCalculator();

	protected abstract void setBoreSectionCalculator();

	protected void configureInstrument()
	{
		setInstrumentCalculator();
		instrument.setCalculator(instrumentCalculator);
	}

	protected void configureMouthpiece()
	{
		setMouthpieceCalculator();
		instrument.getMouthpiece().setCalculator(mouthpieceCalculator);
	}

	protected void configureHoles()
	{
		setHoleCalculator();

		try
		{
			for (Hole currentHole : instrument.getHole())
			{
				Constructor<? extends HoleCalculator> constructor = holeCalculatorClass
						.getConstructor(Hole.class);
				HoleCalculator holeCalculator = constructor
						.newInstance(currentHole);
				currentHole.setCalculator(holeCalculator);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void configureTermination()
	{
		setTerminationCalculator();
		instrument.getTermination().setCalculator(terminationCalculator);
	}
}
