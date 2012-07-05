package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;

public class OptimizableInstrument2
{
	protected Instrument baseInstrument;

	public OptimizableInstrument2(Instrument instrument)
	{
		baseInstrument = instrument;
	}

	/**
	 * @return the baseInstrument
	 */
	public Instrument getBaseInstrument()
	{
		return baseInstrument;
	}

	public double[] getStateVector()
	{
		BorePoint[] sortedPoints = Instrument.sortList(baseInstrument
				.getBorePoint());
		com.wwidesigner.geometry.Hole[] sortedHoles = Instrument
				.sortList(baseInstrument.getHole());

		int len = 1 + sortedHoles.length;

		double[] state_vector = new double[len];
		BorePoint lastPoint = sortedPoints[sortedPoints.length - 1];
		state_vector[0] = lastPoint.getBoreDiameter();

		double accumulatedDistance = 0.;
		for (int i = sortedHoles.length; i > 0; --i)
		{
			com.wwidesigner.geometry.Hole hole = sortedHoles[i - 1];
			state_vector[i] = state_vector[0] - hole.getBorePosition()
					- accumulatedDistance;
			accumulatedDistance += state_vector[i];
		}
		return state_vector;
	}

	public void updateGeometry(double[] state_vector)
	{
		BorePoint[] sortedPoints = Instrument.sortList(baseInstrument
				.getBorePoint());
		com.wwidesigner.geometry.Hole[] sortedHoles = Instrument
				.sortList(baseInstrument.getHole());

		BorePoint lastPoint = sortedPoints[sortedPoints.length - 1];
		lastPoint.setBorePosition(state_vector[0]);

		double accumulatedDistance = 0.;
		for (int i = sortedHoles.length; i > 0; --i)
		{
			com.wwidesigner.geometry.Hole hole = sortedHoles[i - 1];
			hole.setBorePosition(state_vector[0] - state_vector[i]
					- accumulatedDistance);
			accumulatedDistance += state_vector[i];
		}
	}
}