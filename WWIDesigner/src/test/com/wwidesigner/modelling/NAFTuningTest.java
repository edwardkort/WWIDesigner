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

//	private static String instrumentFile_chalumeau = "com/wwidesigner/optimization/example/chalumeau_alto_optimized.xml";
//	private static String tuningFile_chalumeau = "com/wwidesigner/optimization/example/chalumeau_alto_tuning.xml";

//	@Test
//	public void testNafTuningWithGordon()
//	{
//		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
//		try
//		{
//			tuner.setInstrument(instrumentFile_NAF, true);
//			tuner.setTuning(tuningFile_NAF, true);
//			tuner.setParams(new PhysicalParameters(72.0, TemperatureType.F));
//			tuner.setCalculator(new GordonCalculator());
//
//			checkTuning(tuner);
//		}
//		catch (Exception e)
//		{
//			fail(e.getMessage());
//		}
//	}

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
					Note.cents(tgtNote.getFrequency(), predNote.getFrequency()), 0.f, 6.);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
//		InstrumentRangeTuner whistleTuner = new InstrumentRangeTuner();
		try
		{
			tuner.setInstrument(instrumentFile_NAF, true);
			tuner.setTuning(tuningFile_NAF, true);
			tuner.setParams(new PhysicalParameters(72.0, TemperatureType.F));

//			tuner.setCalculator(new GordonCalculator());
//			tuner.showTuning("Cherry D NAF, Gordon Calculator");

			tuner.setCalculator(new NAFCalculator());
			tuner.showTuning("Cherry D NAF, NAF Calculator");

//			whistleTuner.setCalculator(new WhistleCalculator());
//			whistleTuner.showTuning("Light G NAF, Whistle Calculator");
//
//			tuner.setInstrument(instrumentFile_chalumeau, true);
//			tuner.setTuning(tuningFile_chalumeau, true);
//			tuner.setParams(new PhysicalParameters(25.0, TemperatureType.C));
//
//			tuner.setCalculator(new SimpleReedCalculator());
//			tuner.showTuning("Chalumeau, Simple Reed Calculator");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
