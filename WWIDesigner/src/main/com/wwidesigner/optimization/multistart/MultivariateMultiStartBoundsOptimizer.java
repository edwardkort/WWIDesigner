package com.wwidesigner.optimization.multistart;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.optimization.BaseMultivariateSimpleBoundsOptimizer;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.MultivariateOptimizer;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.BaseAbstractMultivariateSimpleBoundsOptimizer;

@Deprecated
public class MultivariateMultiStartBoundsOptimizer implements
		MultivariateOptimizer,
		BaseMultivariateSimpleBoundsOptimizer<MultivariateFunction>
{
	/** Underlying classical optimizer. */
	private final MultivariateOptimizer optimizer;
	/** Maximal number of evaluations allowed. */
	private int maxEvaluations;
	/** Number of evaluations already performed for all starts. */
	private int totalEvaluations;
	/** Number of starts to go. */
	private int starts;
	/** Random generator for multi-start. */
	private AbstractRangeProcessor generator;
	/** Found optima. */
	private PointValuePair[] optima;

	/**
	 * Create a multi-start optimizer from a single-start optimizer.
	 * 
	 * @param aOptimizer
	 *            Single-start optimizer to wrap.
	 * @param aStarts
	 *            Number of starts to perform (including the first one),
	 *            multi-start is disabled if value is less than or equal to 1.
	 * @param aGenerator
	 *            Random vector generator to use for restarts.
	 */
	public MultivariateMultiStartBoundsOptimizer(
			final MultivariateOptimizer aOptimizer, final int aStarts,
			final AbstractRangeProcessor aGenerator)
	{
		if (aOptimizer == null || aGenerator == null)
		{
			throw new NullArgumentException();
		}
		if (aStarts < 1)
		{
			throw new NotStrictlyPositiveException(aStarts);
		}

		this.optimizer = aOptimizer;
		this.starts = aStarts;
		this.generator = aGenerator;
	}

	/**
	 * Get all the optima found during the last call to
	 * {@link #optimize(int,MultivariateFunction,GoalType,double[]) optimize}.
	 * The optimizer stores all the optima found during a set of restarts. The
	 * {@link #optimize(int,MultivariateFunction,GoalType,double[]) optimize}
	 * method returns the best point only. This method returns all the points
	 * found at the end of each starts, including the best one already returned
	 * by the {@link #optimize(int,MultivariateFunction,GoalType,double[])
	 * optimize} method. <br/>
	 * The returned array as one element for each start as specified in the
	 * constructor. It is ordered with the results from the runs that did
	 * converge first, sorted from best to worst objective value (i.e in
	 * ascending order if minimizing and in descending order if maximizing),
	 * followed by and null elements corresponding to the runs that did not
	 * converge. This means all elements will be null if the
	 * {@link #optimize(int,MultivariateFunction,GoalType,double[]) optimize}
	 * method did throw an exception. This also means that if the first element
	 * is not {@code null}, it is the best point found across all starts.
	 * 
	 * @return an array containing the optima.
	 * @throws MathIllegalStateException
	 *             if
	 *             {@link #optimize(int,MultivariateFunction,GoalType,double[])
	 *             optimize} has not been called.
	 */
	public PointValuePair[] getOptima()
	{
		if (optima == null)
		{
			throw new MathIllegalStateException(
					LocalizedFormats.NO_OPTIMUM_COMPUTED_YET);
		}
		return optima.clone();
	}

	/** {@inheritDoc} */
	public int getMaxEvaluations()
	{
		return maxEvaluations;
	}

	/** {@inheritDoc} */
	public int getEvaluations()
	{
		return totalEvaluations;
	}

	/** {@inheritDoc} */
	public ConvergenceChecker<PointValuePair> getConvergenceChecker()
	{
		return optimizer.getConvergenceChecker();
	}

	/**
	 * {@inheritDoc}
	 */
	public PointValuePair optimize(int maxEval, final MultivariateFunction f,
			final GoalType goal, double[] startPoint)
	{
		maxEvaluations = maxEval;
		optima = new PointValuePair[starts];
		totalEvaluations = 0;
		generator.setStaticValues(startPoint);

		// Multi-start loop.
		for (int i = 0; i < starts; ++i)
		{
			// CHECKSTYLE: stop IllegalCatch
			try
			{
				optima[i] = optimizer.optimize(maxEval - totalEvaluations, f,
						goal,
						i == 0 ? startPoint : generator.nextVector());
			}
			catch (RuntimeException mue)
			{
				optima[i] = null;
			}
			// CHECKSTYLE: resume IllegalCatch

			totalEvaluations += optimizer.getEvaluations();
		}

		sortPairs(goal);

		if (optima[0] == null)
		{
			return new PointValuePair(startPoint, 10.0);
		}

		// Return the found point given the best objective function value.
		return optima[0];
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public PointValuePair optimize(int maxEval, MultivariateFunction f,
			GoalType goalType, double[] startPoint, double[] lowerBound,
			double[] upperBound)
	{
		if (optimizer instanceof BaseAbstractMultivariateSimpleBoundsOptimizer)
		{
			maxEvaluations = maxEval;
			optima = new PointValuePair[starts];
			totalEvaluations = 0;
			generator.setStaticValues(startPoint);

			// Multi-start loop.
			for (int i = 0; i < starts; ++i)
			{
				// CHECKSTYLE: stop IllegalCatch
				try
				{
					double[] start = i == 0 ? startPoint : generator
							.nextVector();
					optima[i] = ((BaseAbstractMultivariateSimpleBoundsOptimizer) optimizer)
							.optimize(maxEval - totalEvaluations, f, goalType,
									start, lowerBound, upperBound);
					System.out.println("Start " + (int) (i + 1) + ", start: "
							+ Arrays.toString(start) + ", optimum: " + optima[i].getValue());
				}
				catch (RuntimeException mue)
				{
					optima[i] = null;
					System.out.println("Start " + (int) (i + 1) + " failed, " + mue);
				}
				// CHECKSTYLE: resume IllegalCatch

				totalEvaluations += optimizer.getEvaluations();
			}

			sortPairs(goalType);

			if (optima[0] == null)
			{
				return new PointValuePair(startPoint, 10.0);
			}

			// Return the found point given the best objective function value.
			System.out.println("Best optimum: " + optima[0].getValue());
			return optima[0];
		}
		else
		{
			return optimize(maxEval, f, goalType, startPoint);
		}
	}

	/**
	 * Sort the optima from best to worst, followed by {@code null} elements.
	 * 
	 * @param goal
	 *            Goal type.
	 */
	private void sortPairs(final GoalType goal)
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