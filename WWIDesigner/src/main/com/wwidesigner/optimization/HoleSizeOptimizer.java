package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

public class HoleSizeOptimizer extends InstrumentOptimizer
{
	protected static int defaultNumberOfInterpolationPoints = 12;

	public HoleSizeOptimizer(Instrument inst, InstrumentCalculator calculator,
			TuningInterface tuning)
	{
		super(defaultNumberOfInterpolationPoints, inst, calculator, tuning);
	}

	/**
	 * StateVector has an element for each hole size ratio (hole/bore)
	 */
	public double[] getStateVector()
	{
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		int len = sortedHoles.length;
		double[] state_vector = new double[len];

		for (int i = 0; i < len; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			state_vector[i] = hole.getRatio();
		}

		return state_vector;
	}

	public void updateGeometry(double[] state_vector)
	{
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());

		for (int i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setRatio(state_vector[i]);
		}

		instrument.updateComponents();
	}

	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicImpedanceOptimizationFunction(this,
				tuning);

	}

}
