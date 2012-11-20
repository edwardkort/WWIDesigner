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
 * Base class for optimization objective functions.
 * Each derived class supports optimization of specific aspects
 * of an instrument geometry.
 * @author Burton Patkau
 */
public abstract class BaseObjectiveFunction implements MultivariateFunction, UnivariateFunction
{
	// Information about what is to be optimized.
	protected InstrumentCalculator calculator;
	protected List<Fingering> fingeringTargets;
	protected EvaluatorInterface  evaluator;
	
	// Description of the geometry that this particular objective function supports.
	protected int nrDimensions;			// Number of geometry values.
										// Constant for each derived class.
	protected double[] lowerBounds;		// Lower bound for each geometry value.
	protected double[] upperBounds;		// Upper bound for each geometry value.
	
	// Recommended optimization method.
	public enum OptimizerType
	{
		BrentOptimizer, PowellOptimizer, BOBYQAOptimizer, CMAESOptimizer
	}
	
	protected  OptimizerType  optimizerType;
	protected  int  maxIterations;
	protected  AbstractRangeProcessor  rangeProcessor;
	protected static final int MaxInterpolations = 40;	// Maximum number of interpolations for BOBYQA.

	// Statistics for the results of an optimization.
	protected int evaluationsDone;		// Number of error calculations.
	protected int iterationsDone;		// Number of calculations of error norm.

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
		maxIterations = 1000;
		rangeProcessor = null;
		iterationsDone = 0;
		evaluationsDone = 0;
	}

	/*
	 * The multivariate objective function to be optimized,
	 * a sum of squares of the error value specific to the derived class.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.math3.analysis.MultivariateFunction#value(double* [])
	 */
	@Override
	public double value(double[] point)
	{
		double[] errorVector = getErrorVector(point);
		++ iterationsDone;
		evaluationsDone += errorVector.length;
		return calcNorm(errorVector);
	}

	/*
	 * The univariate objective function to be optimized,
	 * a sum of squares of the error value specific to the derived class.
	 * Requires nrDimensions == 1
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.math3.analysis.UnivariateFunction#value(double)
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
	 * @param point
	 * @return array of error values, one for each fingering target.
	 * @throws DimensionMismatchException.
	 */
	public double[] getErrorVector(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		setGeometryPoint( point );
		double[] errorVector = evaluator.calculateErrorVector(fingeringTargets);
		return errorVector;
	}
	
	/**
	 * Calculate an error norm from an error vector, as the sum of squares.
	 * @param errorVector
	 * @return sum of squared errors
	 */
	public double calcNorm(double[] errorVector)
	{
		double norm = 0.0;
		for (double error: errorVector)
		{
			norm += error*error;
		}
		return norm;
	}

	/**
	 * Retrieve geometry values from the instrument.
	 * Specific values depend on the derived class.
	 * @return point representing current geometry values.  point.length == nrDimensions.
	 */
	public abstract double[] getGeometryPoint();
	
	/**
	 * Set geometry values for an instrument.
	 * Specific values depend on the derived class.
	 * @param point - geometry to set.  point.length == nrDimensions.
	 * @throws DimensionMismatchException.
	 */
	public abstract void setGeometryPoint( double[] point );

	/**
	 * From the number of dimensions, propose a useful number of interpolations
	 * if the optimizer type requires it.
	 */
	public int getNrInterpolations()
	{
		int nrInterpolations = 0; // The default value for CMAES

		if (OptimizerType.BOBYQAOptimizer.equals(optimizerType))
		{
			if (isMultiStart())
			{
				nrInterpolations = 2 * nrDimensions;
			}
			else
			{
				nrInterpolations = (nrDimensions + 1)
						* (nrDimensions + 2) / 2;
			}
			if ( nrInterpolations > MaxInterpolations )
			{
				nrInterpolations = MaxInterpolations;
			}
		}
		return nrInterpolations;
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
		if ( lowerBounds.length != nrDimensions ) {
			throw new DimensionMismatchException(lowerBounds.length, nrDimensions);
		}
		this.lowerBounds = lowerBounds;
	}

	public double[] getUpperBounds()
	{
		return upperBounds;
	}

	public void setUpperBounds(double[] upperBounds)
	{
		if ( upperBounds.length != nrDimensions ) {
			throw new DimensionMismatchException(upperBounds.length, nrDimensions);
		}
		this.upperBounds = upperBounds;
	}

	public int getNrDimensions()
	{
		return nrDimensions;
	}

	public OptimizerType getOptimizerType()
	{
		return optimizerType;
	}

	public void setOptimizerType(OptimizerType optimizerType)
	{
		this.optimizerType = optimizerType;
	}

	public int getMaxIterations()
	{
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations)
	{
		this.maxIterations = maxIterations;
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

	public int getIterationsDone()
	{
		return iterationsDone;
	}

	public int getEvaluationsDone()
	{
		return evaluationsDone;
	}

}
