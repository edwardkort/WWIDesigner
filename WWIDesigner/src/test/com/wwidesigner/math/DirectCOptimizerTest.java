/**
 * Test DIRECTOptimizer against standard objective functions.
 */
package com.wwidesigner.math;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Burton Patkau
 * 
 */
public class DirectCOptimizerTest extends StandardOptimizerTest
{
	public static final double CONVERGENCE_THRESHOLD = 0.005;  // About 3**-5.
	MultivariateOptimizer optimizer;

	public static void main(String[] args)
	{
		DirectCOptimizerTest myTest = new DirectCOptimizerTest();
		myTest.testRosenbrock();
	}
	
	public DirectCOptimizerTest()
	{
		super(new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
	}

	/**
	 * Test the optimization of Rosenbrock's function,
	 * a quadratic in two dimensions.
	 */
	@Test
	public final void testRosenbrock()
	{
		double lowerBound[] = {-2.0, -2.0};
		double upperBound[] = { 2.0,  2.0};
		double expected[]   = { 1.0,  1.0};
		OptimizerTestFunction objective = new RosenbrockFunction(lowerBound, upperBound);

		System.out.println("Rosenbrock:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		// DIRECTOptimizer currently finds a near-minimum, just around the corner from expected.
		Assert.assertArrayEquals("Rosenbrock x is incorrect", expected, outcome.getPoint(), 0.005);
		Assert.assertEquals("Rosenbrock f(x) is incorrect", 0.0, outcome.getValue(), 0.002);
		Assert.assertFalse("Rosenbrock, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 680);
	}

	/**
	 * Test the optimization of Hartman's H3 function.
	 */
	@Test
	public final void testHartman3()
	{
		double lowerBound[] = {0.0, 0.0, 0.0};
		double upperBound[] = {1.0, 1.0, 1.0};
		double expected[]   = {0.1, 0.5559, 0.8522};
		OptimizerTestFunction objective = new HartmanFunction(3, lowerBound, upperBound);

		System.out.println("Hartman H3:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		// x[0] found is inaccurate.
		Assert.assertArrayEquals("Hartman3 x is incorrect", expected, outcome.getPoint(), 0.02);
		Assert.assertEquals("Hartman3 f(x) is incorrect", -3.8628, outcome.getValue(), 0.001);
		Assert.assertFalse("Hartman3, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 200);
	}

	/**
	 * Test the optimization of Hartman's H6 function.
	 */
	@Test
	public final void testHartman6()
	{
		double lowerBound[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		double upperBound[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
		double expected[]   = {0.2017, 0.1500, 0.4769, 0.2753, 0.3117, 0.6573};
		OptimizerTestFunction objective = new HartmanFunction(6, lowerBound, upperBound);

		System.out.println("Hartman H6:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		// When dividing on only one long side at a time, performance on H6 depends greatly
		// on the choice of *which* long side.
		Assert.assertArrayEquals("Hartman6 x is incorrect", expected, outcome.getPoint(), 0.0021);
		Assert.assertEquals("Hartman6 f(x) is incorrect", -3.3224, outcome.getValue(), 0.0005);
		Assert.assertFalse("Hartman6, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 420);
	}

	/**
	 * Test the optimization of Shekel's S5 function.
	 */
	@Test
	public final void testShekel5()
	{
		double lowerBound[] = { 0.0,  0.0,  0.0,  0.0};
		double upperBound[] = {10.0, 10.0, 10.0, 10.0};
		double expected[]   = { 4.0,  4.0,  4.0,  4.0};
		OptimizerTestFunction objective = new ShekelFunction(5, lowerBound, upperBound);

		System.out.println("Shekel S5:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Shekel5 x is incorrect", expected, outcome.getPoint(), 0.015);
		Assert.assertEquals("Shekel5 f(x) is incorrect", -10.1531996790582, outcome.getValue(), 0.06);
		Assert.assertFalse("Shekel5, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 100);
	}

	/**
	 * Test the optimization of Shekel's S7 function.
	 */
	@Test
	public final void testShekel7()
	{
		double lowerBound[] = { 0.0,  0.0,  0.0,  0.0};
		double upperBound[] = {10.0, 10.0, 10.0, 10.0};
		double expected[]   = { 4.0,  4.0,  4.0,  4.0};
		OptimizerTestFunction objective = new ShekelFunction(7, lowerBound, upperBound);

		System.out.println("Shekel S7:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Shekel7 x is incorrect", expected, outcome.getPoint(), 0.015);
		Assert.assertEquals("Shekel7 f(x) is incorrect", -10.4029405668187, outcome.getValue(), 0.06);
		Assert.assertFalse("Shekel7, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 100);
	}

	/**
	 * Test the optimization of Shekel's S10 function.
	 */
	@Test
	public final void testShekel10()
	{
		double lowerBound[] = { 0.0,  0.0,  0.0,  0.0};
		double upperBound[] = {10.0, 10.0, 10.0, 10.0};
		double expected[]   = { 4.0,  4.0,  4.0,  4.0};
		OptimizerTestFunction objective = new ShekelFunction(10, lowerBound, upperBound);

		System.out.println("Shekel S10:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Shekel10 x is incorrect", expected, outcome.getPoint(), 0.015);
		Assert.assertEquals("Shekel10 f(x) is incorrect", -10.5364098166920, outcome.getValue(), 0.06);
		Assert.assertFalse("Shekel10, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 100);
	}

	/**
	 * Test the optimization of the Goldstein-Price function.
	 */
	@Test
	public final void testGoldsteinPrice()
	{
		double lowerBound[] = {-2.0, -2.0};
		double upperBound[] = { 2.0,  2.0};
		double expected[]   = { 0.0, -1.0};
		OptimizerTestFunction objective = new GoldsteinPriceFunction(lowerBound, upperBound);

		System.out.println("Goldstein-Price:");
		PointValuePair outcome = optimizationTest(objective);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Goldstein-Price x is incorrect", expected, outcome.getPoint(), 0.005);
		Assert.assertEquals("Goldstein-Price f(x) is incorrect", 3.0, outcome.getValue(), 0.01);
		Assert.assertFalse("Goldstein-Price, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 190);
	}

}
