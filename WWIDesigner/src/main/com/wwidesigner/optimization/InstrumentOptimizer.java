package com.wwidesigner.optimization;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.PhysicalParameters;

public abstract class InstrumentOptimizer extends BOBYQAOptimizer implements
		InstrumentOptimizerInterface
{
	protected TuningInterface tuning;
	protected PhysicalParameters physicalParams;
	protected double[] lowerBnd;
	protected double[] upperBnd;
	protected OptimizationFunctionInterface optimizationFunction;
	protected Instrument instrument;

	public abstract void setOptimizationFunction();

	public InstrumentOptimizer(int numberOfInterpolationPoints,
			Instrument inst, TuningInterface tuning)
	{
		super(numberOfInterpolationPoints); // the number of interpolation point
											// should be set according
		// to the number of variables in the optimization problem,
		// which depends on the OptimizableInstrument
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

	/**
	 * @return the instrument
	 */
	public Instrument getInstrument()
	{
		return instrument;
	}

	public void optimizeInstrument()
	{
		double[] startPoint = getStateVector();
		setOptimizationFunction();
		optimize(5000, optimizationFunction, GoalType.MINIMIZE, startPoint,
				lowerBnd, upperBnd);
	}

}
