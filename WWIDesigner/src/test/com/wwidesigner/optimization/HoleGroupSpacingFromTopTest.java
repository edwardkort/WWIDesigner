package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.CentDeviationEvaluator;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;
import com.wwidesigner.optimization.gui.ConstraintsDialog;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

public class HoleGroupSpacingFromTopTest extends AbstractOptimizationTest
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
	 * Test that 3 objective functions (HoleFromTop, HoleGroup, and
	 * HoleGroupFromTop) give the same answer with the simplest scenario that
	 * uses no grouping and no hole-from-top ratio constraints:<br>
	 * 7 holes in 7 hole groups<br>
	 * minimum top-hole ratio of 0.
	 */
	@Test
	public void testNoGroupingNoTop()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/G7HoleNAF.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/G7HoleNAFTuning.xml";
			// Hole from top, no grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0203, 0.0005, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.002, 0.002 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.014, 0.014, 0.014, 0.014, 0.014, 0.008, 0.008 };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("HoleFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength1 = getBoreLength(optimizedInstrument);
			double topHolePosition1 = getTopHolePosition(optimizedInstrument);

			// Hole from bottom, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0203, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0005, 0.018, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.002, 0.002 };
			upperBound = new double[] { 0.5, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.20, 0.014, 0.014, 0.014, 0.014, 0.014, 0.008,
					0.008 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
					{ 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
			objective = new HoleGroupObjectiveFunction(calculator, tuning,
					evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("HoleGroupObjectiveFunction");
			// Get bore length and top hole position
			double boreLength2 = getBoreLength(optimizedInstrument);
			double topHolePosition2 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0203, 0.0005, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.002, 0.002 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.014, 0.014, 0.014, 0.014, 0.014, 0.008, 0.008 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
					{ 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("HoleGroupFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength3 = getBoreLength(optimizedInstrument);
			double topHolePosition3 = getTopHolePosition(optimizedInstrument);

			assertEquals(
					"HoleFromTop and HoleGroupFromTop yield different bore lengths",
					boreLength1, boreLength3, 0.01);
			assertEquals(
					"HoleFromTop and HoleGroupFromTop yield different Hole 1 positions",
					topHolePosition1, topHolePosition3, 0.01);
			assertEquals(
					"HoleGroup and HoleGroupFromTop yield different bore lengths",
					boreLength2, boreLength3, 0.01);
			assertEquals(
					"HoleHoleGroup and HoleGroupFromTop yield different Hole 1 positions",
					topHolePosition2, topHolePosition3, 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test that 2 grouping objective functions (HoleGroup, and
	 * HoleGroupFromTop) give the same answer with the simplest scenario that
	 * uses grouping and no hole-from-top ratio constraints:<br>
	 * 7 holes in 3 hole groups<br>
	 * minimum top-hole ratio of 0.
	 */
	@Test
	public void testGroupingNoTop()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/G7HoleNAF.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/G7HoleNAFTuning.xml";
			// Hole from bottom, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0203, 0.0203, 0.0203, 0.0005,
					0.012, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002 };
			upperBound = new double[] { 0.5, 0.05, 0.1, 0.05, 0.003, 0.15,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.008, 0.008 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
			objective = new HoleGroupObjectiveFunction(calculator, tuning,
					evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("HoleGroupObjectiveFunction");
			// Get bore length and top hole position
			double boreLength2 = getBoreLength(optimizedInstrument);
			double topHolePosition2 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.0005, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.003,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.008, 0.008 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("HoleGroupFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength3 = getBoreLength(optimizedInstrument);
			double topHolePosition3 = getTopHolePosition(optimizedInstrument);

			assertEquals(
					"HoleGroup and HoleGroupFromTop yield different bore lengths",
					boreLength2, boreLength3, 0.01);
			assertEquals(
					"HoleHoleGroup and HoleGroupFromTop yield different Hole 1 positions",
					topHolePosition2, topHolePosition3, 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Test that 2 objective functions (HoleFromTop and HoleGroupFromTop) give
	 * the same answer with the simplest scenario that uses no grouping and
	 * hole-from-top ratio constraints:<br>
	 * 7 holes in 7 hole groups<br>
	 * minimum top-hole ratio of 0.25<br>
	 * Test that this answer is different than that of HoleGroup.
	 */
	@Test
	public void testNoGroupingWithTop()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/G7HoleNAF.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/G7HoleNAFTuning.xml";
			// Hole from top, no grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			double minTopRatio = 0.25;
			lowerBound = new double[] { 0.2, minTopRatio, 0.0203, 0.0203,
					0.0203, 0.0203, 0.0203, 0.0005, 0.002, 0.002, 0.002, 0.002,
					0.002, 0.002, 0.002 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.014, 0.014, 0.014, 0.014, 0.014, 0.008, 0.008 };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("HoleFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength1 = getBoreLength(optimizedInstrument);
			double topHolePosition1 = getTopHolePosition(optimizedInstrument);

			// Hole from bottom, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.0203, 0.0203, 0.0203, 0.0203,
					0.0203, 0.0005, 0.018, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.002, 0.002 };
			upperBound = new double[] { 0.5, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.20, 0.014, 0.014, 0.014, 0.014, 0.014, 0.008,
					0.008 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
					{ 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
			objective = new HoleGroupObjectiveFunction(calculator, tuning,
					evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("HoleGroupObjectiveFunction");
			// Get bore length and top hole position
			double boreLength2 = getBoreLength(optimizedInstrument);
			double topHolePosition2 = getTopHolePosition(optimizedInstrument);

			// Hole from top, grouping optimizer
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, minTopRatio, 0.0203, 0.0203,
					0.0203, 0.0203, 0.0203, 0.0005, 0.002, 0.002, 0.002, 0.002,
					0.002, 0.002, 0.002 };
			upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.014, 0.014, 0.014, 0.014, 0.014, 0.008, 0.008 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
					{ 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			optimizedInstrument = doInstrumentOptimization("HoleGroupFromTopObjectiveFunction, top ratio 0");
			// Get bore length and top hole position
			double boreLength3 = getBoreLength(optimizedInstrument);
			double topHolePosition3 = getTopHolePosition(optimizedInstrument);

			assertEquals(
					"HoleFromTop and HoleGroupFromTop yield different bore lengths",
					boreLength1, boreLength3, 0.01);
			assertEquals(
					"HoleFromTop and HoleGroupFromTop yield different Hole 1 positions",
					topHolePosition1, topHolePosition3, 0.01);
			assertEquals(
					"HoleGroup and HoleGroupFromTop yield different bore lengths",
					boreLength2, boreLength3, 0.1);
			assertNotEquals(
					"HoleHoleGroup and HoleGroupFromTop yield same Hole 1 positions",
					topHolePosition2, topHolePosition3, 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * Checks that constraints are built properly by
	 * HoleGroupPositionFromTopObjectiveFunction
	 */
	@Test
	public void testConstraints()
	{
		try
		{
			double minLength = 0.2;
			double maxLength = 0.7;
			double minRatio = 0.;
			double maxRatio = 1.;
			double minSpacing1 = 0.02;
			double maxSpacing1 = 0.05;
			double minSpacing4 = 0.0005;
			double maxSpacing4 = 0.003;
			double minHoleSize5 = 0.006;
			double maxHoleSize5 = 0.018;

			inputInstrumentXML = "com/wwidesigner/optimization/example/G7HoleNAF.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/G7HoleNAFTuning.xml";
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { minLength, minRatio, minSpacing1, 0.03,
					0.04, minSpacing4, 0.002, 0.003, 0.004, 0.005,
					minHoleSize5, 0.007, 0.008 };
			upperBound = new double[] { maxLength, maxRatio, maxSpacing1, 0.06,
					0.07, maxSpacing4, 0.014, 0.015, 0.016, 0.017,
					maxHoleSize5, 0.008, 0.009 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective
					.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);
			objective.setLowerBounds(lowerBound);
			objective.setUpperBounds(upperBound);

			Constraints constraints = objective.getConstraints();
			constraints
					.setConstraintsName("Broad-span bounds, 3 hole groups, from top");

			String constCatPos = "Hole position";
			String constCatSize = "Hole size";
			assertEquals("Min bore length incorrect", minLength, constraints
					.getConstraint(constCatPos, 0).getLowerBound(), 0.00001);
			assertEquals("Max bore length incorrect", maxLength, constraints
					.getConstraint(constCatPos, 0).getUpperBound(), 0.00001);
			assertEquals("Min top ratio incorrect", minRatio, constraints
					.getConstraint(constCatPos, 1).getLowerBound(), 0.00001);
			assertEquals("Max top ratio incorrect", maxRatio, constraints
					.getConstraint(constCatPos, 1).getUpperBound(), 0.00001);
			assertEquals("Min Group 1 spacing incorrect", minSpacing1,
					constraints.getConstraint(constCatPos, 2).getLowerBound(),
					0.00001);
			assertEquals("Max Group 1 spacing incorrect", maxSpacing1,
					constraints.getConstraint(constCatPos, 2).getUpperBound(),
					0.00001);
			assertEquals("Min Hole 6 to 7 distance incorrect", minSpacing4,
					constraints.getConstraint(constCatPos, 5).getLowerBound(),
					0.00001);
			assertEquals("Max Hole 6 to 7 distance incorrect", maxSpacing4,
					constraints.getConstraint(constCatPos, 5).getUpperBound(),
					0.00001);
			assertEquals("Min Hole 5 size incorrect", minHoleSize5, constraints
					.getConstraint(constCatSize, 4).getLowerBound(), 0.00001);
			assertEquals("Max Hole 5 size incorrect", maxHoleSize5, constraints
					.getConstraint(constCatSize, 4).getUpperBound(), 0.00001);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public void showConstraints()
	{
		try
		{
			double minLength = 0.2;
			double maxLength = 0.7;
			double minRatio = 0.;
			double maxRatio = 1.;
			double minSpacing1 = 0.02;
			double maxSpacing1 = 0.05;
			double minSpacing4 = 0.0005;
			double maxSpacing4 = 0.003;
			double minHoleSize5 = 0.006;
			double maxHoleSize5 = 0.018;

			inputInstrumentXML = "com/wwidesigner/optimization/example/G7HoleNAF.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/G7HoleNAFTuning.xml";
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { minLength, minRatio, minSpacing1, 0.03,
					0.04, minSpacing4, 0.002, 0.003, 0.004, 0.005,
					minHoleSize5, 0.007, 0.008 };
			upperBound = new double[] { maxLength, maxRatio, maxSpacing1, 0.06,
					0.07, maxSpacing4, 0.014, 0.015, 0.016, 0.017,
					maxHoleSize5, 0.008, 0.009 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setLowerBounds(lowerBound);
			objective.setUpperBounds(upperBound);

			Constraints constraints = objective.getConstraints();
			constraints
					.setConstraintsName("Broad-span bounds, 3 hole groups, from top");

			ConstraintsDialog dialog = new ConstraintsDialog(constraints);
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
		}
	}

	public void showConstraints6Hole()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/6HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/6HoleNAF1Tuning.xml";
			setCalculator(new NAFCalculator());
			setup();
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
			lowerBound = new double[] { 0.2, 0.0, 0.0203, 0.0203, 0.0203,
					0.002, 0.003, 0.003, 0.003, 0.003, 0.003 };
			upperBound = new double[] { 0.7, 1.0, 0.038, 0.07, 0.038, 0.0102,
					0.0102, 0.010, 0.010, 0.010, 0.012 };
			evaluator = new CentDeviationEvaluator(calculator);
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			objective.setLowerBounds(lowerBound);
			objective.setUpperBounds(upperBound);

			Constraints constraints = objective.getConstraints();
			constraints
					.setConstraintsName("Broad-span bounds, 2 hole groups, from top");

			ConstraintsDialog dialog = new ConstraintsDialog(constraints);
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
			System.out.print(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		HoleGroupSpacingFromTopTest test = new HoleGroupSpacingFromTopTest();
		// test.showConstraints();
		test.showConstraints6Hole();
	}

}
