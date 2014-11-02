package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.CentDeviationEvaluator;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

public class SingleTaperHoleGroupFromTopTest extends AbstractOptimizationTest
{
	protected int[][] holeGroups;

	@Override
	protected void setup() throws Exception
	{
		setInputInstrumentXML(inputInstrumentXML);
		setInputTuningXML(inputTuningXML);
		setParams(new PhysicalParameters(22.22, TemperatureType.C));
		super.setup();
	}

	/**
	 * Test that 4 objective functions (SingleTaperNoHoleGrouping,
	 * SingleTaperNoHoleGroupingFromTop, SingleTaperHoleGroup, and
	 * SingleTaperHoleGroupFromTop) give the same answer with the simplest
	 * scenario that uses no grouping and no hole-from-top ratio constraints:<br>
	 * 6 holes in 6 hole groups<br>
	 * minimum top-hole ratio of 0.
	 */
	@Test
	public void testNoGroupingNoTop()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/6HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/A_minor_ET_6-hole_NAF_tuning.xml";
			// Hole from bottom, no grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0203, 0.0203, 0.0203, 0.0203,
					0.0203, 0.018, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 0.05, 0.05, 0.1, 0.05, 0.05, 0.2,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperNoHoleGroupingObjectiveFunction(
					calculator, tuning, evaluator);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("SingleTaperNoHoleGroupingObjectiveFunction");
			// Get bore length and top hole position
			double boreLength1 = getBoreLength(optimizedInstrument);
			double topHolePosition1 = getTopHolePosition(optimizedInstrument);

			// Hole from top, no grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0203, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperNoHoleGroupingFromTopObjectiveFunction(
					calculator, tuning, evaluator);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("SingleTaperNoHoleGroupingFromTopObjectiveFunction, ratio 0");
			// Get bore length and top hole position
			double boreLength2 = getBoreLength(optimizedInstrument);
			double topHolePosition2 = getTopHolePosition(optimizedInstrument);

			// Hole from bottom, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0203, 0.0203, 0.0203, 0.0203,
					0.0203, 0.018, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,

					0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 0.05, 0.05, 0.1, 0.05, 0.05, 0.20,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 }, { 5 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperHoleGroupObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("SingleTaperHoleGroupObjectiveFunction");
			// Get bore length and top hole position
			double boreLength3 = getBoreLength(optimizedInstrument);
			double topHolePosition3 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0203, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 }, { 5 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperHoleGroupFromTopObjectiveFunction(
					calculator, tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("SingleTaperHoleGroupFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength4 = getBoreLength(optimizedInstrument);
			double topHolePosition4 = getTopHolePosition(optimizedInstrument);

			assertEquals(
					"SingleTaperNoHoleGrouping and SingleTaperHoleGroupFromTop yield different bore lengths",
					boreLength1, boreLength4, 0.05);
			assertEquals(
					"SingleTaperNoHoleGrouping and SingleTaperHoleGroupFromTop yield different Hole 1 positions",
					topHolePosition1, topHolePosition4, 0.05);
			assertEquals(
					"SingleTaperNoHoleGroupingFromTop and SingleTaperHoleGroupFromTop yield different bore lengths",
					boreLength2, boreLength4, 0.05);
			assertEquals(
					"SingleTaperNoHoleGroupingFromTop and SingleTaperHoleGroupFromTop yield different Hole 1 positions",
					topHolePosition2, topHolePosition4, 0.05);
			assertEquals(
					"SingleTaperHoleGroup and SingleTaperHoleGroupFromTop yield different bore lengths",
					boreLength3, boreLength4, 0.05);
			assertEquals(
					"SingleTaperHoleGroup and SingleTaperHoleGroupFromTop yield different Hole 1 positions",
					topHolePosition3, topHolePosition4, 0.05);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test that 2 grouping objective functions (SingleTaperHoleGroup, and
	 * SingleTaperHoleGroupFromTop) give the same answer with the simplest
	 * scenario that uses grouping and no hole-from-top ratio constraints:<br>
	 * 6 holes in 2 hole groups<br>
	 * minimum top-hole ratio of 0.<br>
	 * Also test that increasing the top-hole ratio gives a different answer.
	 */
	@Test
	public void testGroupingNoTop()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/6HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/A_minor_ET_6-hole_NAF_tuning.xml";
			// Hole from bottom, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0203, 0.0203, 0.0203, 0.018,
					0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 0.05, 0.1, 0.05, 0.20, 0.014,
					0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperHoleGroupObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("SingleTaperHoleGroupObjectiveFunction");
			// Get bore length and top hole position
			double boreLength1 = getBoreLength(optimizedInstrument);
			double topHolePosition1 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer, no top constraint
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.1, 0.05, 0.014,
					0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperHoleGroupFromTopObjectiveFunction(
					calculator, tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("SingleTaperHoleGroupFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength2 = getBoreLength(optimizedInstrument);
			double topHolePosition2 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer, top constraint 0.3
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.3, 0.0203, 0.0203, 0.0203,
					0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.1, 0.05, 0.014,
					0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperHoleGroupFromTopObjectiveFunction(
					calculator, tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("SingleTaperHoleGroupFromTopObjectiveFunction, top ratio 0.3");
			// Get bore length and top hole position
			double boreLength3 = getBoreLength(optimizedInstrument);
			double topHolePosition3 = getTopHolePosition(optimizedInstrument);

			assertEquals(
					"SingleTaperHoleGroup and SingleTaperHoleGroupFromTop yield different bore lengths",
					boreLength1, boreLength2, 0.05);
			assertEquals(
					"SingleTaperHoleGroup and SingleTaperHoleGroupFromTop yield different Hole 1 positions",
					topHolePosition1, topHolePosition2, 0.05);
			assertEquals(
					"SingleTaperHoleGroupFromTop (top 0.3) and SingleTaperHoleGroupFromTop (top 0.0) and HoleGroupFromTop yield different bore lengths",
					boreLength3, boreLength2, 0.2);
			assertNotEquals(
					"SingleTaperHoleGroupFromTop (top 0.3) and SingleTaperHoleGroupFromTop (top 0.0) yield different Hole 1 positions",
					topHolePosition3, topHolePosition2, 0.05);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test that 2 objective functions (SingleTaperNoHoleGroupingFromTop and
	 * SingleTaperHoleGroupFromTop) give the same answer with the simplest
	 * scenario that uses no grouping and hole-from-top ratio constraints:<br>
	 * 6 holes in 6 hole groups<br>
	 * minimum top-hole ratio of 0.3<br>
	 * Test that this answer is different than that of HoleGroup.
	 */
	@Test
	public void testNoGroupingWithTop()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/6HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/A_minor_ET_6-hole_NAF_tuning.xml";
			// Hole from top, no grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.3, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0203, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperNoHoleGroupingFromTopObjectiveFunction(
					calculator, tuning, evaluator);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("SingleTaperNoHoleGroupingFromTopObjectiveFunction, ratio 0");
			// Get bore length and top hole position
			double boreLength1 = getBoreLength(optimizedInstrument);
			double topHolePosition1 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.3, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0203, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.8, 0.2, 0.0 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.014, 1.2, 1.0, 1.0 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 }, { 5 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new SingleTaperHoleGroupFromTopObjectiveFunction(
					calculator, tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("SingleTaperHoleGroupFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength2 = getBoreLength(optimizedInstrument);
			double topHolePosition2 = getTopHolePosition(optimizedInstrument);

			assertEquals(
					"SingleTaperNoHoleGroupingFromTop and SingleTaperHoleGroupFromTop yield different bore lengths",
					boreLength1, boreLength2, 0.01);
			assertEquals(
					"SingleTaperNoHoleGroupingFromTop and SingleTaperHoleGroupFromTop yield different Hole 1 positions",
					topHolePosition1, topHolePosition2, 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		SingleTaperHoleGroupFromTopTest test = new SingleTaperHoleGroupFromTopTest();
		test.testNoGroupingWithTop();
	}

}
