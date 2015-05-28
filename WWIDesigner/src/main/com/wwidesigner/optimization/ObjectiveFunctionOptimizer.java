/**
 * Class to manage the optimization of an objective function
 * using the Apache Commons Math optimizers.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.optimization;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

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

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.optimization.multistart.AbstractRangeProcessor;
import com.wwidesigner.optimization.multistart.RandomRangeProcessor;

public class ObjectiveFunctionOptimizer
{
	// Statistics saved from the most recent call to optimizeObjectiveFunction

	protected static double initialNorm; // Initial value of objective function.
	protected static double finalNorm; // Final value of objective function.
	protected static final boolean DEBUG_MODE = false;

	/**
	 * Print a vector of error values during optimization.
	 * 
	 * @param description
	 *            - describes what the error values apply to
	 * @param errorNorm
	 *            - overall norm of the error vector
	 * @param errorVector
	 *            - error values at specific tuning points
	 */
	protected static void printErrors(String description, double errorNorm,
			double[] errorVector)
	{
		boolean firstPass = true;
		System.out.print(description);
		System.out.print(errorNorm);
		if (DEBUG_MODE)
		{
			System.out.print(" from [");
			for (double err : errorVector)
			{
				if (!firstPass)
				{
					System.out.print(",  ");
				}
				else
				{
					firstPass = false;
				}
				System.out.print(err);
			}
			System.out.print("]");
		}
		System.out.println(".");
	}

	/**
	 * Use a specified optimizer type to optimize a specified objective
	 * function.
	 * 
	 * @param objective
	 *            - objective function to optimize
	 * @param optimizerType
	 *            - type of optimizer to use
	 */
	public static boolean optimizeObjectiveFunction(
			BaseObjectiveFunction objective,
			BaseObjectiveFunction.OptimizerType optimizerType)
	{
		// Rely on relative difference to test convergence,
		// to allow for vast differences in absolute error scale
		// between objective functions.
		ConvergenceChecker<PointValuePair> convergenceChecker = new SimpleValueChecker(
				1.e-6, 1.e-14);

		System.out.print("\nSystem has ");
		System.out.print(objective.getNrDimensions());
		System.out.print(" optimization variables and ");
		System.out.print(objective.getNrNotes());
		System.out.print(" target notes.");

		long startTime = System.currentTimeMillis();
		double[] startPoint = objective.getInitialPoint();
		double[] errorVector = objective.getErrorVector(startPoint);
		initialNorm = objective.calcNorm(errorVector);
		System.out.println();
		printErrors("Initial error: ", initialNorm, errorVector);
		finalNorm = initialNorm;

		try
		{
			if (optimizerType
					.equals(BaseObjectiveFunction.OptimizerType.BrentOptimizer))
			{
				// Univariate optimization.
				BrentOptimizer optimizer = new BrentOptimizer(1.e-6, 1.e-14);
				UnivariatePointValuePair outcome;
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new UnivariateObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()), MaxIter
								.unlimited(),
						new SearchInterval(objective.getLowerBounds()[0],
								objective.getUpperBounds()[0], startPoint[0]));
				double[] geometry = new double[1];
				geometry[0] = outcome.getPoint();
				objective.setGeometryPoint(geometry);
			}
			else if (optimizerType
					.equals(BaseObjectiveFunction.OptimizerType.PowellOptimizer))
			{
				// Multivariate optimization, without bounds.
				PowellOptimizer optimizer = new PowellOptimizer(1.e-6, 1.e-14);
				PointValuePair outcome;
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()),
						MaxIter.unlimited(), new InitialGuess(startPoint));
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if (optimizerType
					.equals(BaseObjectiveFunction.OptimizerType.SimplexOptimizer))
			{
				// Multivariate optimization, without bounds.
				SimplexOptimizer optimizer = new SimplexOptimizer(
						convergenceChecker);
				MultiDirectionalSimplex simplex = new MultiDirectionalSimplex(
						objective.getSimplexStepSize());
				PointValuePair outcome;
				outcome = optimizer.optimize(GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()),
						MaxIter.unlimited(), new InitialGuess(startPoint),
						simplex);
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if (optimizerType
					.equals(BaseObjectiveFunction.OptimizerType.MultiStartOptimizer))
			{
				PointValuePair outcome;
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
			else if (optimizerType
					.equals(BaseObjectiveFunction.OptimizerType.CMAESOptimizer))
			{
				// Multivariate optimization, with bounds.
				MultivariateOptimizer optimizer;
				PointValuePair outcome;
				optimizer = new CMAESOptimizer(objective.getMaxEvaluations(),
						0.0001 * initialNorm, true, 0, 0,
						new MersenneTwister(), false, convergenceChecker);
				outcome = optimizer.optimize(
						GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()),
						MaxIter.unlimited(),
						new InitialGuess(startPoint),
						new SimpleBounds(objective.getLowerBounds(), objective
								.getUpperBounds()),
						new CMAESOptimizer.PopulationSize(objective
								.getNrInterpolations()),
						new CMAESOptimizer.Sigma(objective.getStdDev()));
				objective.setGeometryPoint(outcome.getPoint());
			}
			else
			{
				// Multivariate BOBYQA optimization, with bounds.
				MultivariateOptimizer optimizer;
				double trustRegion = objective.getInitialTrustRegionRadius();
				double stoppingTrustRegion = objective
						.getStoppingTrustRegionRadius();
				PointValuePair outcome;
				optimizer = new BOBYQAOptimizer(
						objective.getNrInterpolations(), trustRegion,
						stoppingTrustRegion);

				// Run optimization first with the first-stage evaluator, if
				// specified
				EvaluatorInterface originalEvaluator = objective.getEvaluator();
				if (objective.isRunTwoStageOptimization())
				{
					objective.setEvaluator(objective.getFirstStageEvaluator());
					outcome = optimizer.optimize(GoalType.MINIMIZE,
							new ObjectiveFunction(objective), new MaxEval(
									objective.getMaxEvaluations()), MaxIter
									.unlimited(), new InitialGuess(startPoint),
							new SimpleBounds(objective.getLowerBounds(),
									objective.getUpperBounds()));
					objective.setGeometryPoint(outcome.getPoint());
				}

				objective.setEvaluator(originalEvaluator);
				outcome = optimizer.optimize(
						GoalType.MINIMIZE,
						new ObjectiveFunction(objective),
						new MaxEval(objective.getMaxEvaluations()
								- objective.getNumberOfEvaluations()),
						MaxIter.unlimited(),
						new InitialGuess(objective.getInitialPoint()),
						new SimpleBounds(objective.getLowerBounds(), objective
								.getUpperBounds()));
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
		finalNorm = objective.calcNorm(errorVector);
		printErrors("Final error:  ", finalNorm, errorVector);
		System.out.print("Residual error ratio: ");
		System.out.println(finalNorm / initialNorm);
		long elapsedTime = System.currentTimeMillis() - startTime;
		double elapsedSeconds = 0.001 * (double) elapsedTime;
		System.out.print("Elapsed time: ");
		System.out.printf("%3.1f", elapsedSeconds);
		System.out.println(" seconds.");

		return true;
	} // optimizeObjectiveFunction

	/**
	 * Use a multi-start BOBYQA optimization to optimize a specified objective
	 * function. Use range processor and number of starts from {@code objective}
	 * if available; otherwise use a RandomRangeProcessor with 30 starts. If a
	 * single variable, use BrentOptimizer.
	 * 
	 * @param objective
	 *            - objective function to optimize
	 * @return best point and value found, or {@code null} if no good point
	 *         found.
	 */
	public static PointValuePair optimizeMultiStart(
			BaseObjectiveFunction objective, double[] startPoint)
	{
		AbstractRangeProcessor rangeProcessor = objective.getRangeProcessor();
		int nrStarts = 30;
		if (rangeProcessor == null)
		{
			rangeProcessor = new RandomRangeProcessor(
					objective.getLowerBounds(), objective.getUpperBounds(),
					null, nrStarts);
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
			if (totalEvaluations < maxEvaluations)
			{
				int runEvaluations = 0;
				System.out.print("Start " + (int) (startNr + 1) + ": ");
				try
				{
					int numVariables = objective.getNrDimensions();
					if (numVariables > 1) // Use BOBYQA
					{
						double trustRegion = objective
								.getInitialTrustRegionRadius(nextStart);
						double stoppingTrustRegion = objective
								.getStoppingTrustRegionRadius();
						BOBYQAOptimizer optimizer = new BOBYQAOptimizer(
								objective.getNrInterpolations(), trustRegion,
								stoppingTrustRegion);
						optima[startNr] = optimizer.optimize(GoalType.MINIMIZE,
								new ObjectiveFunction(objective), new MaxEval(
										maxEvaluations - totalEvaluations),
								MaxIter.unlimited(),
								new InitialGuess(nextStart),
								new SimpleBounds(objective.getLowerBounds(),
										objective.getUpperBounds()));
						runEvaluations = optimizer.getEvaluations();
					}
					else
					// Use Brent
					{
						BrentOptimizer optimizer = new BrentOptimizer(1.e-6,
								1.e-14);
						UnivariatePointValuePair outcome = optimizer.optimize(
								GoalType.MINIMIZE,
								new UnivariateObjectiveFunction(objective),
								new MaxEval(objective.getMaxEvaluations()),
								MaxIter.unlimited(), new SearchInterval(
										objective.getLowerBounds()[0],
										objective.getUpperBounds()[0],
										startPoint[0]));
						optima[startNr] = new PointValuePair(
								new double[] { outcome.getPoint() },
								outcome.getValue());
						runEvaluations = optimizer.getEvaluations();
					}
					double value = optima[startNr].getValue();
					if (value == Double.POSITIVE_INFINITY)
					{
						System.out.print("no valid solution found");
					}
					else
					{
						System.out.print("optimum "
								+ optima[startNr].getValue());
					}
				}
				catch (TooManyEvaluationsException e)
				{
					System.out.print("Exception: " + e.getMessage());
				}
				// Thrown by BOBYQA for no apparent reason: a bug?
				catch (NoSuchElementException e)
				{
					System.out.print("no valid solution found");
				}
				catch (Exception e)
				{
					System.out.print("Exception: " + e.getMessage());
					// e.printStackTrace();
				}
				finally
				{
					System.out.println(" at start point "
							+ Arrays.toString(nextStart));
				}

				totalEvaluations += runEvaluations;
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
	 * Sort PointValuePairs from best to worst, followed by {@code null}
	 * elements.
	 * 
	 * @param goal
	 *            - GoalType.MINIMIZE or GoalType.MAXIMIZE
	 * @param optima
	 *            - array of point-value pairs from successive optimizations.
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

	public static double getInitialNorm()
	{
		return initialNorm;
	}

	public static double getFinalNorm()
	{
		return finalNorm;
	}

	public static double getResidualErrorRatio()
	{
		return finalNorm / initialNorm;
	}
}