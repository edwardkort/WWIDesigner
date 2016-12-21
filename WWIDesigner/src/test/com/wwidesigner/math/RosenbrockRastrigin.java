/**
 * Test DIRECT optimizers against Rastrigin objective functions.
 */
package com.wwidesigner.math;

import java.util.Arrays;

import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.random.MersenneTwister;

import com.wwidesigner.math.StandardOptimizerTest.OptimizerTestFunction;

/**
 * @author Burton Patkau
 * 
 */
public class RosenbrockRastrigin
{
	public static final double CONVERGENCE_THRESHOLD = 3.0e-10; // About 3**-20.
	public static final int N = 9;
	public static final double LOWER_BOUND = -2.0;
	public static final double UPPER_BOUND = 5.0;

	public static void main(String[] args)
	{
		RosenbrockRastrigin myTest = new RosenbrockRastrigin();
		myTest.testOptimization(N, new DIRECTOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testOptimization(N, new DIRECT1Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testOptimization(N, new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testOptimization(N, new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testOptimization(N, new BOBYQAOptimizer(2 * N + 1, 1.0, 1e-4));
		ConvergenceChecker<PointValuePair> convergenceChecker = new SimpleValueChecker(
				1.e-6, 1.e-14);
		MultivariateOptimizer optimizer = new CMAESOptimizer(
				100000, 0.01, true, 0, 0,
				new MersenneTwister(), false, convergenceChecker);
		myTest.testOptimization(N, optimizer);
	}
	
	public RosenbrockRastrigin()
	{
	}

	/**
	 * Test the optimization of the Rosenbrock-Rastrigin function.
	 */
	public final void testOptimization(int n, MultivariateOptimizer optimizer)
	{
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);
		double lowerBound[] = new double[n];
		double upperBound[] = new double[n];
		double expected[]   = new double[n];
		Arrays.fill(lowerBound, LOWER_BOUND);
		Arrays.fill(upperBound, UPPER_BOUND);
		Arrays.fill(expected,   0.0);
		OptimizerTestFunction objective
				= new StandardOptimizerTest.RosenbrockRastriginFunction(lowerBound, upperBound);

		try
		{
			test.optimizationTest(objective);
		}
		catch (Exception ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
	}

}
