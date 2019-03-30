/**
 * 
 */
package com.wwidesigner.modelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class TaborPipeTuningTest
{

	private static String instrumentFile = "com/wwidesigner/modelling/example/TaborPipe.xml";
	private static String tuningFile = "com/wwidesigner/modelling/example/A4-TaborPipe.xml";

	@Test
	public void testInstrumentTuning()
	{
		InstrumentTuner tuner = new LinearVInstrumentTuner(4);
		double temperature = 27.0;
		PhysicalParameters params = new PhysicalParameters(temperature, TemperatureType.C,
				98.4, 100, 0.04);
		try
		{
			tuner.setInstrument(instrumentFile, true);
			tuner.setTuning(tuningFile, true);
			tuner.setParams(params);
			tuner.setCalculator(new WhistleCalculator());

			checkTuning(tuner);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected static void checkTuning(InstrumentTuner tuner)
	{
		Tuning predicted = tuner.getPredictedTuning();
		List<Fingering> tgtFingering  = tuner.getTuning().getFingering();
		List<Fingering> predFingering = predicted.getFingering();
		Note tgtNote;
		Note predNote;
		double tuningError;
		double allowedError;

		for ( int i = 0; i < tgtFingering.size(); ++ i )
		{
			tgtNote  = tgtFingering.get(i).getNote();
			predNote = predFingering.get(i).getNote();
			tuningError = Note.cents(tgtNote.getFrequency(), predNote.getFrequency());
			if (i == 0)
			{
				allowedError = 15.0;
			}
			else if (i == 5 || i == 6)
			{
				allowedError = 15.0;
			}
			else
			{
				allowedError = 10.0;
			}
			System.out.println("Note " + (i + 1) + " " + tgtNote.getName() + " tuning error " + tuningError);
			
			assertEquals("Note " + (i + 1) + " " + tgtNote.getName() + " tuning incorrect", 0.0f,
					tuningError, allowedError);
		}
	}

	@Test
	public void testNames()
	{
		InstrumentTuner tuner = new LinearVInstrumentTuner(4);
		try
		{
			tuner.setInstrument(instrumentFile, true);
			Instrument pipe = tuner.getInstrument();
			assertEquals("Wrong bore point for top of Head", 1,
					Instrument.positionIndex(pipe.getBorePoint(), "Head", false, false));
			assertEquals("Wrong bore point for bottom of Head", 3,
					Instrument.positionIndex(pipe.getBorePoint(), "Head", false, true));
			assertEquals("Wrong bore point for top of Body", 3,
					Instrument.positionIndex(pipe.getBorePoint(), "Body", false, false));
			assertEquals("Wrong bore point for bottom of Body", 5,
					Instrument.positionIndex(pipe.getBorePoint(), "Body", false, true));
			assertEquals("Wrong index for non-existent bore point", -1,
					Instrument.positionIndex(pipe.getBorePoint(), "Headjoint", false, false));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		TaborPipeTuningTest myTest = new TaborPipeTuningTest();
		myTest.testInstrumentTuning();
	}
}
