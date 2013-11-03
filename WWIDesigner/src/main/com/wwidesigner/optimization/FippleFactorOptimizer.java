/**
 * 
 */
package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * @author kort
 *
 */
@Deprecated
public class FippleFactorOptimizer extends InstrumentOptimizer
{
	protected static int defaultNumberOfInterpolationPoints = 2;

	/**
	 * @param numberOfInterpolationPoints
	 * @param inst
	 * @param calculator
	 * @param tuning
	 */
	public FippleFactorOptimizer(Instrument inst, InstrumentCalculator calculator,
			TuningInterface tuning)
	{
		super(defaultNumberOfInterpolationPoints, inst, calculator, tuning);
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.optimization.InstrumentOptimizerInterface#getStateVector()
	 */
	@Override
	public double[] getStateVector()
	{
		double[] stateVector = new double[1];
		
		double fippleFactor = instrument.getMouthpiece().getFipple().getFippleFactor();
		stateVector[0] = fippleFactor;
		
		return stateVector;
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.optimization.InstrumentOptimizerInterface#updateGeometry(double[])
	 */
	@Override
	public void updateGeometry(double[] stateVector)
	{
		double fippleFactor = stateVector[0];
		instrument.getMouthpiece().getFipple().setFippleFactor(fippleFactor);
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.optimization.InstrumentOptimizer#setOptimizationFunction()
	 */
	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicImpedanceOptimizationFunction(this,
				tuning);
	}

}
