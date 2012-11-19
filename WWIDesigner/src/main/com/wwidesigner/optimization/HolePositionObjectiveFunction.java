package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole positions:
 * - Distance from top of bore to first group,
 * - For each group, spacing within group and spacing to next group,
 *   ending with spacing from last group to end of bore.
 * @author Edward Kort, Burton Patkau
 *
 */
public class HolePositionObjectiveFunction extends BaseObjectiveFunction
{

	public HolePositionObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = calculator.getInstrument().getHole().size() + 1;
		optimizerType = OptimizerType.BOBYQAOptimizer;		// MultivariateOptimizer
		nrInterpolations = 2 * nrDimensions;
		maxIterations = 2000;
	}

	@Override
	public double[] getGeometryPoint()
	{
		PositionInterface[] sortedPoints
				= Instrument.sortList(calculator.getInstrument().getBorePoint());
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		double[] geometry = new double[nrDimensions];
		// First dimension is first hole position relative to bore origin.
		double priorHolePosition = 0; 

		for (int i = 0; i < nrDimensions - 1; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i] = hole.getBorePosition() - priorHolePosition;
			priorHolePosition = hole.getBorePosition();
		}

		// Final dimension is the position of the end of the bore
		// relative to the last hole.
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double endOfBore = lastPoint.getBorePosition();
		geometry[nrDimensions-1] = endOfBore - priorHolePosition;

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		PositionInterface[] sortedPoints
				= Instrument.sortList(calculator.getInstrument().getBorePoint());
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		// First dimension is first hole position relative to bore origin.
		double priorHolePosition = 0; 

		for (int i = 0; i < nrDimensions - 1; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition( priorHolePosition + point[i] );
			priorHolePosition = hole.getBorePosition();
		}

		// Final dimension is the position of the end of the bore
		// relative to the last hole.
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		lastPoint.setBorePosition( priorHolePosition + point[nrDimensions-1]);

		calculator.getInstrument().updateComponents();
	}

}
