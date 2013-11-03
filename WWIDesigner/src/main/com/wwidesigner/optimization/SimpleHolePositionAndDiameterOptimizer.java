package com.wwidesigner.optimization;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.util.SortedPositionList;

@Deprecated
public class SimpleHolePositionAndDiameterOptimizer extends InstrumentOptimizer
{
	private static int defaultNumberOfInterpolationPoints = 60;

	public SimpleHolePositionAndDiameterOptimizer(Instrument inst,
			InstrumentCalculator calculator, TuningInterface tuning)
	{
		super(defaultNumberOfInterpolationPoints, inst, calculator, tuning);
	}

	public double[] getStateVector()
	{
		SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(
				instrument.getBorePoint());
		SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
				instrument.getHole());

		int len = 1 + 2 * sortedHoles.size();

		double[] state_vector = new double[len];

		// First values is total bore length
		BorePoint lastPoint = sortedPoints.getLast();
		state_vector[0] = lastPoint.getBorePosition();

		// Measure from bore head
		int i = 1;
		// Next values are hole positions, from the TOP
		for (Hole currentHole : sortedHoles)
		{
			state_vector[i++] = currentHole.getBorePosition();
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

		// Measure from bore head
		int i = 1;
		// Next values are hole positions, from the TOP
		for (Hole currentHole : sortedHoles)
		{
			currentHole.setBorePosition(state_vector[i++]);
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
		optimizationFunction = new BasicImpedanceOptimizationFunction(this, tuning);

	}

}
