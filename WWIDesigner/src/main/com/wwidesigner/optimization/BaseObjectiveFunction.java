/**
 * Base class to represent an objective function to be optimized.
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

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.util.FastMath;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.multistart.AbstractRangeProcessor;

/**
 * Base class for optimization objective functions. Each derived class supports
 * optimization of specific aspects of an instrument geometry.
 * 
 * @author Burton Patkau
 */
public abstract class BaseObjectiveFunction implements MultivariateFunction,
		UnivariateFunction
{
	// The Intent denotes the use of the ObjectiveFunction based on it
	// Constraints
	// content.
	public static final int OPTIMIZATION_INTENT = 0;
	public static final int BLANK_CONSTRAINTS_INTENT = 1;
	public static final int DEFAULT_CONSTRAINTS_INTENT = 2;

	// Information about what is to be optimized.
	protected InstrumentCalculator calculator;
	protected List<Fingering> fingeringTargets;
	protected EvaluatorInterface evaluator;
	protected EvaluatorInterface firstStageEvaluator;

	// Description of the geometry that this particular objective function
	// supports.
	protected int nrDimensions; // Number of geometry values.
								// Constant for each derived class.
	protected double[] lowerBounds; // Lower bound for each geometry value.
	protected double[] upperBounds; // Upper bound for each geometry value.
	protected Constraints constraints; // Description of bounds.

	// Recommended optimization method.
	public enum OptimizerType
	{
		BrentOptimizer, BOBYQAOptimizer, CMAESOptimizer, MultiStartOptimizer, SimplexOptimizer, PowellOptimizer
	}

	protected OptimizerType optimizerType;
	protected int maxEvaluations; // Limit on number of error norm calculations.
	protected AbstractRangeProcessor rangeProcessor;
	protected Double initialTrustRegionRadius; // Used by the BOBYQA optimizer
												// for
												// processing and termination
												// logic.

	// Statistics for the results of an optimization.
	protected int tuningsDone; // Number of tuning error calculations.
	protected int evaluationsDone; // Number of calculations of error norm.

	protected boolean runTwoStageOptimization = false;

	/**
	 * The constructor sets what is to be optimized.
	 * 
	 * @param calculator
	 * @param tuning
	 * @param evaluator
	 */
	public BaseObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		this.calculator = calculator;
		this.fingeringTargets = tuning.getFingering();
		this.evaluator = evaluator;
		nrDimensions = 1;
		optimizerType = OptimizerType.BOBYQAOptimizer;
		maxEvaluations = 5000;
		rangeProcessor = null;
		evaluationsDone = 0;
		tuningsDone = 0;
		constraints = new Constraints(calculator.getInstrument()
				.getLengthType());
	}

	/**
	 * The multivariate objective function to be optimized, a sum of squares of
	 * the error value specific to the derived class.
	 * 
	 * @param point
	 *            - geometry values to test. point.length == nrDimensions.
	 * @return value of objective function at the specified point.
	 * 
	 * @see org.apache.commons.math3.analysis.MultivariateFunction#value(double*
	 *      [])
	 */
	@Override
	public double value(double[] point)
	{
		double[] errorVector = getErrorVector(point);
		++evaluationsDone;
		tuningsDone += errorVector.length;
		return calcNorm(errorVector);
	}

	/**
	 * The univariate objective function to be optimized, a sum of squares of
	 * the error value specific to the derived class.
	 * 
	 * Requires nrDimensions == 1.
	 * 
	 * @param point
	 *            - geometry value to test.
	 * @return value of objective function at the specified point.
	 * 
	 * @see org.apache.commons.math3.analysis.UnivariateFunction#value(double)
	 */
	@Override
	public double value(double point)
	{
		double[] geometry = new double[1];
		geometry[0] = point;
		return value(geometry);
	}

	/**
	 * Supports the changing of the evaluator at any time.
	 * 
	 * @param evaluator
	 */
	public void setEvaluator(EvaluatorInterface evaluator)
	{
		this.evaluator = evaluator;
	}

	public EvaluatorInterface getEvaluator()
	{
		return evaluator;
	}

	public EvaluatorInterface getFirstStageEvaluator()
	{
		return firstStageEvaluator;
	}

	public void setFirstStageEvaluator(EvaluatorInterface firstStageEvaluator)
	{
		this.firstStageEvaluator = firstStageEvaluator;
	}

	public InstrumentCalculator getCalculator()
	{
		return calculator;
	}

	/**
	 * Calculate errors at each fingering target.
	 * 
	 * @param point
	 *            - geometry values to test. point.length == nrDimensions.
	 * @return array of error values, one for each fingering target.
	 * @throws DimensionMismatchException.
	 */
	public double[] getErrorVector(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		setGeometryPoint(point);
		double[] errorVector = evaluator.calculateErrorVector(fingeringTargets);
		return errorVector;
	}

	/**
	 * Calculate an error norm from an error vector, as the sum of squares.
	 * Weight each squared error by the optimization weight from each Fingering.
	 * 
	 * @param errorVector
	 * @return sum of squared errors
	 */
	public double calcNorm(double[] errorVector)
	{
		double norm = 0.0;
		for (int i = 0; i < errorVector.length; i++)
		{
			double err = errorVector[i];
			int weight = fingeringTargets.get(i).getOptimizationWeight();
			if (weight > 0)
			{
				norm += err * err * weight;
			}
		}

		return norm;
	}

	public boolean isRunTwoStageOptimization()
	{
		return runTwoStageOptimization && firstStageEvaluator != null;
	}

	public void setRunTwoStageOptimization(boolean runTwoStageOptimization)
	{
		this.runTwoStageOptimization = runTwoStageOptimization;
	}

	/**
	 * Retrieve physical geometry values from the instrument. Specific values
	 * depend on the derived class.
	 * 
	 * @return point representing current physical geometry values. point.length
	 *         == nrDimensions.
	 */
	public abstract double[] getGeometryPoint();

	/**
	 * Set physical geometry values for an instrument. Specific values depend on
	 * the derived class.
	 * 
	 * @param point
	 *            - physical geometry values to set. point.length ==
	 *            nrDimensions.
	 * @throws DimensionMismatchException.
	 */
	public abstract void setGeometryPoint(double[] point);

	/**
	 * Retrieve geometry values from the instrument, ensuring the values lie
	 * within the current bounds. Specific values depend on the derived class.
	 * 
	 * @return point representing current geometry values. lowerBounds[i] <=
	 *         point[i] <= upperBounds[i]. point.length == nrDimensions.
	 */
	public double[] getInitialPoint()
	{
		double[] unnormalized = this.getGeometryPoint();
		double[] normalized = new double[unnormalized.length];

		for (int i = 0; i < unnormalized.length; i++)
		{
			if (unnormalized[i] <= lowerBounds[i])
			{
				normalized[i] = lowerBounds[i];
			}
			else if (unnormalized[i] >= upperBounds[i])
			{
				normalized[i] = upperBounds[i];
			}
			else
			{
				normalized[i] = unnormalized[i];
			}
		}
		return normalized;
	}

	/**
	 * From the number of dimensions, propose a useful number of interpolations
	 * if the optimizer type requires it.
	 */
	public int getNrInterpolations()
	{
		if (optimizerType.equals(OptimizerType.CMAESOptimizer))
		{
			// Typical population size used for CMAES.
			// return 4 + (int) (3 * Math.log(nrDimensions));
			return 5 + (int) (5 * FastMath.log(nrDimensions));
		}

		if (optimizerType.equals(OptimizerType.BOBYQAOptimizer)
				|| optimizerType.equals(OptimizerType.MultiStartOptimizer))
		{
			// Largest recommended value for BOBYQA.
			return 2 * nrDimensions + 1;
		}
		// Not required for other optimizers.
		return 1;
	}

	/**
	 * From the bounds, propose a useful standard deviation for each dimension,
	 * should the optimizer type require it (CMAES).
	 */
	public double[] getStdDev()
	{
		double[] sigma = new double[nrDimensions];
		for (int i = 0; i < nrDimensions; i++)
		{
			if (upperBounds[i] <= lowerBounds[i])
			{
				sigma[i] = 0.0;
			}
			else
			{
				sigma[i] = 0.2 * (upperBounds[i] - lowerBounds[i]);
			}
		}
		return sigma;
	}

	/**
	 * From the bounds and the initial value, determine the maximum feasible
	 * value for the initial trust region radius.
	 */
	public double getInitialTrustRegionRadius()
	{
		if (initialTrustRegionRadius == null)
		{
			double initial[] = getInitialPoint();
			getInitialTrustRegionRadius(initial);
		}

		return initialTrustRegionRadius;
	}

	public double getStoppingTrustRegionRadius()
	{
		return 1.e-8 * getInitialTrustRegionRadius();
	}

	/**
	 * From the bounds and the initial value, determine the maximum feasible
	 * value for the initial trust region radius.
	 */
	public double getInitialTrustRegionRadius(double[] initial)
	{
		double minRadius = 1.0;
		double minDimensionRadius;

		for (int i = 0; i < nrDimensions; ++i)
		{
			// For each dimension, the radius should not be more than
			// the distance from the initial point to either bound,
			// but let it be at least 10% of the distance
			// between the bounds.
			minDimensionRadius = initial[i] - lowerBounds[i];
			if (minDimensionRadius > upperBounds[i] - initial[i])
			{
				minDimensionRadius = upperBounds[i] - initial[i];
			}
			if (minDimensionRadius < 0.1 * (upperBounds[i] - lowerBounds[i]))
			{
				minDimensionRadius = 0.1 * (upperBounds[i] - lowerBounds[i]);
			}
			if (minDimensionRadius < minRadius)
			{
				minRadius = minDimensionRadius;
			}
		}
		initialTrustRegionRadius = minRadius;

		return initialTrustRegionRadius;
	}

	/**
	 * From the bounds and the initial value, generate suggested side lengths in
	 * each direction for a simplex.
	 */
	public double[] getSimplexStepSize()
	{
		double[] stepSize = new double[nrDimensions];
		double initial[] = getInitialPoint();

		for (int i = 0; i < nrDimensions; ++i)
		{
			// For each dimension, the step size will be part-way
			// to the more distant bound.
			stepSize[i] = upperBounds[i] - initial[i];
			if (stepSize[i] < initial[i] - lowerBounds[i])
			{
				// Step size is negative, toward lower bound.
				stepSize[i] = lowerBounds[i] - initial[i];
			}
			stepSize[i] = 0.25 * stepSize[i];
			if (stepSize[i] == 0.0)
			{
				stepSize[i] = 0.1 * initial[i];
			}
		}
		return stepSize;
	}

	public Instrument getInstrument()
	{
		return calculator.getInstrument();
	}

	public double[] getLowerBounds()
	{
		return lowerBounds;
	}

	public void setLowerBounds(double[] lowerBounds)
	{
		if (lowerBounds.length != nrDimensions)
		{
			throw new DimensionMismatchException(lowerBounds.length,
					nrDimensions);
		}
		this.lowerBounds = lowerBounds.clone();
		validateBounds();
		// Reset initialTrustRadius
		initialTrustRegionRadius = null;
	}

	public void setUpperBounds(double[] upperBounds)
	{
		if (upperBounds.length != nrDimensions)
		{
			throw new DimensionMismatchException(upperBounds.length,
					nrDimensions);
		}
		this.upperBounds = upperBounds.clone();
		validateBounds();
		// Reset initialTrustRadius
		initialTrustRegionRadius = null;
	}

	/**
	 * Support the input bounds being reversed (lower bound > upper bound) or
	 * equal. Rebuilds the bounds arrays.
	 */
	protected void validateBounds()
	{
		if (lowerBounds != null && upperBounds != null)
		{
			for (int i = 0; i < nrDimensions; i++)
			{
				double lb = lowerBounds[i];
				double ub = upperBounds[i];
				if (lb > ub)
				{
					lowerBounds[i] = ub;
					upperBounds[i] = lb;
				}
				// Subtract a small amount from the lower bound so that the
				// optimizer sees the range as non-zero.
				else if (lb == ub)
				{
					lowerBounds[i] = lb - 1.e-7;
				}
			}
			constraints.setLowerBounds(lowerBounds);
			constraints.setUpperBounds(upperBounds);
		}
		else if (lowerBounds != null)
		{
			constraints.setLowerBounds(lowerBounds);
		}
		else if (upperBounds != null)
		{
			constraints.setUpperBounds(upperBounds);
		}
	}

	public int getNrDimensions()
	{
		return nrDimensions;
	}

	/**
	 * 
	 * @return The number of Fingerings that have a positive optimization
	 *         weight.
	 */
	public int getNrNotes()
	{
		int weightedNotes = 0;
		for (Fingering fingering : fingeringTargets)
		{
			if (fingering.getOptimizationWeight() > 0)
			{
				weightedNotes++;
			}
		}

		return weightedNotes;
	}

	public double[] getUpperBounds()
	{
		return upperBounds;
	}

	public OptimizerType getOptimizerType()
	{
		if (isMultiStart())
		{
			return OptimizerType.MultiStartOptimizer;
		}
		return optimizerType;
	}

	public void setOptimizerType(OptimizerType optimizerType)
	{
		this.optimizerType = optimizerType;
	}

	public int getMaxEvaluations()
	{
		return maxEvaluations;
	}

	public void setMaxEvaluations(int maxEvaluations)
	{
		this.maxEvaluations = maxEvaluations;
	}

	public boolean isMultiStart()
	{
		if (rangeProcessor != null)
		{
			return true;
		}
		return false;
	}

	public AbstractRangeProcessor getRangeProcessor()
	{
		return rangeProcessor;
	}

	public void setRangeProcessor(AbstractRangeProcessor rangeProcessor)
	{
		this.rangeProcessor = rangeProcessor;
	}

	public int getNumberOfEvaluations()
	{
		return evaluationsDone;
	}

	public int getNumberOfTunings()
	{
		return tuningsDone;
	}

	public Constraints getConstraints()
	{
		return constraints;
	}

	/**
	 * Sets the lower and upper bounds using values contained in the
	 * constraints.
	 * 
	 * @param constraints
	 * @throws Exception
	 *             If the constraints are invalid for this optimizer.
	 */
	public void setConstraintsBounds(Constraints constraints) throws Exception
	{
		lowerBounds = constraints.getLowerBounds();
		upperBounds = constraints.getUpperBounds();
		setConstraints(constraints);
		validateBounds();
	}

	/**
	 * Sets the member constraints from an externally derive one.
	 * 
	 * @param constraints
	 */
	public void setConstraints(Constraints constraints)
	{
		this.constraints = constraints;
	}

	/**
	 * Creates the Constraints unique to a given ObjectiveFunction.
	 */
	abstract protected void setConstraints();

}
