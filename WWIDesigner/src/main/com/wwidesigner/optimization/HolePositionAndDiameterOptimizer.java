package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.note.TuningInterface;

public class HolePositionAndDiameterOptimizer extends InstrumentOptimizer
{
	static final int numberOfInterpolationPoints = 20;

	public HolePositionAndDiameterOptimizer(Instrument inst, TuningInterface tuning)
	{
		super(numberOfInterpolationPoints, inst, tuning);
	}

	public double[] getStateVector()
	{
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		int len = 1 + 2*sortedHoles.length;

		double[] state_vector = new double[len];

		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		state_vector[0] = lastPoint.getBorePosition();

		double accumulatedDistance = 0.;
		for (int i = sortedHoles.length; i > 0; --i)
		{
			com.wwidesigner.geometry.Hole hole = 
					(com.wwidesigner.geometry.Hole) sortedHoles[i - 1];
			state_vector[i] = state_vector[0] - hole.getBorePosition()
					- accumulatedDistance;
			accumulatedDistance += state_vector[i];
		}
		
		for (int i = 0; i < sortedHoles.length; ++i) 
		{
			com.wwidesigner.geometry.Hole hole = 
					(com.wwidesigner.geometry.Hole) sortedHoles[i];
		    state_vector[1+sortedHoles.length+i] = hole.getRatio();
		}
		
		return state_vector;
	}

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
		
		for (int i = 0; i < sortedHoles.length; ++i) 
		{
			com.wwidesigner.geometry.Hole hole = 
					(com.wwidesigner.geometry.Hole) sortedHoles[i];
			
			hole.setRatio(state_vector[1+sortedHoles.length+i]);
			
//		    state_vector[1+sortedHoles.length+i] = hole.getRatio();
		}

	}

	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicOptimizationFunction(this, tuning,
				physicalParams);
		
	}

}