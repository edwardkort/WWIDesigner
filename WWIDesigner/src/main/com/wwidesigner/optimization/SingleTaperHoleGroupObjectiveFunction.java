package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for hole positions and diameters,
 * and a simple one-section taper:
 * - Distance from top of bore to first group,
 * - For each group, spacing within group and spacing to next group,
 *   ending with spacing from last group to end of bore.
 * - For each hole, ratio of hole diameter to bore diameter.
 * - Taper ratio head diameter / foot diameter.
 * - Fraction of bore that is tapered.
 * - Fraction of untapered length at head end.
 * The foot diameter remains invariant.
 * @author Edward Kort, Burton Patkau
 *
 */
public class SingleTaperHoleGroupObjectiveFunction extends
		HoleGroupObjectiveFunction
{

	public SingleTaperHoleGroupObjectiveFunction(
			InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator, int[][] holeGroups) throws Exception
	{
		super(calculator, tuning, evaluator, holeGroups);
		nrDimensions += 3;
	}

	@Override
	public double[] getGeometryPoint()
	{
		// Base class returns an array double[nrDimensions].
		double[] geometry = super.getGeometryPoint();
		PositionInterface[] sortedPoints
				= Instrument.sortList(calculator.getInstrument().getBorePoint());
		int idx = nrDimensions - 3;
		// Assume there are at least two points, taper starts on either the first or second,
		// and ends on either the last or second last.
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint nextPoint = (BorePoint) sortedPoints[1];
		BorePoint penultimatePoint = (BorePoint) sortedPoints[sortedPoints.length-2]; 
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length-1];
		double boreLength = bottomPoint.getBorePosition() - topPoint.getBorePosition();
		double taperStart;
		double taperEnd;

		geometry[idx++] = topPoint.getBoreDiameter()/bottomPoint.getBoreDiameter();
		if ( topPoint.getBoreDiameter() == bottomPoint.getBoreDiameter() ) {
			// Bore doesn't really taper.
			taperStart = topPoint.getBorePosition();
			taperEnd = bottomPoint.getBorePosition();
		}
		else {
			if ( topPoint.getBoreDiameter() == nextPoint.getBoreDiameter() ) {
				// Taper starts on second point.
				taperStart = nextPoint.getBorePosition();
			}
			else {
				// Taper starts on first point.
				taperStart = topPoint.getBorePosition();
			}
			if ( bottomPoint.getBoreDiameter() == penultimatePoint.getBoreDiameter() ) {
				// Taper ends on second-last point.
				taperEnd = penultimatePoint.getBorePosition();
			}
			else {
				// Taper ends on bottom point.
				taperEnd = bottomPoint.getBorePosition();
			}
		}
		if ( taperEnd - taperStart >= boreLength )
		{
			geometry[idx++] = 1.0;
			geometry[idx++] = 0.0;
		}
		else
		{
			geometry[idx++] = (taperEnd - taperStart)/boreLength;
			geometry[idx++] = (taperStart - topPoint.getBorePosition()) 
					/ (boreLength - (taperEnd - taperStart) );
		}

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		super.setGeometryPoint(point);

		// Replace existing bore points with a new list of up to 4 points.
		List<BorePoint> borePoints = new ArrayList<BorePoint>();
		
		int idx = nrDimensions - 3;
		PositionInterface[] sortedPoints
				= Instrument.sortList(calculator.getInstrument().getBorePoint());
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length-1];
		double footDiameter = bottomPoint.getBoreDiameter();
		double headDiameter = footDiameter * point[idx];
		double boreLength = bottomPoint.getBorePosition() - topPoint.getBorePosition();
		double taperLength = boreLength * point[idx+1];
		double taperStart = (boreLength - taperLength) * point[idx+2];

		BorePoint newPoint = new BorePoint();
		newPoint.setBoreDiameter(headDiameter);
		newPoint.setBorePosition(topPoint.getBorePosition());
		borePoints.add(newPoint);
		if ( taperStart > 0 )
		{
			// Taper begins on second point rather than first.
			newPoint = new BorePoint();
			newPoint.setBoreDiameter(headDiameter);
			newPoint.setBorePosition(topPoint.getBorePosition() + taperStart);
			borePoints.add(newPoint);
		}
		// Add point for end of taper.
		newPoint = new BorePoint();
		newPoint.setBoreDiameter(footDiameter);
		newPoint.setBorePosition(topPoint.getBorePosition() + taperStart + taperLength);
		borePoints.add(newPoint);
		if ( taperStart + taperLength < boreLength )
		{
			// Taper ends on second last point rather than last.
			newPoint = new BorePoint();
			newPoint.setBoreDiameter(footDiameter);
			newPoint.setBorePosition(topPoint.getBorePosition() + boreLength);
			borePoints.add(newPoint);
		}
		calculator.getInstrument().setBorePoint(borePoints);
		calculator.getInstrument().updateComponents();
	}

}
