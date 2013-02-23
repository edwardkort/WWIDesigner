package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.optimization.Constraint.ConstraintType;

public class ConstraintsTest extends AbstractOptimizationTest
{
	@Test
	public final void testGetFippleConstraint()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/NoHoleNAF1.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml");
			setCalculator(new NAFCalculator());
			setup();
			objective = new FippleFactorObjectiveFunction(calculator, tuning,
					evaluator);
			Constraints constraints = objective.getConstraints();
			String category = "Mouthpiece";

			assertEquals("Number of constraints incorrect",
					objective.getNrDimensions(),
					constraints.getNumberOfConstraints(category));

			Constraint constraint = constraints.getConstraint(category, 0);
			assertEquals("Constraint name incorrect", "Fipple factor",
					constraint.getDisplayName());

			assertEquals("Constraint dimensionality incorrect",
					ConstraintType.DIMENSIONLESS, constraint.getType());

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testGetHoleSizeConstraint()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/6HoleNAF1.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/6HoleNAF1Tuning.xml");
			setCalculator(new NAFCalculator());
			setup();
			objective = new HoleSizeObjectiveFunction(calculator, tuning,
					evaluator);
			Constraints constraints = objective.getConstraints();
			String category = "Hole size";

			assertEquals("Number of constraints incorrect",
					objective.getNrDimensions(),
					constraints.getNumberOfConstraints(category));

			// Test hole name that is not set in Instrument.xml
			Constraint constraint = constraints.getConstraint(category, 5);
			assertEquals("Constraint name incorrect",
					"Hole 1 (bottom) diameter", constraint.getDisplayName());

			assertEquals("Constraint dimensionality incorrect",
					ConstraintType.DIMENSIONAL, constraint.getType());

			// Test hole name that is set in Instrument.xml
			constraint = constraints.getConstraint(category, 0);
			assertEquals("Constraint name incorrect", "Hole T (top) diameter",
					constraint.getDisplayName());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testGetHolePositionConstraint()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/6HoleNAF1.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/6HoleNAF1Tuning.xml");
			setCalculator(new NAFCalculator());
			setup();
			objective = new HolePositionObjectiveFunction(calculator, tuning,
					evaluator);
			Constraints constraints = objective.getConstraints();
			String category = "Hole position";

			assertEquals("Number of constraints incorrect",
					objective.getNrDimensions(),
					constraints.getNumberOfConstraints(category));

			// Test bottom hole with no name set in Instrument.xml
			Constraint constraint = constraints.getConstraint(category, 6);
			assertEquals("Constraint name incorrect",
					"Hole 1 (bottom) to bore end distance",
					constraint.getDisplayName());

			assertEquals("Constraint dimensionality incorrect",
					ConstraintType.DIMENSIONAL, constraint.getType());

			// Test top hole with name set in Instrument.xml
			constraint = constraints.getConstraint(category, 1);
			assertEquals("Constraint name incorrect",
					"Hole T (top) to Hole 5 distance",
					constraint.getDisplayName());

			// Test bore length
			constraint = constraints.getConstraint(category, 0);
			assertEquals("Constraint name incorrect", "Bore length",
					constraint.getDisplayName());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void test2GroupingConstraints()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
			setCalculator(new NAFCalculator());
			setup();
			int[][] holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			objective = new HoleGroupPositionObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			Constraints constraints = objective.getConstraints();
			String category = "Hole position";

			assertEquals("Number of constraints incorrect",
					objective.getNrDimensions(),
					constraints.getNumberOfConstraints(category));

			assertEquals("First constraint incorrect", "Bore length",
					constraints.getConstraint(category, 0).getDisplayName());

			assertEquals("First group constraint incorrect",
					"Group 1 (Hole 7 (top), Hole 6, Hole 5) spacing",
					constraints.getConstraint(category, 1).getDisplayName());

			assertEquals("First inter-group constraint incorrect",
					"Hole 5 to Hole 4 distance",
					constraints.getConstraint(category, 2).getDisplayName());

			assertEquals(
					"Last distance constraint incorrect",
					"Hole 1 (bottom) to bore end distance",
					constraints.getConstraint(category,
							constraints.getNumberOfConstraints(category) - 1)
							.getDisplayName());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
}
