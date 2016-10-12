/**
 * Infrastructure to test optimizers against standard objective functions.
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

/**
 * @author Burton Patkau
 * 
 */
public class StandardOptimizerTest
{
	MultivariateOptimizer optimizer;

	public StandardOptimizerTest(MultivariateOptimizer optimizer)
	{
		this.optimizer = optimizer;
	}
	
	public abstract static class OptimizerTestFunction implements MultivariateFunction
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
		return optimizationTest(objective, null);
	}

	public PointValuePair optimizationTest(OptimizerTestFunction objective,
			Double targetFunctionValue)
	{
		PointValuePair outcome;
		long startTime = System.currentTimeMillis();

		outcome = optimizer.optimize(
				GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(50000),
				MaxIter.unlimited(),
				new InitialGuess(objective.getStartPoint()),
				new DIRECTOptimizer.TargetFunctionValue(targetFunctionValue),
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
}
