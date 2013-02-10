package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.modelling.NAFCalculator;

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

			assertEquals("Number of constraints incorrect",
					objective.getNrDimensions(),
					constraints.getNumberOfConstraints());

			Constraint constraint = constraints.getConstraint(0);
			assertEquals("Constraint name incorrect", "Fipple factor",
					constraint.getDisplayName());

			assertEquals("Constraint dimensionality incorrect", false,
					constraint.isDimensional());

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

			assertEquals("Number of constraints incorrect",
					objective.getNrDimensions(),
					constraints.getNumberOfConstraints());

			// Test hole name that is not set in Instrument.xml
			Constraint constraint = constraints.getConstraint(5);
			assertEquals("Constraint name incorrect",
					"Hole 1 (bottom) diameter", constraint.getDisplayName());

			assertEquals("Constraint dimensionality incorrect", true,
					constraint.isDimensional());

			// Test hole name that is set in Instrument.xml
			constraint = constraints.getConstraint(0);
			assertEquals("Constraint name incorrect",
					"Hole T (top) diameter", constraint.getDisplayName());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}
