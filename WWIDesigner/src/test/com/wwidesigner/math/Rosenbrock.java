/**
 * Test DIRECT optimizers against high-dimension Rosenbrock objective functions.
 */
package com.wwidesigner.math;

import java.util.Arrays;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import com.wwidesigner.math.StandardOptimizerTest.OptimizerTestFunction;

/**
 * @author Burton Patkau
 * 
 */
public class Rosenbrock
{
	public static final double CONVERGENCE_THRESHOLD = 1.7e-5;  // About 3**-10.
	public static final int N = 14;
	public static final double LOWER_BOUND = -1.0;
	public static final double UPPER_BOUND = 2.0;
	MultivariateOptimizer optimizer;

	public static void main(String[] args)
	{
		Rosenbrock myTest = new Rosenbrock();
		myTest.testRosenbrock(N, new DIRECTOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testScaledRosenbrock(N, new DIRECTOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testRosenbrock(N, new DIRECT1Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testScaledRosenbrock(N, new DIRECT1Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testRosenbrock(N, new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testScaledRosenbrock(N, new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testRosenbrock(N, new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testScaledRosenbrock(N, new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testRosenbrock(N, new BOBYQAOptimizer(2 * N + 1, 1.0, 1e-4));
		myTest.testScaledRosenbrock(N, new BOBYQAOptimizer(2 * N + 1, 1.0, 1e-4));
		myTest.testScaledRosenbrock(N, new BOBYQAOptimizer(2 * N + 1, 1.0 * N, 1e-4 * N));
	}
	
	public Rosenbrock()
	{
	}

	/**
	 * Test the optimization of Rosenbrock's function.
	 */
	public final void testRosenbrock(int n, MultivariateOptimizer optimizer)
	{
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);
		double lowerBound[] = new double[n];
		double upperBound[] = new double[n];
		double expected[]   = new double[n];
		Arrays.fill(lowerBound, LOWER_BOUND);
		Arrays.fill(upperBound, UPPER_BOUND);
		Arrays.fill(expected,   1.0);
		OptimizerTestFunction objective
				= new StandardOptimizerTest.RosenbrockFunction(lowerBound, upperBound);

		try
		{
			test.optimizationTest(objective, 0.01);
		}
		catch (TooManyEvaluationsException ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	/**
	 * Test the optimization of a Rosenbrock's function,
	 * with dimensions of different scale.
	 */
	public final void testScaledRosenbrock(int n, MultivariateOptimizer optimizer)
	{
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);
		double lowerBound[] = new double[n];
		double upperBound[] = new double[n];
		double expected[]   = new double[n];
		Arrays.fill(lowerBound, LOWER_BOUND);
		Arrays.fill(upperBound, UPPER_BOUND);
		Arrays.fill(expected,   1.0);
		OptimizerTestFunction objective
				= new StandardOptimizerTest.ScaledRosenbrockFunction(lowerBound, upperBound);

		try
		{
			test.optimizationTest(objective, 0.01);
		}
		catch (TooManyEvaluationsException ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
	}

}
