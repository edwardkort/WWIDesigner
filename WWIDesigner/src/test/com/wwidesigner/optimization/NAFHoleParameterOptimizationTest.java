package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

public class NAFHoleParameterOptimizationTest extends AbstractOptimizationTest
{
	private void doSetup(String[] args) throws Exception
	{
		setInputInstrumentXML(args[0]);
		setInputTuningXML(args[1]);
		setParams(new PhysicalParameters(22.22, TemperatureType.C));
		setCalculator(new NAFCalculator());
		setup();
		evaluator = new ReactanceEvaluator(calculator);
	}

	private void resetCalculator()
	{
		DefaultHoleCalculator defHoleCalc = (DefaultHoleCalculator) calculator
				.getHoleCalculator();
		defHoleCalc.setFingerAdjustment(0.0d);
		defHoleCalc.setFudgeFactor(1.0d);
	}

	private void testFingerAdjOptimizer(String[] args) throws Exception
	{
		doSetup(args);
		resetCalculator();
		setLowerBound(new double[] { 0.0, 0.0 });
		setUpperBound(new double[] { 1.0, 1.0 });
		objective = new FippleFactorFingerAdjObjectiveFunction(calculator,
				tuning, evaluator);
		Instrument optimizedInstrument = doInstrumentOptimization(
				"Fipple factor and finger adjustment optimization");

		double fippleFactor = optimizedInstrument.getMouthpiece().getFipple()
				.getFippleFactor();
		System.out.println("Fipple factor: " + fippleFactor);
		double fingerAdj = ((DefaultHoleCalculator) calculator
				.getHoleCalculator()).getFingerAdjustment();
		System.out.println("Finger adjustment: " + fingerAdj);
		double holeSizeMult = ((DefaultHoleCalculator) calculator
				.getHoleCalculator()).getFudgeFactor();
		System.out.println("Hole-size multiplier: " + holeSizeMult);
	}

	private void testHoleSizeMultiplierOptimizer(String[] args) throws Exception
	{
		doSetup(args);
		resetCalculator();
		setLowerBound(new double[] { 0.0, 0.0 });
		setUpperBound(new double[] { 1.0, 1.0 });
		objective = new FippleFactorHoleSizeMultObjectiveFunction(calculator,
				tuning, evaluator);
		Instrument optimizedInstrument = doInstrumentOptimization(
				"Fipple factor and hole-size multiplier optimization");

		double fippleFactor = optimizedInstrument.getMouthpiece().getFipple()
				.getFippleFactor();
		System.out.println("Fipple factor: " + fippleFactor);
		double fingerAdj = ((DefaultHoleCalculator) calculator
				.getHoleCalculator()).getFingerAdjustment();
		System.out.println("Finger adjustment: " + fingerAdj);
		double holeSizeMult = ((DefaultHoleCalculator) calculator
				.getHoleCalculator()).getFudgeFactor();
		System.out.println("Hole-size multiplier: " + holeSizeMult);
	}

	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println(
					"Usage: NAFHoleParameterOptimizationTest <input instrument file> <input tuning file>");
			return;
		}

		NAFHoleParameterOptimizationTest test = new NAFHoleParameterOptimizationTest();
		try
		{
			test.testFingerAdjOptimizer(args);
			test.testHoleSizeMultiplierOptimizer(args);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return;
		}

	}

}
