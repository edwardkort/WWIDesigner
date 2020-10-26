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
import org.apache.commons.math3.exception.ZeroException;
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

import com.wwidesigner.math.DIRECTCOptimizer;
import com.wwidesigner.math.DIRECTOptimizer;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.optimization.multistart.AbstractRangeProcessor;
import com.wwidesigner.optimization.multistart.RandomRangeProcessor;
import com.wwidesigner.util.OperationCancelledException;

public class ObjectiveFunctionOptimizer
{
	// Statistics saved from the most recent call to optimizeObjectiveFunction

	protected static double initialNorm; // Initial value of objective function.
	protected static double finalNorm; // Final value of objective function.
	protected static final boolean DEBUG_MODE = false;

	// Number of evaluations in a single multi-start run.
	private static int singleRunEvaluations;

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
		System.out.print("\nSystem has ");
		System.out.print(objective.getNrDimensions());
		System.out.print(" optimization variables and ");
		System.out.print(objective.getNrNotes());
		System.out.print(" target notes.");

		if (objective.getNrDimensions() == 0)
		{
			throw new ZeroException();
		}

		long startTime = System.currentTimeMillis();
		double[] startPoint = objective.getInitialPoint();
		double[] errorVector = objective.getErrorVector(startPoint);
		initialNorm = objective.calcNorm(errorVector);
		System.out.println();
		printErrors("Initial error: ", initialNorm, errorVector);
		finalNorm = initialNorm;

		try
		{
			if (objective.isMultiStart())
			{
				// Make the startPoint from the optimization result of a
				// DIRECT/BOBYQA run.
				PointValuePair outcome;
				MultivariateOptimizer optimizer = new DIRECTCOptimizer(6.0e-6);
				outcome = runDirect(optimizer, objective, startPoint);
				System.out.println(
						"After global optimizer, error: " + outcome.getValue());

				outcome = runBobyqa(objective, outcome.getPoint());
				System.out
						.println("Refined start, error: " + outcome.getValue());

				outcome = optimizeMultiStart(objective, outcome.getPoint());
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
					.equals(BaseObjectiveFunction.OptimizerType.BrentOptimizer))
			{
				// Univariate optimization.
				UnivariatePointValuePair outcome = runBrent(objective,
						startPoint);
				double[] geometry = new double[1];
				geometry[0] = outcome.getPoint();
				objective.setGeometryPoint(geometry);
			}
			else if (optimizerType.equals(
					BaseObjectiveFunction.OptimizerType.PowellOptimizer))
			{
				// Multivariate optimization, without bounds.
				PointValuePair outcome = runPowell(objective, startPoint);
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if (optimizerType.equals(
					BaseObjectiveFunction.OptimizerType.SimplexOptimizer))
			{
				// Multivariate optimization, without bounds.
				PointValuePair outcome = runSimplex(objective, startPoint);
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if (optimizerType
					.equals(BaseObjectiveFunction.OptimizerType.CMAESOptimizer))
			{
				// Multivariate optimization, with bounds.
				PointValuePair outcome = runCmaes(objective, startPoint);
				objective.setGeometryPoint(outcome.getPoint());
			}
			else if (optimizerType.equals(
					BaseObjectiveFunction.OptimizerType.DIRECTOptimizer))
			{
				// Multivariate DIRECT optimization, with bounds.
				// Convergence threshold about 3^-15.
				MultivariateOptimizer optimizer = new DIRECTCOptimizer(7.0e-8);
				PointValuePair outcome = runDirect(optimizer, objective,
						startPoint);

				System.out.println("After " + objective.getNumberOfEvaluations()
						+ " evaluations, global optimizer found optimum "
						+ outcome.getValue());

				// Use BOBYQA to refine global optimum found.
				PointValuePair outcome2 = runBobyqa(objective,
						outcome.getPoint());
				if (outcome.getValue() < outcome2.getValue())
				{
					// Don't use second-stage optimum if it isn't better.
					System.out.println("Second-stage optimizer found optimum "
							+ outcome2.getValue());
					objective.setGeometryPoint(outcome.getPoint());
				}
				else
				{
					objective.setGeometryPoint(outcome2.getPoint());
				}
			}
			else
			{
				// Multivariate BOBYQA optimization, with bounds.
				PointValuePair outcome = runBobyqa(objective, startPoint);
				objective.setGeometryPoint(outcome.getPoint());
			}
		}
		catch (TooManyEvaluationsException e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
		catch (OperationCancelledException e)
		{
			if (objective.isMultiStart())
			{
				System.out.println("\nOptimization cancelled.\n");
				return false;
			}
			System.out.println(
					"\nOptimization cancelled.\nPartially-optimized result returned.\n");
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
	 * single variable, use BrentOptimizer. Multi-start recognizes
	 * objective.isRunTwoStageOptimization (if set true), doing the 30 starts
	 * with the first stage evaluator, and then doing a final run with the
	 * original evaluator - starting with the best result from the 30 starts.
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
		EvaluatorInterface originalEvaluator = objective.getEvaluator();
		if (objective.isRunTwoStageOptimization())
		{
			EvaluatorInterface firstStageEvaluator = objective
					.getFirstStageEvaluator();
			System.out.println("Evaluator: "
					+ firstStageEvaluator.getClass().getSimpleName());
			objective.setEvaluator(firstStageEvaluator);
		}

		for (int startNr = 0; startNr < nrStarts; ++startNr)
		{
			if (totalEvaluations < maxEvaluations)
			{
				System.out.print("Start " + (startNr + 1) + ": ");
				optima[startNr] = doSingleStart(objective, startPoint,
						maxEvaluations - totalEvaluations, nextStart);
				nextStart = rangeProcessor.nextVector();
				totalEvaluations += singleRunEvaluations;
			}
		}

		sortPairs(GoalType.MINIMIZE, optima);

		if (optima[0] != null)
		{
			// Return the found point given the best objective function value.
			System.out.println("Best optimum: " + optima[0].getValue());
			if (objective.isRunTwoStageOptimization())
			{
				System.out.println("Final run with evaluator: "
						+ originalEvaluator.getClass().getSimpleName());
				objective.setEvaluator(originalEvaluator);
				optima[0] = doSingleStart(objective, startPoint,
						objective.getMaxEvaluations() / 30,
						optima[0].getPoint());
			}
		}

		return optima[0];
	}

	protected static PointValuePair doSingleStart(
			BaseObjectiveFunction objective, double[] startPoint,
			int maxEvaluations, double[] nextStart)
	{
		singleRunEvaluations = 0;
		PointValuePair result = null;
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
				result = runBobyqa(optimizer, objective, nextStart,
						maxEvaluations);
				singleRunEvaluations = optimizer.getEvaluations();
			}
			else
			// Use Brent
			{
				BrentOptimizer optimizer = new BrentOptimizer(1.e-6, 1.e-14);
				UnivariatePointValuePair outcome = runBrent(optimizer,
						objective, startPoint);
				result = new PointValuePair(new double[] { outcome.getPoint() },
						outcome.getValue());
				singleRunEvaluations = optimizer.getEvaluations();
			}
			double value = result.getValue();
			if (value == Double.POSITIVE_INFINITY)
			{
				System.out.print("no valid solution found");
			}
			else
			{
				System.out.print("optimum " + result.getValue());
			}
		}
		catch (TooManyEvaluationsException e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
		// Thrown by BOBYQA for no apparent reason: a bug?
		catch (NoSuchElementException e)
		{
			System.out.println("no valid solution found");
		}
		catch (OperationCancelledException e)
		{
			// Restore starting point.
			objective.setGeometryPoint(startPoint);
			// Re-throw the exception to give up the whole multi-start
			// optimization.
			throw new OperationCancelledException(e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			// e.printStackTrace();
		}
		finally
		{
			System.out.println(" at start point " + Arrays.toString(nextStart));
		}

		return result;
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
	protected static void sortPairs(final GoalType goal,
			PointValuePair[] optima)
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

	protected static UnivariatePointValuePair runBrent(BrentOptimizer optimizer,
			BaseObjectiveFunction objective, double[] startPoint)
			throws TooManyEvaluationsException
	{
		UnivariatePointValuePair outcome;
		outcome = optimizer.optimize(GoalType.MINIMIZE,
				new UnivariateObjectiveFunction(objective),
				new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
				new SearchInterval(objective.getLowerBounds()[0],
						objective.getUpperBounds()[0], startPoint[0]));

		return outcome;
	}

	protected static UnivariatePointValuePair runBrent(
			BaseObjectiveFunction objective, double[] startPoint)
			throws TooManyEvaluationsException
	{
		BrentOptimizer optimizer = new BrentOptimizer(1.e-6, 1.e-14);

		return runBrent(optimizer, objective, startPoint);
	}

	protected static PointValuePair runBobyqa(BOBYQAOptimizer optimizer,
			BaseObjectiveFunction objective, double[] startPoint,
			int maxEvaluations) throws TooManyEvaluationsException
	{
		double[] thisStartPoint = startPoint;
		PointValuePair outcome;
		EvaluatorInterface originalEvaluator = objective.getEvaluator();
		if (objective.isRunTwoStageOptimization())
		{
			objective.setEvaluator(objective.getFirstStageEvaluator());
			outcome = optimizer.optimize(GoalType.MINIMIZE,
					new ObjectiveFunction(objective),
					new MaxEval(maxEvaluations), MaxIter.unlimited(),
					new InitialGuess(startPoint),
					new SimpleBounds(objective.getLowerBounds(),
							objective.getUpperBounds()));
			objective.setGeometryPoint(outcome.getPoint());
			thisStartPoint = objective.getInitialPoint();
		}

		objective.setEvaluator(originalEvaluator);
		outcome = optimizer.optimize(GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(maxEvaluations - optimizer.getEvaluations()),
				MaxIter.unlimited(), new InitialGuess(thisStartPoint),
				new SimpleBounds(objective.getLowerBounds(),
						objective.getUpperBounds()));

		return outcome;
	}

	protected static PointValuePair runBobyqa(BaseObjectiveFunction objective,
			double[] startPoint) throws TooManyEvaluationsException
	{
		double trustRegion = objective.getInitialTrustRegionRadius(startPoint);
		double stoppingTrustRegion = objective.getStoppingTrustRegionRadius();
		BOBYQAOptimizer optimizer = new BOBYQAOptimizer(
				objective.getNrInterpolations(), trustRegion,
				stoppingTrustRegion);

		return runBobyqa(optimizer, objective, startPoint,
				objective.getMaxEvaluations());
	}

	protected static PointValuePair runPowell(BaseObjectiveFunction objective,
			double[] startPoint) throws TooManyEvaluationsException
	{
		PowellOptimizer optimizer = new PowellOptimizer(1.e-6, 1.e-14);

		return optimizer.optimize(GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
				new InitialGuess(startPoint));
	}

	protected static PointValuePair runSimplex(BaseObjectiveFunction objective,
			double[] startPoint) throws TooManyEvaluationsException
	{
		// Rely on relative difference to test convergence,
		// to allow for vast differences in absolute error scale
		// between objective functions.
		ConvergenceChecker<PointValuePair> convergenceChecker = new SimpleValueChecker(
				1.e-6, 1.e-14);
		SimplexOptimizer optimizer = new SimplexOptimizer(convergenceChecker);
		MultiDirectionalSimplex simplex = new MultiDirectionalSimplex(
				objective.getSimplexStepSize());

		return optimizer.optimize(GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
				new InitialGuess(startPoint), simplex);
	}

	protected static PointValuePair runCmaes(BaseObjectiveFunction objective,
			double[] startPoint) throws TooManyEvaluationsException
	{
		// Rely on relative difference to test convergence,
		// to allow for vast differences in absolute error scale
		// between objective functions.
		ConvergenceChecker<PointValuePair> convergenceChecker = new SimpleValueChecker(
				1.e-6, 1.e-14);
		MultivariateOptimizer optimizer = new CMAESOptimizer(
				objective.getMaxEvaluations(), 0.0001 * initialNorm, true, 0, 0,
				new MersenneTwister(), false, convergenceChecker);

		return optimizer.optimize(GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(objective.getMaxEvaluations()), MaxIter.unlimited(),
				new InitialGuess(startPoint),
				new SimpleBounds(objective.getLowerBounds(),
						objective.getUpperBounds()),
				new CMAESOptimizer.PopulationSize(
						objective.getNrInterpolations()),
				new CMAESOptimizer.Sigma(objective.getStdDev()));
	}

	protected static PointValuePair runDirect(MultivariateOptimizer optimizer,
			BaseObjectiveFunction objective, double[] startPoint)
			throws TooManyEvaluationsException
	{
		PointValuePair outcome;

		// Run optimization first with the first-stage evaluator, if
		// specified
		EvaluatorInterface originalEvaluator = objective.getEvaluator();
		if (objective.isRunTwoStageOptimization())
		{
			objective.setEvaluator(objective.getFirstStageEvaluator());
		}
		// Specify a target function value, to guard against
		// underconstrained
		// optimizations. Value here should be suitable for
		// CentsDeviationEvaluator,
		// and adequate for most other evaluators.
		outcome = optimizer.optimize(GoalType.MINIMIZE,
				new ObjectiveFunction(objective),
				new MaxEval(2 * objective.getMaxEvaluations()),
				MaxIter.unlimited(), new InitialGuess(startPoint),
				new DIRECTOptimizer.TargetFunctionValue(0.001),
				new SimpleBounds(objective.getLowerBounds(),
						objective.getUpperBounds()));
		objective.setEvaluator(originalEvaluator);

		return outcome;
	}
}
