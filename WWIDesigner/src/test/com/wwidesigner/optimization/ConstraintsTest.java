package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.optimization.Constraint.ConstraintType;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;
import com.wwidesigner.optimization.gui.ConstraintsDialog;

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
			assertEquals("Number of holes incorrect", 0,
					constraints.getNumberOfHoles());
			assertEquals("Optimizer name incorrect", "Fipple factor",
					constraints.getObjectiveDisplayName());

			String category = "Mouthpiece fipple";
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
					evaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
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

	@Test
	public void testFullGroupingConstraints()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
			setCalculator(new NAFCalculator());
			setup();
			int[][] holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			objective = new SingleTaperHoleGroupObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);
			Constraints constraints = objective.getConstraints();

			String category = "Hole position";
			assertEquals("Number of hole position constraints incorrect", 6,
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

			category = "Hole size";
			assertEquals("Number of hole size constraints incorrect", 7,
					constraints.getNumberOfConstraints(category));
			assertEquals("First hole size constraint incorrect",
					"Hole 7 (top) diameter",
					constraints.getConstraint(category, 0).getDisplayName());
			assertEquals(
					"Last hole size constraint incorrect",
					"Hole 1 (bottom) diameter",
					constraints.getConstraint(category,
							constraints.getNumberOfConstraints(category) - 1)
							.getDisplayName());

			category = "Single bore taper";
			assertEquals("Number of bore taper constraints incorrect", 3,
					constraints.getNumberOfConstraints(category));
			assertEquals("First bore taper constraint incorrect",
					"Bore diameter ratio (top/bottom)", constraints
							.getConstraint(category, 0).getDisplayName());
			assertEquals(
					"Last bore taper constraint incorrect",
					"Taper length, fraction of bore below start",
					constraints.getConstraint(category,
							constraints.getNumberOfConstraints(category) - 1)
							.getDisplayName());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public void showConstraintsView()
	{
		try
		{
			setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
			setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
			setCalculator(new NAFCalculator());
			setup();
			int[][] holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			objective = new SingleTaperHoleGroupObjectiveFunction(calculator,
					tuning, evaluator, holeGroups);

			lowerBound = new double[] { 0.2, 0.010, 0.012, 0.012, 0.012,
					0.012, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.002,
					0.5, 0.0, 0.0 };
			upperBound = new double[] { 0.7, 0.04, 0.05, 0.05, 0.1, 0.30,
					0.014, 0.014, 0.014, 0.014, 0.014, 0.015, 0.015, 2.0, 1.0,
					1.0 };
			objective.setLowerBounds(lowerBound);
			objective.setUpperBounds(upperBound);

			Constraints constraints = objective.getConstraints();
			constraints.setConstraintsName("Broad-span bounds, 2 triplets of hole groups");
			ConstraintsDialog dialog = new ConstraintsDialog(constraints);
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

	}

	public static void main(String[] args)
	{
		ConstraintsTest test = new ConstraintsTest();
		test.showConstraintsView();

	}
}
