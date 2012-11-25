package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole diameters.
 * @author Edward Kort, Burton Patkau
 *
 */
public class HoleSizeObjectiveFunction extends BaseObjectiveFunction
{

	public HoleSizeObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = calculator.getInstrument().getHole().size();
		optimizerType = OptimizerType.BOBYQAOptimizer;		// MultivariateOptimizer
	}

	@Override
	public double[] getGeometryPoint()
	{
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		double[] geometry = new double[nrDimensions];

		for (int i = 0; i < nrDimensions; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i] = hole.getDiameter();
		}

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		for (int i = 0; i < nrDimensions; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setDiameter(point[i]);
		}

		calculator.getInstrument().updateComponents();
	}
}
