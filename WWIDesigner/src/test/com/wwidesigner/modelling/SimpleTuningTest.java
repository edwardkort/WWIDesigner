/**
 * 
 */
package com.wwidesigner.modelling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class SimpleTuningTest
{

	private static String instrumentFile_NAF = "com/wwidesigner/optimization/example/LightG6HoleNAF.xml";
	private static String tuningFile_NAF = "com/wwidesigner/optimization/example/LightG6HoleNAFTuning.xml";

	private static String instrumentFile_chalumeau = "com/wwidesigner/optimization/example/chalumeau_alto_optimized.xml";
	private static String tuningFile_chalumeau = "com/wwidesigner/optimization/example/chalumeau_alto_tuning.xml";

	@Test
	public void testNafTuningWithGordon()
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
		try
		{
			tuner.setInstrument(instrumentFile_NAF, true);
			tuner.setTuning(tuningFile_NAF, true);
			tuner.setParams(new PhysicalParameters(74.0, TemperatureType.F));
			tuner.setCalculator(new GordonCalculator());

			checkTuning(tuner);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void testNafTuningWithNAF()
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
		try
		{
			tuner.setInstrument(instrumentFile_NAF, true);
			tuner.setTuning(tuningFile_NAF, true);
			tuner.setParams(new PhysicalParameters(74.0, TemperatureType.F));
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
		Map<Fingering, Double> tuningMap = tuner.getTuning();
		for (Map.Entry<Fingering, Double> entry : tuningMap.entrySet())
		{
			Note note = entry.getKey().getNote();
			assertEquals(note.getName() + " tuning incorrect",
					note.getFrequency(), entry.getValue(), 20.);
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
			tuner.setParams(new PhysicalParameters(74.0, TemperatureType.F));

			tuner.setCalculator(new GordonCalculator());
			tuner.showTuning("Light G NAF, Gordon Calculator");

			tuner.setCalculator(new NAFCalculator());
			tuner.showTuning("Light G NAF, NAF Calculator");

			tuner.setCalculator(new WhistleCalculator());
			tuner.showTuning("Light G NAF, Whistle Calculator");

			tuner.setInstrument(instrumentFile_chalumeau, true);
			tuner.setTuning(tuningFile_chalumeau, true);
			tuner.setParams(new PhysicalParameters(25.0, TemperatureType.C));

			tuner.setCalculator(new SimpleReedCalculator());
			tuner.showTuning("Chalumeau, Simple Reed Calculator");
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
