package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;

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
	// Information about what is to be optimized.
	protected InstrumentCalculator calculator;
	protected List<Fingering> fingeringTargets;
	protected EvaluatorInterface evaluator;

	// Description of the geometry that this particular objective function
	// supports.
	protected int nrDimensions; // Number of geometry values.
								// Constant for each derived class.
	protected double[] lowerBounds; 		// Lower bound for each geometry value.
	protected double[] upperBounds; 		// Upper bound for each geometry value.
	protected Constraints constraints; 		// Description of bounds.

	// Recommended optimization method.
	public enum OptimizerType
	{
		BrentOptimizer, BOBYQAOptimizer, CMAESOptimizer, MultiStartOptimizer, SimplexOptimizer, PowellOptimizer
	}

	protected OptimizerType optimizerType;
	protected int maxEvaluations;		// Limit on number of error norm calculations.
	protected AbstractRangeProcessor rangeProcessor;

	// Statistics for the results of an optimization.
	protected int tuningsDone; 		// Number of tuning error calculations.
	protected int evaluationsDone; 	// Number of calculations of error norm.

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
	 * @param point - geometry values to test.
	 *              point.length == nrDimensions.
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
	 * @param point - geometry value to test.
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
	 * Calculate errors at each fingering target.
	 * 
	 * @param point - geometry values to test.
	 *              point.length == nrDimensions.
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
	 * 
	 * @param errorVector
	 * @return sum of squared errors
	 */
	public static double calcNorm(double[] errorVector)
	{
		double norm = 0.0;
		for (double error : errorVector)
		{
			norm += error * error;
		}
		return norm;
	}

	/**
	 * Retrieve physical geometry values from the instrument. Specific values depend on
	 * the derived class.
	 * 
	 * @return point representing current physical geometry values.
	 * 		   point.length == nrDimensions.
	 */
	public abstract double[] getGeometryPoint();

	/**
	 * Set physical geometry values for an instrument. Specific values depend on
	 * the derived class.
	 * 
	 * @param point - physical geometry values to set. 
	 *            point.length == nrDimensions.
	 * @throws DimensionMismatchException.
	 */
	public abstract void setGeometryPoint(double[] point);

	/**
	 * Retrieve geometry values from the instrument, ensuring the values lie
	 * within the current bounds. Specific values depend on the derived class.
	 * 
	 * @return point representing current geometry values.
	 * 				 lowerBounds[i] <= point[i] <= upperBounds[i].
	 * 				 point.length == nrDimensions.
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
			return 5 + (int) (5 * Math.log(nrDimensions));
		}

		if (optimizerType.equals(OptimizerType.BOBYQAOptimizer))
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
		double [] sigma = new double[nrDimensions];
		for ( int i = 0; i < nrDimensions; i++ )
		{
			if ( upperBounds[i] <= lowerBounds[i] )
			{
				sigma[i] = 0.0;
			}
			else
			{
				sigma[i] = 0.2 * ( upperBounds[i] - lowerBounds[i] );
			}
		}
		return sigma;
	}
	
	/**
	 * From the bounds and the initial value, determine the maximum feasible value
	 * for the initial trust region radius.
	 */
	public double getInitialTrustRegionRadius()
	{
		double initial[] = getInitialPoint();
		return getInitialTrustRegionRadius( initial );
	}
	
	/**
	 * From the bounds and the initial value, determine the maximum feasible value
	 * for the initial trust region radius.
	 */
	public double getInitialTrustRegionRadius(double[] initial)
	{
		double minRadius = 1.0;
		double minDimensionRadius;

		for (int i = 0; i < nrDimensions; ++ i)
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
		return minRadius;
	}

	/**
	 * From the bounds and the initial value, generate suggested side lengths
	 * in each direction for a simplex.
	 */
	public double[] getSimplexStepSize()
	{
		double [] stepSize = new double[nrDimensions];
		double initial[] = getInitialPoint();

		for (int i = 0; i < nrDimensions; ++ i)
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
		constraints.setLowerBounds(this.lowerBounds);
	}

	public void setUpperBounds(double[] upperBounds)
	{
		if (upperBounds.length != nrDimensions)
		{
			throw new DimensionMismatchException(upperBounds.length,
					nrDimensions);
		}
		this.upperBounds = upperBounds.clone();
		constraints.setUpperBounds(this.upperBounds);
	}
	
	public int getNrDimensions()
	{
		return nrDimensions;
	}

	public double[] getUpperBounds()
	{
		double[] bounds = upperBounds.clone();
		for (int i = 0; i < bounds.length; i++)
		{
			if (bounds[i] <= lowerBounds[i])
			{
				bounds[i] = lowerBounds[i];
			}
		}
		return bounds;
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

	abstract protected void setConstraints();

}
