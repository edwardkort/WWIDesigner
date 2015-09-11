/**
 * 
 */
package com.wwidesigner.modelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import org.junit.Test;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class NAFTuningTest
{

	private static String instrumentFile_NAF = "com/wwidesigner/modelling/example/NAF_D_minor_cherry_actual_geometry.xml";
	private static String tuningFile_NAF = "com/wwidesigner/modelling/example/NAF_D_minor_cherry_actual_tuning.xml";

	@Test
	public void testNafTuningWithNAF()
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
		try
		{
			tuner.setInstrument(instrumentFile_NAF, true);
			tuner.setTuning(tuningFile_NAF, true);
			tuner.setParams(new PhysicalParameters(72.0, TemperatureType.F));
			tuner.setCalculator(new NAFCalculator());

			checkTuning(tuner);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected void checkTuning(SimpleInstrumentTuner tuner)
	{
		Tuning predicted = tuner.getPredictedTuning();
		List<Fingering> tgtFingering  = tuner.getTuning().getFingering();
		List<Fingering> predFingering = predicted.getFingering();
		Note tgtNote;
		Note predNote;

		for ( int i = 0; i < tgtFingering.size(); ++ i )
		{
			tgtNote  = tgtFingering.get(i).getNote();
			predNote = predFingering.get(i).getNote();
			assertEquals(tgtNote.getName() + " tuning incorrect",
					Note.cents(tgtNote.getFrequency(), predNote.getFrequency()), 0.f, 15.);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
		try
		{
			tuner.setInstrument(instrumentFile_NAF, true);
			tuner.setTuning(tuningFile_NAF, true);
			tuner.setParams(new PhysicalParameters(72.0, TemperatureType.F));


			tuner.setCalculator(new NAFCalculator());
			tuner.showTuning("Cherry D NAF, NAF Calculator");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
