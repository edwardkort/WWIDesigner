/**
 * Test DIRECT-C optimizer with different variant intervals.
 */
package com.wwidesigner.math;

import java.util.Arrays;

import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import com.wwidesigner.math.StandardOptimizerTest.OptimizerTestFunction;

/**
 * @author Burton Patkau
 * 
 */
public class DirectCVariant
{
	public static final double X_CONVERGENCE_THRESHOLD = 3.0e-10;  // About 3**-15.
	public static final Double FCN_CONVERGENCE_THRESHOLD = 0.01;

	public static void main(String[] args)
	{
		OptimizerTestFunction objective;
		double lowerBounds[];
		double upperBounds[];
		lowerBounds = new double[15];
		upperBounds = new double[15];
		fillBounds(lowerBounds, -30.0, false);
		fillBounds(upperBounds,  10.0, true);
		objective = new StandardOptimizerTest.AckleyFunction(lowerBounds, upperBounds);
		testOptimizer(objective);
		
		fillBounds(lowerBounds, -30.0, false);
		fillBounds(upperBounds,  15.0, true);
		objective = new StandardOptimizerTest.SkewedAckleyFunction(lowerBounds, upperBounds);
		testOptimizer(objective);

		lowerBounds = new double[ 5];
		upperBounds = new double[ 5];
		fillBounds(lowerBounds, -2.5, false);
		fillBounds(upperBounds,  5.0, true);
		objective = new StandardOptimizerTest.RastriginFunction(lowerBounds, upperBounds);
		testOptimizer(objective);

		lowerBounds = new double[15];
		upperBounds = new double[15];
		fillBounds(lowerBounds, -2.0, false);
		fillBounds(upperBounds,  5.0, true);
		objective = new StandardOptimizerTest.RosenbrockRastriginFunction(lowerBounds, upperBounds);
		testOptimizer(objective);
	}
	
	public DirectCVariant()
	{
	}
	
	protected static void fillBounds(double[] boundArray, double bound, boolean skew)
	{
		Arrays.fill(boundArray, bound);
		if (skew)
		{
			for (int i = 0; i < boundArray.length; ++i)
			{
				boundArray[i] += 0.01 * i;
			}
		}
	}

	public static void testOptimizerVariant(OptimizerTestFunction objective, int variantInterval)
	{
		MultivariateOptimizer optimizer = new DIRECTCOptimizer(X_CONVERGENCE_THRESHOLD, variantInterval);
		StandardOptimizerTest test = new StandardOptimizerTest(optimizer);

		try
		{
			test.optimizationTest(objective, FCN_CONVERGENCE_THRESHOLD);
		}
		catch (Exception ex)
		{
			System.out.println("Exception: " + ex.getMessage());
		}
	}
	
	public static void testOptimizer(OptimizerTestFunction objective)
	{
	//	testOptimizerVariant(objective, 2);
		testOptimizerVariant(objective, 3);
		testOptimizerVariant(objective, 4);
	//	testOptimizerVariant(objective, 5);
	//	testOptimizerVariant(objective, 6);
	}

}
