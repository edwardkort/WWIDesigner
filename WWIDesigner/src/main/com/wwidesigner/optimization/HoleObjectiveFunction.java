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
 * Optimization objective function for hole positions and diameters:
 * - Distance from top of bore to first hole,
 * - For each hole, spacing to next group,
 *   ending with spacing from last hole to end of bore.
 * - For each hole, ratio of hole diameter to bore diameter.
 * @author Edward Kort, Burton Patkau
 *
 */
public class HoleObjectiveFunction extends BaseObjectiveFunction
{

	public HoleObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 2 * calculator.getInstrument().getHole().size() + 1;
		optimizerType = OptimizerType.BOBYQAOptimizer;		// MultivariateOptimizer
		nrInterpolations = 2 * nrDimensions;
		maxIterations = 5000;
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

		int i = 0;	// Hole number
		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i] = hole.getBorePosition() - priorHolePosition;
			priorHolePosition = hole.getBorePosition();
		}
		// Final length is the position of the end of the bore
		// relative to the last hole.
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double endOfBore = lastPoint.getBorePosition();
		geometry[sortedHoles.length] = endOfBore - priorHolePosition;

		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i+sortedHoles.length+1] = hole.getRatio();
		}

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

		int i = 0;		// Hole number.
		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition( priorHolePosition + point[i] );
			priorHolePosition = hole.getBorePosition();
		}

		// Final length is the position of the end of the bore
		// relative to the last hole.
		BorePoint lastPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		lastPoint.setBorePosition( priorHolePosition + point[sortedHoles.length]);

		for (i = 0; i < sortedHoles.length; ++i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setRatio( point[i+sortedHoles.length+1] );
		}

		calculator.getInstrument().updateComponents();
	}

}
