package com.wwidesigner.optimization;

import java.lang.reflect.Constructor;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
//import com.wwidesigner.geometry.calculation.SimpleHoleCalculator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

public class NAFHoleParameterOptimizationTest extends AbstractOptimizationTest
{
	private void doSetup(String[] args) throws Exception
	{
		setInputInstrumentXML(args[0]);
		setInputTuningXML(args[1]);
		setParams(new PhysicalParameters(Double.parseDouble(args[2]),
				TemperatureType.F));
		setCalculator(new NAFCalculator());
		setup();
		evaluator = new ReactanceEvaluator(calculator);
	}

	private void resetCalculator(double fingerAdj, double holeMultiplier)
	{
		DefaultHoleCalculator defHoleCalc = (DefaultHoleCalculator) calculator
				.getHoleCalculator();
		defHoleCalc.setFingerAdjustment(fingerAdj);
		defHoleCalc.setHoleSizeMult(holeMultiplier);
	}

	private void setBounds()
	{
		setLowerBound(new double[] { 0.0, 0.0 });
		setUpperBound(new double[] { 1.0, 10.0 });
	}

	@SuppressWarnings("rawtypes")
	private void testFingerAdjOptimizer(String[] args) throws Exception
	{
		double startingFingerAdj = 0.0d;
		double startingMultiplier = 0.9959d;
		Class objectiveClass = FippleFactorFingerAdjObjectiveFunction.class;
		String optimizationTitle = "Fipple factor and finger adjustment optimization";
		setBounds();

		runOptimizer(args, startingFingerAdj, startingMultiplier,
				objectiveClass, optimizationTitle);
	}

	@SuppressWarnings("rawtypes")
	private void testHoleSizeMultiplierOptimizer(String[] args) throws Exception
	{
		double startingFingerAdj = 0.002863d;
		double startingMultiplier = 1.0d;
		Class objectiveClass = FippleFactorHoleSizeMultObjectiveFunction.class;
		String optimizationTitle = "Fipple factor and hole-size multiplier optimization";
		setBounds();

		runOptimizer(args, startingFingerAdj, startingMultiplier,
				objectiveClass, optimizationTitle);
	}

	@SuppressWarnings("rawtypes")
	private void testHoleSizeMultiplierOptimizerDefaultFingerAdj(String[] args)
			throws Exception
	{
		double startingFingerAdj = 0.01d;
		double startingMultiplier = 1.0d;
		Class objectiveClass = FippleFactorHoleSizeMultObjectiveFunction.class;
		String optimizationTitle = "Fipple factor and hole-size multiplier optimization, default finger adjustment";
		setBounds();

		runOptimizer(args, startingFingerAdj, startingMultiplier,
				objectiveClass, optimizationTitle);
	}

	@SuppressWarnings("rawtypes")
	private void testFingerAdjHoleSizeMultOptimizer(String[] args)
			throws Exception
	{
		double startingFingerAdj = 0.0d;
		double startingMultiplier = 1.0d;
		Class objectiveClass = FippleFactorFingerAdjHoleMultObjectiveFunction.class;
		String optimizationTitle = "Fipple factor, finger adjustment, and hole-size multiplier optimization";
		setLowerBound(new double[] { 0.0, 0.0, 0.0 });
		setUpperBound(new double[] { 1.0, 10.0, 1.0 });

		runOptimizer(args, startingFingerAdj, startingMultiplier,
				objectiveClass, optimizationTitle);
	}

	@SuppressWarnings("rawtypes")
	private void testFippleFactorOptimizer(String[] args) throws Exception
	{
		double startingFingerAdj = 0.002863d;
		double startingMultiplier = 0.9959d;
		Class objectiveClass = FippleFactorObjectiveFunction.class;
		String optimizationTitle = "Fipple factor optimization, fixed finger adjustment and hole-size multiplier";
		setLowerBound(new double[] { 0.0 });
		setUpperBound(new double[] { 1.0 });

		runOptimizer(args, startingFingerAdj, startingMultiplier,
				objectiveClass, optimizationTitle);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runOptimizer(String[] args, double startingFingerAdj,
			double startingMultiplier, Class objectiveClass,
			String optimizationTitle) throws Exception
	{
		doSetup(args);
		resetCalculator(startingFingerAdj, startingMultiplier);
		Constructor<BaseObjectiveFunction> constr = objectiveClass
				.getConstructor(InstrumentCalculator.class,
						TuningInterface.class, EvaluatorInterface.class);
		objective = constr.newInstance(calculator, tuning, evaluator);
		System.out.print("\n" + optimizationTitle);
		Instrument optimizedInstrument = doInstrumentOptimization(
				optimizationTitle);

		double fippleFactor = optimizedInstrument.getMouthpiece().getFipple()
				.getFippleFactor();
		System.out.println("Fipple factor: " + fippleFactor);
		double fingerAdj = ((DefaultHoleCalculator) calculator
				.getHoleCalculator()).getFingerAdjustment();
		System.out.println("Finger adjustment: " + fingerAdj);
		double holeSizeMult = ((DefaultHoleCalculator) calculator
				.getHoleCalculator()).getHoleSizeMult();
		System.out.println("Hole-size multiplier: " + holeSizeMult);
	}

	public static void main(String[] args)
	{
		if (args.length != 3)
		{
			System.out.println(
					"Usage: NAFHoleParameterOptimizationTest <input instrument file> <input tuning file> <tuning temp [F]");
			return;
		}

		NAFHoleParameterOptimizationTest test = new NAFHoleParameterOptimizationTest();
		try
		{
			test.testFingerAdjOptimizer(args);
			test.testHoleSizeMultiplierOptimizer(args);
			test.testHoleSizeMultiplierOptimizerDefaultFingerAdj(args);
			test.testFingerAdjHoleSizeMultOptimizer(args);
			test.testFippleFactorOptimizer(args);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			return;
		}

	}

}
