/**
 * Test DIRECT optimizers against Rastrigin objective functions.
 */
package com.wwidesigner.math;

import java.util.Arrays;

import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import com.wwidesigner.math.StandardOptimizerTest.OptimizerTestFunction;

/**
 * @author Burton Patkau
 * 
 */
public class Rastrigin
{
	public static final double CONVERGENCE_THRESHOLD = 1.6e-4;  // About 3**-8.
	public static final int N = 15;
	public static final double LOWER_BOUND = -2.0;
	public static final double UPPER_BOUND = 5.0;
	MultivariateOptimizer optimizer;

	public static void main(String[] args)
	{
		Rastrigin myTest = new Rastrigin();
		myTest.testRastrigin(N, new DIRECTOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testRastrigin(N, new DIRECT1Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testRastrigin(N, new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
		myTest.testRastrigin(N, new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
		myTest.testRastrigin(N, new BOBYQAOptimizer(2 * N + 1, 1.0, 1e-4));
	}
	
	public Rastrigin()
	{
	}

	/**
	 * Test the optimization of Rastrigin's function.
	 */
	public final void testRastrigin(int n, MultivariateOptimizer optimizer)
	{
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);
		double lowerBound[] = new double[n];
		double upperBound[] = new double[n];
		double expected[]   = new double[n];
		Arrays.fill(lowerBound, LOWER_BOUND);
		Arrays.fill(upperBound, UPPER_BOUND);
		Arrays.fill(expected,   0.0);
		OptimizerTestFunction objective
				= new StandardOptimizerTest.RastriginFunction(10.0, lowerBound, upperBound);

		try
		{
			test.optimizationTest(objective, 0.01);
		}
		catch (Exception ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
	}

}
