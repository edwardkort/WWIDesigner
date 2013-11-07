/**
 * Class to manage the optimization of an objective function
 * using the Apache Commons Math optimizers.
 */
package com.wwidesigner.optimization;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.apache.commons.math3.random.MersenneTwister;

import com.wwidesigner.optimization.multistart.AbstractRangeProcessor;
import com.wwidesigner.optimization.multistart.RandomRangeProcessor;

public class ObjectiveFunctionOptimizer
{
	/**
	 * Print a vector of error values during optimization.
	 * @param description - describes what the error values apply to
	 * @param errorNorm - overall norm of the error vector
	 * @param errorVector - error values at specific tuning points
	 */
	protected static void printErrors(String description, double errorNorm, double[] errorVector)
	{
		boolean firstPass = true;
		System.out.print(description);
		System.out.print(errorNorm);
		System.out.print(" from [");
		for (double err: errorVector)
		{
			if (! firstPass)
			{
				System.out.print(",  ");
			}
			else
			{
				firstPass = false;
			}
			System.out.print(err);
		}
		System.out.println("].");
	}

	/**
	 * Use a specified optimizer type to optimize a specified objective function.
	 * @param objective - objective function to optimize
	 * @param optimizerType - type of optimizer to use
	 */
	public static boolean optimizeObjectiveFunction(BaseObjectiveFunction  objective,
			BaseObjectiveFunction.OptimizerType optimizerType )
	{
		ConvergenceChecker<PointValuePair> convergenceChecker = new SimpleValueChecker(0.00001, 0.0000001);
		
		double[] startPoint = objective.getInitialPoint();
		double[] errorVector = objective.getErrorVector(startPoint);
		double initialNorm = BaseObjectiveFunction.calcNorm(errorVector);
		System.out.println();
		printErrors("Initial error: ", initialNorm, errorVector);
		
		try
		{
			if ( optimizerType.equals(BaseObjectiveFunction.OptimizerType.BrentOptimizer) )
			{
				// Univariate optimization.
				BrentOptimizer optimizer = new BrentOptimizer(0.00001, 0.00001);
				UnivariatePointValuePair  outcome;
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new UnivariateObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
						new SearchInterval(objective.getLowerBounds()[0], 
								objective.getUpperBounds()[0],
								startPoint[0]));
				double[] geometry = new double[1];
				geometry[0] = outcome.getPoint();
				objective.setGeometryPoint(geometry);
			}
			else if ( optimizerType.equals(BaseObjectiveFunction.OptimizerType.PowellOptimizer) )
			{
				// Multivariate optimization, without bounds.
				PowellOptimizer optimizer = new PowellOptimizer(0.00001, 0.000001);
				PointValuePair  outcome;
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
						new InitialGuess(startPoint));
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if ( optimizerType.equals(BaseObjectiveFunction.OptimizerType.SimplexOptimizer) )
			{
				// Multivariate optimization, without bounds.
				SimplexOptimizer optimizer = new SimplexOptimizer(convergenceChecker);
				MultiDirectionalSimplex simplex 
					= new MultiDirectionalSimplex(objective.getSimplexStepSize());
				PointValuePair  outcome;
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
						new InitialGuess(startPoint),
						simplex);
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if ( optimizerType.equals(BaseObjectiveFunction.OptimizerType.MultiStartOptimizer) )
			{
				PointValuePair  outcome;
				outcome = optimizeMultiStart(objective, startPoint);
				if (outcome == null)
				{
					// Restore starting point.
					objective.setGeometryPoint(startPoint);
				}
				else
				{
					objective.setGeometryPoint(outcome.getPoint());
				}
			}
			else if ( optimizerType.equals(BaseObjectiveFunction.OptimizerType.CMAESOptimizer) )
			{
				// Multivariate optimization, with bounds.
				MultivariateOptimizer optimizer;
				PointValuePair  outcome;
				optimizer = new CMAESOptimizer(objective.getMaxEvaluations(), 
							0.01 * initialNorm, true, 0, 0, new MersenneTwister(), false,
							convergenceChecker);
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
						new InitialGuess(startPoint),
						new SimpleBounds(objective.getLowerBounds(), 
								objective.getUpperBounds()),
						new CMAESOptimizer.PopulationSize(objective.getNrInterpolations()),
						new CMAESOptimizer.Sigma(objective.getStdDev()));
				objective.setGeometryPoint(outcome.getPoint());
			}
			else {
				// Multivariate BOBYQA optimization, with bounds.
				MultivariateOptimizer optimizer;
				double trustRegion = objective.getInitialTrustRegionRadius();
				PointValuePair  outcome;
				optimizer = new BOBYQAOptimizer(objective.getNrInterpolations(), 
							trustRegion, 1e-8 * trustRegion);
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
						new InitialGuess(startPoint),
						new SimpleBounds(objective.getLowerBounds(), 
								objective.getUpperBounds()) );
				objective.setGeometryPoint(outcome.getPoint());
			}
		}
		catch (TooManyEvaluationsException e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		System.out.print("Performed ");
		System.out.print(objective.getNumberOfTunings());
		System.out.print(" tuning calculations in ");
		System.out.print(objective.getNumberOfEvaluations());
		System.out.println(" error norm evaluations.");
		errorVector = objective.getErrorVector(objective.getInitialPoint());
		double finalNorm = BaseObjectiveFunction.calcNorm(errorVector);
		printErrors("Final error:  ", finalNorm, errorVector);
		System.out.print("Residual error ratio: ");
		System.out.println(finalNorm/initialNorm);
		
		return true;
	} // optimizeObjectiveFunction

	/**
	 * Use a multi-start BOBYQA optimization to optimize a specified objective function.
	 * Use range processor and number of starts from {@code objective} if available;
	 * otherwise use a RandomRangeProcessor with 30 starts.
	 * @param objective - objective function to optimize
	 * @return best point and value found, or {@code null} if no good point found.
	 */
	public static PointValuePair optimizeMultiStart(BaseObjectiveFunction  objective,
			double[] startPoint)
	{
		AbstractRangeProcessor  rangeProcessor = objective.getRangeProcessor();
		int nrStarts = 30;
		if (rangeProcessor == null)
		{
			rangeProcessor = new RandomRangeProcessor(objective.getLowerBounds(), 
						objective.getUpperBounds(), null, nrStarts);
		}
		else
		{
			nrStarts = rangeProcessor.getNumberOfStarts();
		}
		PointValuePair[] optima = new PointValuePair[nrStarts];
		int maxEvaluations = objective.getMaxEvaluations();
		int totalEvaluations = 0;
		rangeProcessor.setStaticValues(startPoint);

		// Multi-start loop.
		double[] nextStart = startPoint.clone();

		for (int startNr = 0; startNr < nrStarts; ++startNr)
		{
			if (totalEvaluations >= maxEvaluations)
			{
				optima[startNr] = null;
			}
			else {
				double trustRegion = objective.getInitialTrustRegionRadius(nextStart);
				BOBYQAOptimizer optimizer = new BOBYQAOptimizer(objective.getNrInterpolations(), 
						trustRegion, 1e-8 * trustRegion);
				try
				{
					System.out.print("Start " + (int) (startNr + 1) + ": " );
					optima[startNr] = optimizer.optimize(GoalType.MINIMIZE,
							new ObjectiveFunction(objective),
							new MaxEval(maxEvaluations - totalEvaluations), MaxIter.unlimited(),
							new InitialGuess(nextStart),
							new SimpleBounds(objective.getLowerBounds(), 
									objective.getUpperBounds()));
					System.out.println("optimum " + optima[startNr].getValue()
							+ " at start point " + Arrays.toString(nextStart));
				}
				catch (TooManyEvaluationsException e)
				{
					System.out.println("Exception: " + e.getMessage());
					optima[startNr] = null;
				}
				catch (Exception e)
				{
					System.out.println("Exception: " + e.getMessage());
					// e.printStackTrace();
					optima[startNr] = null;
				}
	
				totalEvaluations += optimizer.getEvaluations();
				nextStart = rangeProcessor.nextVector();
			}
		}

		sortPairs(GoalType.MINIMIZE, optima);

		if (optima[0] != null)
		{
			// Return the found point given the best objective function value.
			System.out.println("Best optimum: " + optima[0].getValue());
		}

		return optima[0];
	}
	
	/**
	 * Sort PointValuePairs from best to worst, followed by {@code null} elements.
	 * 
	 * @param goal - GoalType.MINIMIZE or GoalType.MAXIMIZE
	 * @param optima - array of point-value pairs from successive optimizations.
	 */
	protected static void sortPairs(final GoalType goal, PointValuePair[] optima)
	{
		Arrays.sort(optima, new Comparator<PointValuePair>()
		{
			public int compare(final PointValuePair o1, final PointValuePair o2)
			{
				if (o1 == null)
				{
					return (o2 == null) ? 0 : 1;
				}
				else if (o2 == null)
				{
					return -1;
				}
				final double v1 = o1.getValue();
				final double v2 = o2.getValue();
				return (goal == GoalType.MINIMIZE) ? Double.compare(v1, v2)
						: Double.compare(v2, v1);
			}
		});
	}
}
