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
import com.wwidesigner.util.OperationCancelledException;

/**
 * Base class for optimization objective functions. Each derived class supports
 * optimization of specific aspects of an instrument geometry.
 * 
 * @author Burton Patkau
 */
public abstract class BaseObjectiveFunction
		implements MultivariateFunction, UnivariateFunction
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
		BrentOptimizer, BOBYQAOptimizer, CMAESOptimizer, DIRECTOptimizer, SimplexOptimizer, PowellOptimizer
	}

	protected OptimizerType optimizerType;
	protected int maxEvaluations; // Limit on number of error norm calculations.
	protected AbstractRangeProcessor rangeProcessor;
	protected Double initialTrustRegionRadius; // Used by the BOBYQA optimizer
												// for
												// processing and termination
												// logic.
	protected boolean cancel;

	// Statistics for the results of an optimization.
	protected int tuningsDone; // Number of tuning error calculations.
	protected int evaluationsDone; // Number of calculations of error norm.

	protected boolean runTwoStageOptimization = false;

	/**
	 * The constructor sets what is to be optimized.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public BaseObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
	{
		this.calculator = aCalculator;
		this.fingeringTargets = tuning.getFingering();
		this.evaluator = aEvaluator;
		nrDimensions = 0;
		optimizerType = OptimizerType.BOBYQAOptimizer;
		maxEvaluations = 10000;
		rangeProcessor = null;
		cancel = false;
		evaluationsDone = 0;
		tuningsDone = 0;
		constraints = new Constraints(
				aCalculator.getInstrument().getLengthType());
	}

	/**
	 * The multivariate objective function to be optimized, a sum of squares of
	 * the error value specific to the derived class.
	 * 
	 * @param point
	 *            - geometry values to test. point.length == nrDimensions.
	 * @return value of objective function at the specified point.
	 * 
	 * @see org.apache.commons.math3.analysis.MultivariateFunction#value(double[])
	 */
	@Override
	public double value(double[] point)
	{
		++evaluationsDone;
		double[] errorVector = getErrorVector(point);
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
	 * @param aEvaluator
	 */
	public void setEvaluator(EvaluatorInterface aEvaluator)
	{
		this.evaluator = aEvaluator;
	}

	public EvaluatorInterface getEvaluator()
	{
		return evaluator;
	}

	public EvaluatorInterface getFirstStageEvaluator()
	{
		return firstStageEvaluator;
	}

	public void setFirstStageEvaluator(EvaluatorInterface aFirstStageEvaluator)
	{
		this.firstStageEvaluator = aFirstStageEvaluator;
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
	 * @throws OperationCancelledException
	 * @throws DimensionMismatchException.
	 */
	public double[] getErrorVector(double[] point)
	{
		if (cancel)
		{
			cancel = false;
			throw new OperationCancelledException("Operation cancelled.");
		}
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

	public void setRunTwoStageOptimization(boolean aRunTwoStageOptimization)
	{
		this.runTwoStageOptimization = aRunTwoStageOptimization;
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
				|| isMultiStart()
				|| optimizerType.equals(OptimizerType.DIRECTOptimizer))
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
	 * From the bounds and the initial value, determine a recommended
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
	 * From the bounds and the initial value, determine a recommended value for
	 * the initial trust region radius: about one tenth of the greatest expected
	 * change to a variable, which we take as the difference
	 * upperBound - lowerBound, with a minimum value of half the smallest
	 * difference upperBound - lowerBound (excluding nearly-equal bounds).
	 */
	public double getInitialTrustRegionRadius(double[] initial)
	{
		double maxExpectedChange;
		double minRadius;
		double boundDifference;

		maxExpectedChange = 0.0;
		minRadius = 1.0e-6;
		for (int i = 0; i < nrDimensions; ++i)
		{
			boundDifference = upperBounds[i] - lowerBounds[i];
			if (boundDifference > 1.0e-7
					&& 0.5 * boundDifference < minRadius)
			{
				minRadius = 0.5 * boundDifference;
			}
			if (boundDifference > maxExpectedChange)
			{
				maxExpectedChange = boundDifference;
			}
		}
		if (minRadius > 0.1 * maxExpectedChange)
		{
			initialTrustRegionRadius = minRadius;
		}
		else
		{
			initialTrustRegionRadius = 0.1 * maxExpectedChange;
		}

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

	public void setLowerBounds(double[] aLowerBounds)
	{
		if (aLowerBounds.length != nrDimensions)
		{
			throw new DimensionMismatchException(aLowerBounds.length,
					nrDimensions);
		}
		this.lowerBounds = aLowerBounds.clone();
		validateBounds();
		// Reset initialTrustRadius
		initialTrustRegionRadius = null;
	}

	public void setUpperBounds(double[] aUpperBounds)
	{
		if (aUpperBounds.length != nrDimensions)
		{
			throw new DimensionMismatchException(aUpperBounds.length,
					nrDimensions);
		}
		this.upperBounds = aUpperBounds.clone();
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
		// if (isMultiStart())
		// {
		// return OptimizerType.MultiStartOptimizer;
		// }
		return optimizerType;
	}

	public void setOptimizerType(OptimizerType aOptimizerType)
	{
		this.optimizerType = aOptimizerType;
	}

	public int getMaxEvaluations()
	{
		return maxEvaluations;
	}

	public void setMaxEvaluations(int aMaxEvaluations)
	{
		this.maxEvaluations = aMaxEvaluations;
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

	public void setRangeProcessor(AbstractRangeProcessor aRangeProcessor)
	{
		this.rangeProcessor = aRangeProcessor;
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
	 * @param aConstraints
	 * @throws Exception
	 *             If the constraints are invalid for this optimizer.
	 */
	public void setConstraintsBounds(Constraints aConstraints) throws Exception
	{
		lowerBounds = aConstraints.getLowerBounds();
		upperBounds = aConstraints.getUpperBounds();
		setConstraints(aConstraints);
		validateBounds();
	}

	/**
	 * Sets the member constraints from an externally derive one.
	 * 
	 * @param aConstraints
	 */
	public void setConstraints(Constraints aConstraints)
	{
		this.constraints = aConstraints;
	}

	/**
	 * Creates the Constraints unique to a given ObjectiveFunction.
	 */
	abstract protected void setConstraints();

	/**
	 * Set cancel to true to cancel an optimization.
	 */
	public void setCancel(boolean aCancel)
	{
		this.cancel = aCancel;
	}

	/**
	 * Currently, multi-start optimization makes no sense with the DIRECT
	 * optimizer selected. Check for this combination. Other combinations may be
	 * added in the future.
	 * 
	 * @param anOptimizerType - optimizer type to be tested
	 * @return False if multi-start optimization and DIRECT optimizer, true
	 *         otherwise.
	 */
	public boolean isOptimizerMatch(OptimizerType anOptimizerType)
	{
		if (isMultiStart()
				&& anOptimizerType.equals(OptimizerType.DIRECTOptimizer))
		{
			return false;
		}

		return true;
	}

}
