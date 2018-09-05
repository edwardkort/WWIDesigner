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
public class Ackley
{
	public static final double CONVERGENCE_THRESHOLD = 1.6e-4;  // About 3**-8.
	public static final int N = 16;
	public static final double LOWER_BOUND = -30.0;
	public static final double UPPER_BOUND = 20.0;

	public static void main(String[] args)
	{
		Ackley.testAckley(N, new DIRECTOptimizer(CONVERGENCE_THRESHOLD));
		Ackley.testSkewedAckley(N, new DIRECTOptimizer(CONVERGENCE_THRESHOLD));
		Ackley.testAckley(N, new DIRECT1Optimizer(CONVERGENCE_THRESHOLD));
		Ackley.testSkewedAckley(N, new DIRECT1Optimizer(CONVERGENCE_THRESHOLD));
		Ackley.testAckley(N, new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
		Ackley.testSkewedAckley(N, new DIRECTCOptimizer(CONVERGENCE_THRESHOLD));
		Ackley.testAckley(N, new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
		Ackley.testSkewedAckley(N, new DIRECT_L_Optimizer(CONVERGENCE_THRESHOLD));
		Ackley.testAckley(N, new BOBYQAOptimizer(2 * N + 1, 1.0, 1e-4));
		Ackley.testSkewedAckley(N, new BOBYQAOptimizer(2 * N + 1, 1.0, 1e-4));
	}
	
	public Ackley()
	{
	}

	/**
	 * Test the optimization of Ackleys's function.
	 */
	public final static void testAckley(int n, MultivariateOptimizer optimizer)
	{
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);
		double lowerBound[] = new double[n];
		double upperBound[] = new double[n];
		double expected[]   = new double[n];
		Arrays.fill(lowerBound, LOWER_BOUND);
		Arrays.fill(upperBound, UPPER_BOUND);
		Arrays.fill(expected,   0.0);
		// Skew the bounds slightly so the problem is not as symmetric.
		// This neutralizes the advantage DIRECT-L has by not including
		// duplicate points in the hull.
		for (int i = 0; i < upperBound.length; ++i)
		{
			upperBound[i] += 0.1 * i;
		}
		OptimizerTestFunction objective
				= new StandardOptimizerTest.AckleyFunction(lowerBound, upperBound);

		try
		{
			test.optimizationTest(objective, 0.01);
		}
		catch (Exception ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	/**
	 * Test the optimization of Ackleys's function.
	 */
	public final static void testSkewedAckley(int n, MultivariateOptimizer optimizer)
	{
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);
		double lowerBound[] = new double[n];
		double upperBound[] = new double[n];
		double expected[]   = new double[n];
		Arrays.fill(lowerBound, LOWER_BOUND);
		Arrays.fill(upperBound, UPPER_BOUND);
		Arrays.fill(expected,   0.0);
		OptimizerTestFunction objective
				= new StandardOptimizerTest.SkewedAckleyFunction(lowerBound, upperBound);

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
