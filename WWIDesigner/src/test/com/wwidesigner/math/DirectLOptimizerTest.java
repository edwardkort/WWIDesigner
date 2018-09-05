/**
 * Test DIRECT_L_Optimizer against standard objective functions.
 */
package com.wwidesigner.math;

import java.util.Arrays;

import org.apache.commons.math3.optim.PointValuePair;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Burton Patkau
 * 
 */
public class DirectLOptimizerTest extends StandardOptimizerTest
{
	public static final double CONVERGENCE_THRESHOLD = 0.00005;

	public static void main(String[] args)
	{
		DirectLOptimizerTest myTest = new DirectLOptimizerTest();
		myTest.testRosenbrock();
	}
	
	public DirectLOptimizerTest()
	{
		super(new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
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

		PointValuePair outcome = optimizationTest(objective, 0.0001);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Rosenbrock x is incorrect", expected, outcome.getPoint(), 0.01);
		Assert.assertEquals("Rosenbrock f(x) is incorrect", 0.0, outcome.getValue(), 0.00005);
		Assert.assertFalse("Rosenbrock, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 380);
	}

	/**
	 * Test the optimization of Hartman's H3 function.
	 */
	@Test
	public final void testHartman3()
	{
		double lowerBound[] = {0.0, 0.0, 0.0};
		double upperBound[] = {1.0, 1.0, 1.0};
		double expected[]   = {0.114614, 0.555649, 0.852547};
		OptimizerTestFunction objective = new HartmanFunction(lowerBound, upperBound);

		PointValuePair outcome = optimizationTest(objective, -3.86278 * 0.9999);

		// Test that optimum and optimizer point are correct.
		// x[0] found is inaccurate.
		Assert.assertArrayEquals("Hartman3 x is incorrect", expected, outcome.getPoint(), 0.003);
		Assert.assertEquals("Hartman3 f(x) is incorrect", -3.86278, outcome.getValue(), 0.0004);
		Assert.assertFalse("Hartman3, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 120);
	}

	/**
	 * Test the optimization of Hartman's H6 function.
	 */
	@Test
	public final void testHartman6()
	{
		double lowerBound[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		double upperBound[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
		double expected[]   = {0.20169, 0.150011, 0.476874, 0.275332, 0.311652, 0.6573};
		OptimizerTestFunction objective = new HartmanFunction(lowerBound, upperBound);

		PointValuePair outcome = optimizationTest(objective, -3.3224 * 0.9999);

		// Test that optimum and optimizer point are correct.
		// When dividing on only one long side at a time, performance on H6 depends greatly
		// on the choice of *which* long side.
		Assert.assertArrayEquals("Hartman6 x is incorrect", expected, outcome.getPoint(), 0.022);
		Assert.assertEquals("Hartman6 f(x) is incorrect", -3.32237, outcome.getValue(), 0.03);
		Assert.assertFalse("Hartman6, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 1160);
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

		PointValuePair outcome = optimizationTest(objective, -10.1531996790582 * 0.9999);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Shekel5 x is incorrect", expected, outcome.getPoint(), 0.002);
		Assert.assertEquals("Shekel5 f(x) is incorrect", -10.1531996790582, outcome.getValue(), 0.0011);
		Assert.assertFalse("Shekel5, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 160);
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

		PointValuePair outcome = optimizationTest(objective, -10.4029405668187 * 0.9999);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Shekel7 x is incorrect", expected, outcome.getPoint(), 0.002);
		Assert.assertEquals("Shekel7 f(x) is incorrect", -10.4029405668187, outcome.getValue(), 0.0011);
		Assert.assertFalse("Shekel7, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 150);
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

		PointValuePair outcome = optimizationTest(objective, -10.5364098166920 * 0.9999);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Shekel10 x is incorrect", expected, outcome.getPoint(), 0.002);
		Assert.assertEquals("Shekel10 f(x) is incorrect", -10.5364098166920, outcome.getValue(), 0.0011);
		Assert.assertFalse("Shekel10, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 150);
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

		PointValuePair outcome = optimizationTest(objective, 3.0 * 1.0001);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Goldstein-Price x is incorrect", expected, outcome.getPoint(), 0.0005);
		Assert.assertEquals("Goldstein-Price f(x) is incorrect", 3.0, outcome.getValue(), 0.0001);
		Assert.assertFalse("Goldstein-Price, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 110);
	}

	/**
	 * Test the optimization of the Rastrigin function.
	 */
	@Test
	public final void testRastrigin()
	{
		final int N = 10;	// With bounds below, DIRECT-L gets lucky.
		double lowerBound[] = new double[N];
		double upperBound[] = new double[N];
		double expected[]   = new double[N];
		Arrays.fill(lowerBound, -1.5);
		Arrays.fill(upperBound,  3.0);
		Arrays.fill(expected,    0.0);
		OptimizerTestFunction objective = new RastriginFunction(lowerBound, upperBound);

		PointValuePair outcome = optimizationTest(objective, 0.0001);

		// Test that optimum and optimizer point are correct.
		Assert.assertArrayEquals("Rastrigin x is incorrect", expected, outcome.getPoint(), 0.001);
		Assert.assertEquals("Rastrigin f(x) is incorrect", 0.0, outcome.getValue(), 0.001);
		Assert.assertFalse("Rastrigin, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 1700);
	}

}
