package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

public class ConstraintsTest extends AbstractOptimizationTest
{
	@Test
	public final void testGetFippleConstraint()
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

	public static void main(String[] args)
	{
		ConstraintsTest test = new ConstraintsTest();
		test.testGetFippleConstraint();
	}

}
