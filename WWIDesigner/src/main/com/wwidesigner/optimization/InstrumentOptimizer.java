package com.wwidesigner.optimization;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;
import org.apache.commons.math3.optimization.direct.BaseAbstractMultivariateSimpleBoundsOptimizer;
import org.apache.commons.math3.optimization.direct.CMAESOptimizer;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.PhysicalParameters;

public abstract class InstrumentOptimizer implements
		InstrumentOptimizerInterface
{
	protected TuningInterface tuning;
	protected PhysicalParameters physicalParams;
	protected double[] lowerBnd;
	protected double[] upperBnd;
	protected OptimizationFunctionInterface optimizationFunction;
	protected Instrument instrument;
	@SuppressWarnings("rawtypes")
	protected BaseAbstractMultivariateSimpleBoundsOptimizer baseOptimizer;
	protected OptimizerType baseOptimizerType;
	protected int numberOfInterpolationPoints;

	public abstract void setOptimizationFunction();

	public InstrumentOptimizer(int numberOfInterpolationPoints,
			Instrument inst, TuningInterface tuning)
	{
		// Default to a BOBYQAOptimizer
		// The number of interpolation point
		// should be set according
		// to the number of variables in the optimization problem,
		// which depends on the OptimizableInstrument
		setBaseOptimizer(OptimizerType.BOBYQAOptimizer, numberOfInterpolationPoints);

		this.instrument = inst;
		this.tuning = tuning;
	}

	/**
	 * @return the physicalParams
	 */
	public PhysicalParameters getPhysicalParams()
	{
		return physicalParams;
	}

	/**
	 * @param physicalParams
	 *            the physicalParams to set
	 */
	public void setPhysicalParams(PhysicalParameters physicalParams)
	{
		this.physicalParams = physicalParams;
	}

	/**
	 * @param lowerBound
	 *            the lowerBound to set
	 */
	public void setLowerBnd(double[] lowerBound)
	{
		this.lowerBnd = lowerBound;
	}

	/**
	 * @param upperBound
	 *            the upperBound to set
	 */
	public void setUpperBnd(double[] upperBound)
	{
		this.upperBnd = upperBound;
	}

	public void setBaseOptimizer(OptimizerType baseOptimizerType, int numberOfInterpolationPoints)
	{
		this.numberOfInterpolationPoints = numberOfInterpolationPoints;
		
		switch (baseOptimizerType)
		{
			case BOBYQAOptimizer:
				baseOptimizer = new BOBYQAOptimizer(numberOfInterpolationPoints);
				baseOptimizerType = OptimizerType.BOBYQAOptimizer;
				break;
			case CMAESOptimizer:
				baseOptimizer = new CMAESOptimizer(numberOfInterpolationPoints);
				baseOptimizerType = OptimizerType.CMAESOptimizer;
				break;
		}
	}
	
	/**
	 * @return the instrument
	 */
	public Instrument getInstrument()
	{
		return instrument;
	}

	@SuppressWarnings("unchecked")
	public void optimizeInstrument()
	{
		double[] startPoint = getStateVector();
		setOptimizationFunction();
		baseOptimizer.optimize(25000, optimizationFunction, GoalType.MINIMIZE,
				startPoint, lowerBnd, upperBnd);
	}

	public enum OptimizerType
	{
		BOBYQAOptimizer, CMAESOptimizer

	}

}
