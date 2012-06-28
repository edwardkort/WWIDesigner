package com.wwidesigner.optimization;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;

import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.PhysicalParameters;

public class InstrumentOptimizer2 extends BOBYQAOptimizer
{
	protected OptimizableInstrument2 instrument;
	protected TuningInterface tuning;
	protected PhysicalParameters physicalParams;
	protected double[] lowerBound;
	protected double[] upperBound;

	public InstrumentOptimizer2(OptimizableInstrument2 inst,
			TuningInterface tuning)
	{
		super(20); // the number of interpolation point should be set according
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
	public void setLowerBound(double[] lowerBound)
	{
		this.lowerBound = lowerBound;
	}

	/**
	 * @param upperBound
	 *            the upperBound to set
	 */
	public void setUpperBound(double[] upperBound)
	{
		this.upperBound = upperBound;
	}

	public void optimizeInstrument()
	{
		double[] startPoint = instrument.getStateVector();
		OptimizationFunction2 func = new OptimizationFunction2(instrument,
				tuning, physicalParams);
		optimize(5000, func, GoalType.MINIMIZE, startPoint, getLowerBound(),
				getUpperBound());
	}

}
