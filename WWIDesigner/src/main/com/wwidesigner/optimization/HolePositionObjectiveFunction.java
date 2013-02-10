package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore length and hole positions:
 * - Position of end bore point.
 * - For each hole, spacing below the hole to the next hole,
 *   or (for the last hole) to end of bore.
 * Assumes that total spacing is less than the bore length.
 * (In practice, it will be significantly less.)
 * @author Edward Kort, Burton Patkau
 *
 */
public class HolePositionObjectiveFunction extends BaseObjectiveFunction
{

	public HolePositionObjectiveFunction(InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 1 + calculator.getInstrument().getHole().size();
		optimizerType = OptimizerType.BOBYQAOptimizer;		// MultivariateOptimizer
		if ( nrDimensions == 1 ) {
			// BOBYQA doesn't support single dimension.
			optimizerType = OptimizerType.CMAESOptimizer;
		}
		
		// Length cannot be shorter than position of lowest hole.
		// (Use 1 mm past the lower edge of the lowest hole.)

		PositionInterface[] holeList = Instrument.sortList(calculator.getInstrument().getHole());
		if (holeList.length > 0)
		{
			Hole endHole = (Hole) holeList[holeList.length-1];
			lowerBounds = new double[nrDimensions];
			lowerBounds[0] = endHole.getBorePosition() + endHole.getDiameter()/2.0 + 0.001;
		}
	}
	
	/**
	 * @return The position of the farthest bore point.
	 */
	protected double getEndOfBore()
	{
		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		double endPosition = boreList.get(0).getBorePosition();

		for (BorePoint borePoint: boreList)
		{
			if ( borePoint.getBorePosition() > endPosition )
			{
				endPosition = borePoint.getBorePosition();
			}
		}
		return endPosition;
	}

	@Override
	public double[] getGeometryPoint()
	{
		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		double[] geometry = new double[nrDimensions];

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Measure hole positions from bottom to top, starting with
		// the position of the farthest bore point.

		geometry[0] = getEndOfBore();
		double priorHolePosition = geometry[0];

		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			geometry[i + 1] = priorHolePosition - hole.getBorePosition();
			priorHolePosition = hole.getBorePosition();
		}
		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		if ( point.length != nrDimensions ) {
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		// Ensure that no bore points are beyond the new bottom position.
		// Find the farthest one out, and update its position.

		List<BorePoint> boreList = calculator.getInstrument().getBorePoint();
		BorePoint endPoint = boreList.get(0);
		
		for (BorePoint borePoint: boreList)
		{
			if ( borePoint.getBorePosition() > endPoint.getBorePosition() )
			{
				endPoint = borePoint;
			}
			if ( borePoint.getBorePosition() > point[0] )
			{
				borePoint.setBorePosition(point[0]);
			}
		}
		endPoint.setBorePosition(point[0]);

		PositionInterface[] sortedHoles
				= Instrument.sortList(calculator.getInstrument().getHole());

		// Geometry dimensions are distances between holes.
		// Final dimension is distance between last hole and end of bore.
		// Position the holes from bottom to top.
		double priorHolePosition = point[0];

		for (int i = sortedHoles.length - 1; i >= 0; --i)
		{
			Hole hole = (Hole) sortedHoles[i];
			hole.setBorePosition( priorHolePosition - point[i + 1] );
			priorHolePosition = hole.getBorePosition();
		}

		calculator.getInstrument().updateComponents();
	}

}
