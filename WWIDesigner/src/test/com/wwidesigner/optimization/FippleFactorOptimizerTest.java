/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class FippleFactorOptimizerTest extends AbstractOptimizationTest
{
	@Test
	public final void testNoHoleOptimization()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/NoHoleNAF1.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml");
			setParams(new PhysicalParameters(22.22, TemperatureType.C));
			setCalculator(new NAFCalculator());
			setup();
			setLowerBound(new double[] { 0.2 });
			setUpperBound(new double[] { 1.5 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new FippleFactorObjectiveFunction(calculator, tuning, evaluator);

			Instrument optimizedInstrument = doInstrumentOptimization("No-hole");

			// Test fipple factor
			double fippleFactor = optimizedInstrument.getMouthpiece()
					.getFipple().getFippleFactor();
			assertEquals("Fipple factor incorrect", 0.80, fippleFactor, 0.01);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		FippleFactorOptimizerTest test = new FippleFactorOptimizerTest();
		test.testNoHoleOptimization();
	}
}
