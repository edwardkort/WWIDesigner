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

}
