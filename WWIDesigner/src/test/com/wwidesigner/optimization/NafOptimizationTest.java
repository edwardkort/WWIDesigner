/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 * 
 */
public class NafOptimizationTest extends AbstractOptimizationTest
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
			setLowerBound(new double[] { 0.25 });
			setUpperBound(new double[] { 0.4 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new LengthObjectiveFunction(calculator, tuning,
					evaluator);

			Instrument optimizedInstrument = doInstrumentOptimization("No-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.97,
					lastPoint.getBorePosition(), 0.1);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testNoHoleTaperOptimization()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/NoHoleTaperNAF.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/NoHoleTaperNAFTuning.xml");
			setParams(new PhysicalParameters(22.22, TemperatureType.C));
			setCalculator(new NAFCalculator());
			setup();
			setLowerBound(new double[] { 0.3 });
			setUpperBound(new double[] { 0.6 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new LengthObjectiveFunction(calculator, tuning,
					evaluator);

			Instrument optimizedInstrument = doInstrumentOptimization("No-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 17.38,
					lastPoint.getBorePosition(), 0.1);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test7HoleTaperOptimization()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
			setParams(new PhysicalParameters(22.22, TemperatureType.C));
			setCalculator(new NAFCalculator());
			setup();
			setLowerBound(new double[] { 0.25, 0.03, 0.04, 0.06, 0.075, 0.09,
					0.001, 0.01, 0.003, 0.003, 0.003, 0.003, 0.003, 0.0015,
					0.0015 });
			setUpperBound(new double[] { 0.5, 0.15, 0.18, 0.21, 0.24, 0.27,
					0.3, 0.3, 0.012, 0.012, 0.012, 0.012, 0.012, 0.012, 0.012 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new HoleObjectiveFunction(calculator, tuning, evaluator);
			objective.setMaxEvaluations(30000);

			Instrument optimizedInstrument = doInstrumentOptimization("7-hole taper");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 14.75,
					lastPoint.getBorePosition(), 0.1);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test7HoleTaperOptimization2()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
			setParams(new PhysicalParameters(22.22, TemperatureType.C));
			setCalculator(new NAFCalculator());
			setup();
			setLowerBound(new double[] { 0.2, 0.014, 0.014, 0.014, 0.014,
					0.014, 0.0005, 0.05, 0.0025, 0.0025, 0.0025, 0.0025,
					0.0025, 0.0013, 0.0013 });
			setUpperBound(new double[] { 0.5, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.2, 0.013, 0.013, 0.013, 0.013, 0.013, 0.010, 0.010 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new HoleObjectiveFunction(calculator, tuning, evaluator);
			objective.setMaxEvaluations(20000);

			Instrument optimizedInstrument = doInstrumentOptimization("7-hole taper2");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 13.93,
					lastPoint.getBorePosition(), 0.1);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test1HoleOptimization()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/1HoleNAF1.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml");
			setParams(new PhysicalParameters(22.22, TemperatureType.C));
			setCalculator(new NAFCalculator());
			setup();
			setLowerBound(new double[] { 0.20, 0.25, 0.0075 });
			setUpperBound(new double[] { 0.4, 1.0, 0.010 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning, evaluator);

			Instrument optimizedInstrument = doInstrumentOptimization("One-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.46,
					lastPoint.getBorePosition(), 0.1);

			// Test hole positions
			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
					holes);

			// This hole diameter is set based on the optimizer's return value:
			// an
			// infinite number of position/hole diameter values are possible.
			assertEquals("Hole 1 diameter incorrect", 0.39, sortedHoles.get(0)
					.getDiameter(), 0.02); // 0.398

			// This hole position derives from the actual instrument AND 2 other
			// calculation algorithms.
			assertEquals("Hole 1 position incorrect", 7.5, sortedHoles.get(0)
					.getBorePosition(), 0.1); // 8.1

			double distance = lastPoint.getBorePosition()
					- sortedHoles.get(0).getBorePosition();

			System.out.println("didtance " + distance);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test6HoleOptimization()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/6HoleNAF1.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/6HoleNAF1Tuning.xml");
			setParams(new PhysicalParameters(22.22, TemperatureType.C));
			setCalculator(new NAFCalculator());
			setup();
			setLowerBound(new double[] { 0.1905, 0.25, 0.02032, 0.02032, 0.02032,
					0.02032, 0.02032, 0.002032, 0.003175, 0.003175, 0.003175, 0.003175, 0.003175 });
			setUpperBound(new double[] { 0.6985, 0.5, 0.03175, 0.03175, 0.0762, 0.03175,
					0.03175, 0.0127, 0.0127, 0.0127, 0.0127, 0.0127, 0.0127 });
			evaluator = new ReactanceEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning, evaluator);

			Instrument optimizedInstrument = doInstrumentOptimization("Six-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.059,
					lastPoint.getBorePosition(), 0.1);

			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
					holes);

			assertEquals("Hole 1 diameter incorrect", 0.340, sortedHoles.get(0)
					.getDiameter(), 0.1); // 0.308
			assertEquals("Hole 2 diameter incorrect", 0.425, sortedHoles.get(1)
					.getDiameter(), 0.1); // 0.361
			assertEquals("Hole 3 diameter incorrect", 0.416, sortedHoles.get(2)
					.getDiameter(), 0.1); // 0.352
			assertEquals("Hole 4 diameter incorrect", 0.390, sortedHoles.get(3)
					.getDiameter(), 0.1); // 0.357
			assertEquals("Hole 5 diameter incorrect", 0.407, sortedHoles.get(4)
					.getDiameter(), 0.1); // 0.379
			assertEquals("Hole 6 diameter incorrect", 0.375, sortedHoles.get(5)
					.getDiameter(), 0.1); // 0.398

			assertEquals("Hole 1 position incorrect", 2.915, sortedHoles.get(0)
					.getBorePosition(), 0.5); // 3.15
			assertEquals("Hole 2 position incorrect", 3.715, sortedHoles.get(1)
					.getBorePosition(), 0.5); // 4.0
			assertEquals("Hole 3 position incorrect", 4.515, sortedHoles.get(2)
					.getBorePosition(), 0.5); // 4.85
			assertEquals("Hole 4 position incorrect", 5.315, sortedHoles.get(3)
					.getBorePosition(), 0.5); // 5.9
			assertEquals("Hole 5 position incorrect", 6.322, sortedHoles.get(4)
					.getBorePosition(), 0.5); // 7.0
			assertEquals("Hole 6 position incorrect", 7.122, sortedHoles.get(5)
					.getBorePosition(), 0.5); // 8.1

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		NafOptimizationTest test = new NafOptimizationTest();
		test.testNoHoleOptimization();
		// test.testNoHoleTaperOptimization();
		// test.test1HoleOptimization();
		// test.test6HoleOptimization();
		// test.test7HoleTaperOptimization();
		// test.test7HoleTaperOptimization2();
	}
}
