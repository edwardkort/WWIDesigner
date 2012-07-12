/**
 * 
 */
package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.note.TuningInterface;

/**
 * @author kort
 * 
 */
public class HolePositionOptimizer extends InstrumentOptimizer
{

	static final int numberOfInterpolationPoints = 20;

	/**
	 * @param inst
	 * @param tuning
	 */
	public HolePositionOptimizer(Instrument inst, TuningInterface tuning)
	{
		super(numberOfInterpolationPoints, inst, tuning);
	}

	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicOptimizationFunction(this, tuning,
				physicalParams);

	}

	@Override
	public double[] getStateVector()
	{
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		int len = 1 + sortedHoles.length;

		double[] state_vector = new double[len];
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		state_vector[0] = lastPoint.getBorePosition();

		double accumulatedDistance = 0.;
		for (int i = sortedHoles.length; i > 0; --i)
		{
			com.wwidesigner.geometry.Hole hole = (com.wwidesigner.geometry.Hole) sortedHoles[i - 1];
			state_vector[i] = state_vector[0] - hole.getBorePosition()
					- accumulatedDistance;
			accumulatedDistance += state_vector[i];
		}
		return state_vector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.InstrumentOptimizerInterface#updateGeometry
	 * (double[])
	 */
	@Override
	public void updateGeometry(double[] state_vector)
	{
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		lastPoint.setBorePosition(state_vector[0]);

		double accumulatedDistance = 0.;
		for (int i = sortedHoles.length; i > 0; --i)
		{
			com.wwidesigner.geometry.Hole hole = (com.wwidesigner.geometry.Hole) sortedHoles[i - 1];
			hole.setBorePosition(state_vector[0] - state_vector[i]
					- accumulatedDistance);
			accumulatedDistance += state_vector[i];
		}
	}
}
