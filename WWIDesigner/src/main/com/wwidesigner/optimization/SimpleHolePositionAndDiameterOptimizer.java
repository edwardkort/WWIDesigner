package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.SortedPositionList;

public class SimpleHolePositionAndDiameterOptimizer extends InstrumentOptimizer
{
	private static int defaultNumberOfInterpolationPoints = 60;

	public SimpleHolePositionAndDiameterOptimizer(Instrument inst,
			TuningInterface tuning)
	{
		super(defaultNumberOfInterpolationPoints, inst, tuning);
	}

	public double[] getStateVector()
	{
		instrument.updateComponents();

		SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(
				instrument.getBorePoint());
		SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
				instrument.getHole());

		int len = 1 + 2 * sortedHoles.size();

		double[] state_vector = new double[len];

		// First values is total bore length
		BorePoint lastPoint = sortedPoints.getLast();
		state_vector[0] = lastPoint.getBorePosition();

		// Measure from mouthpiece
		double topPosition = instrument.getMouthpiece().getBorePosition();
		int i = 1;
		// Next values are hole positions, from the TOP
		for (Hole currentHole : sortedHoles)
		{
			state_vector[i++] = currentHole.getBorePosition() - topPosition;
		}

		// Next values are hole diameters, from the TOP
		for (Hole currentHole : sortedHoles)
		{
			state_vector[i++] = currentHole.getDiameter();
		}

		return state_vector;
	}

	public void updateGeometry(double[] state_vector)
	{
		SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(
				instrument.getBorePoint());
		SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
				instrument.getHole());

		BorePoint lastPoint = sortedPoints.getLast();
		lastPoint.setBorePosition(state_vector[0]);

		// Measure from mouthpiece
		double topPosition = instrument.getMouthpiece().getBorePosition();
		int i = 1;
		// Next values are hole positions, from the TOP
		for (Hole currentHole : sortedHoles)
		{
			currentHole.setBorePosition(state_vector[i++] + topPosition);
		}

		// Next values are hole diameters, from the TOP
		for (Hole currentHole : sortedHoles)
		{
			currentHole.setDiameter(state_vector[i++]);
		}

		instrument.updateComponents();

	}

	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicOptimizationFunction(this, tuning,
				physicalParams);

	}

}
