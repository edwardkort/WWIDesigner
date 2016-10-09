/**
 * Test DiRectOptimizer against standard objective functions.
 */
package com.wwidesigner.math;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Burton Patkau
 * 
 */
public class DirectOptimizerTest
{
	public static final double CONVERGENCE_THRESHOLD = 0.005;  // About 3**-5.
	MultivariateOptimizer optimizer;

	public static void main(String[] args)
	{
		DirectOptimizerTest myTest = new DirectOptimizerTest();
		myTest.testRosenbrock();
	}
	
	public DirectOptimizerTest()
	{
		optimizer = new DIRECTOptimizer(CONVERGENCE_THRESHOLD);
	}
	
	public abstract class OptimizerTestFunction implements MultivariateFunction
	{
		public int getEvaluations()
		{
			return evaluations;
		}

		public double[] getStartPoint()
		{
			// Position start point in the centre of the domain.
			int dimension = getLowerBound().length;
			double[] centre = new double[dimension];

			for (int i = 0; i < dimension; i++) {
				centre[i] = 0.5*(getUpperBound()[i] + getLowerBound()[i]);
			}

			return centre;
		}

		public double[] getLowerBound()
		{
			return lowerBound;
		}

		public double[] getUpperBound()
		{
			return upperBound;
		}

		protected int evaluations;
		protected double[] lowerBound;
		protected double[] upperBound;
		
		public OptimizerTestFunction(double[] lowerBound, double[] upperBound)
		{
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			this.evaluations = 0;
		}
	}

	public PointValuePair optimizationTest(OptimizerTestFunction objective)
	{
		PointValuePair outcome;
		long startTime = System.currentTimeMillis();

		outcome = optimizer.optimize(
				GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(50000),
				MaxIter.unlimited(),
				new InitialGuess(objective.getStartPoint()),
				new SimpleBounds(objective.getLowerBound(), objective.getUpperBound()));

		System.out.print("  Performed ");
		System.out.print(optimizer.getEvaluations());
		System.out.print(" function evaluations in ");
		System.out.print(optimizer.getIterations());
		System.out.println(" iterations.");
		System.out.print("  Optimum value: " + outcome.getValue());
		System.out.print(", at x =");
		for (int i = 0; i < outcome.getPoint().length; ++i)
		{
			System.out.print(" ");
			System.out.printf(" %9.6f", outcome.getPoint()[i]);
		}
		System.out.println();
		long elapsedTime = System.currentTimeMillis() - startTime;
		double elapsedSeconds = 0.001 * (double) elapsedTime;
		System.out.print("  Elapsed time: ");
		System.out.printf("%4.2f", elapsedSeconds);
		System.out.println(" seconds.");
		
		return outcome;
	}

	public class RosenbrockFunction extends OptimizerTestFunction
	{
		public RosenbrockFunction(double[] lowerBound,
				double[] upperBound)
		{
			super(lowerBound, upperBound);
		}

		@Override
		public double value(double[] point)
		{
			++ evaluations;
			double a = point[1] - point[0] * point[0];
			double b = 1 - point[0];
			return (100 * a*a + b*b);
		}
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
		Assert.assertArrayEquals("Rosenbrock x is incorrect", expected, outcome.getPoint(), 0.03);
		Assert.assertEquals("Rosenbrock f(x) is incorrect", 0.0, outcome.getValue(), 0.002);
		Assert.assertFalse("Rosenbrock, too many evaluations, " +  objective.getEvaluations(),
				objective.getEvaluations() > 460);
	}

	public class HartmanFunction extends OptimizerTestFunction
	{
		protected final double[][] a;
		protected final double[][] p;
		protected final double[] c;

		public HartmanFunction(int n, double[] lowerBound,
				double[] upperBound)
		{
			super(lowerBound, upperBound);
			c = new double[]{1.0, 1.2, 3.0, 3.2};
			if (n == 3)
			{
				a = new double[][]
						{{ 3.00,  0.10,  3.00,  0.10},
						 {10.00, 10.00, 10.00, 10.00},
						 {30.00, 35.00, 30.00, 35.00}};
				p = new double[][]
						{{0.36890, 0.46990, 0.10910, 0.03815},
						 {0.11700, 0.43870, 0.87320, 0.57430},
						 {0.26730, 0.74700, 0.55470, 0.88280}};
			}
			else
			{
				assert n == 6;
				a = new double[][]
						{{10.00,  0.05,  3.00, 17.00},
					     { 3.00, 10.00,  3.50,  8.00},
					     {17.00, 17.00,  1.70,  0.05},
					     { 3.50,  0.10, 10.00, 10.00},
					     { 1.70,  8.00, 17.00,  0.10},
					     { 8.00, 14.00,  8.00, 14.00}};
				p = new double[][]
						{{0.1312, 0.2329, 0.2348, 0.4047},
					     {0.1696, 0.4135, 0.1451, 0.8828},
					     {0.5569, 0.8307, 0.3522, 0.8732},
					     {0.0124, 0.3736, 0.2883, 0.5743},
					     {0.8283, 0.1004, 0.3047, 0.1091},
					     {0.5886, 0.9991, 0.6650, 0.0381}};
			}
		}

		@Override
		public double value(double[] point)
		{
			++ evaluations;
			double d, e, f;
			d = 0.0;
			for (int i = 0; i < a[0].length; ++i)
			{
				e = 0.0;
				for (int j = 0; j < point.length; ++j)
				{
					f = point[j] - p[j][i];
					e += a[j][i] * f * f;
				}
				d += c[i] * FastMath.exp(-e);
			}
			return -d;
		}
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
				objective.getEvaluations() > 415);
	}

	public class ShekelFunction extends OptimizerTestFunction
	{
		protected final int m;
		protected final double[][] a;
		protected final double[] c;

		public ShekelFunction(int m, double[] lowerBound,
				double[] upperBound)
		{
			super(lowerBound, upperBound);
			this.m = m;
			c = new double[]{0.1, 0.2, 0.2, 0.4, 0.4, 0.6, 0.3, 0.7, 0.5, 0.5};
			a = new double[][]
					{ {4.0, 4.0, 4.0, 4.0},
					  {1.0, 1.0, 1.0, 1.0},
					  {8.0, 8.0, 8.0, 8.0},
					  {6.0, 6.0, 6.0, 6.0},
					  {3.0, 7.0, 3.0, 7.0},
					  {2.0, 9.0, 2.0, 9.0},
					  {5.0, 5.0, 3.0, 3.0},
					  {8.0, 1.0, 8.0, 1.0},
					  {6.0, 2.0, 6.0, 2.0},
					  {7.0, 3.6, 7.0, 3.6}};
		}
		
		protected double sqr(double x)
		{
			return x * x;
		}

		@Override
		public double value(double[] point)
		{
			++ evaluations;
			double d, f;
			d = 0.0;
			for (int i = 0; i < m; ++i) {
				f = 1.0 / (c[i]
						+ sqr(point[0]-a[i][0])
						+ sqr(point[1]-a[i][1])
						+ sqr(point[2]-a[i][2])
						+ sqr(point[3]-a[i][3]));
				d += f;
			}
			return -d;
		}
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

	public class GoldsteinPriceFunction extends OptimizerTestFunction
	{
		public GoldsteinPriceFunction(double[] lowerBound,
				double[] upperBound)
		{
			super(lowerBound, upperBound);
		}

		@Override
		public double value(double[] point)
		{
			++ evaluations;
			double x0, x1, a1, a12, a2, b1, b12, b2;
			x0 = point[0]; x1 = point[1];
			a1 = x0+x1+1; a12 = a1*a1;
			a2 = 19 - 14*x0 + 3*x0*x0 - 14*x1 + 6*x0*x1 + 3*x1*x1;
			b1 = 2*x0-3*x1; b12 = b1*b1;
			b2 = 18 - 32*x0 + 12*x0*x0 + 48*x1 - 36*x0*x1 + 27*x1*x1;
			return ((1 + a12 * a2) * (30 + b12 * b2));
		}
	}

	/**
	 * Test the optimization of Rosenbrock's function,
	 * a quadratic in two dimensions.
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
				objective.getEvaluations() > 160);
	}

}
