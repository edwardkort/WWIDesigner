package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

public class SingleTaperHoleGroupingOptimizer extends HoleGroupSpacingOptimizer
{
	static
	{
		defaultNumberOfInterpolationPoints = 33;
	}

	public SingleTaperHoleGroupingOptimizer(Instrument inst,
			InstrumentCalculator calculator, TuningInterface tuning)
	{
		super(inst, calculator, tuning);
	}

	/**
	 * stateVector has the following values:<br/>
	 * Total bore length<br/>
	 * For each hole group, from the top, the spacing<br/>
	 * Distance from bottom hole to foot of flute<br/>
	 * The hole/bore diameter ratio for each hole<br/>
	 * The headDiameter as the ratio to the foot diameter<br/>
	 * Length of the taper<br/>
	 * Distance from the bottom of the taper to foot of flute.<br/>
	 * 
	 * @see com.wwidesigner.optimization.InstrumentOptimizerInterface#getStateVector
	 *      ()
	 */
	@Override
	public double[] getStateVector()
	{
		double[] originalVector = super.getStateVector();
		double[] newVector = Arrays.copyOf(originalVector,
				originalVector.length + 3);

		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());

		double footDiameter = 0.;
		double headDiameter = 0.;
		for (int i = 1; i < sortedPoints.length; i++)
		{
			BorePoint point = (BorePoint) sortedPoints[i];
			double boreDiameter = point.getBoreDiameter();
			headDiameter += boreDiameter;
			if (i == sortedPoints.length - 1)
			{
				footDiameter = boreDiameter;
			}
		}
		headDiameter /= sortedPoints.length;
		
		double boreLength = newVector[0];
		
		int idx = originalVector.length;
		newVector[idx++] = 1.05; //headDiameter/footDiameter;
		newVector[idx++] = 0.25 * boreLength;
		newVector[idx] = 0.25 * boreLength;
		
		return newVector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.InstrumentOptimizerInterface#updateGeometry
	 * (double[])
	 */
	@Override
	public void updateGeometry(double[] stateVector)
	{
		prepareGeometryUpdate(stateVector);
		
		// Get the fixed bore values from the existing geometry
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());
		double headPosition = sortedPoints[0].getBorePosition();
		double footDiameter = ((BorePoint)sortedPoints[sortedPoints.length - 1]).getBoreDiameter();
		
		// This instrument will have only 4 borePoints
		List<BorePoint> borePoints = new ArrayList<BorePoint>();
		
		int lastIdx = stateVector.length - 1;
		double footPosition = stateVector[0];
		
		// First borePoint is at original position
		BorePoint point1 = new BorePoint();
		point1.setBorePosition(headPosition);
		double headDiameter = stateVector[lastIdx - 2] * footDiameter;
		point1.setBoreDiameter(headDiameter);
		
		// Set bottom point
		BorePoint point4 = new BorePoint();
		point4.setBorePosition(footPosition);
		point4.setBoreDiameter(footDiameter);
		
		// Set bottom of taper
		BorePoint point3 = new BorePoint();
		double bottomOfTaperPosition = footPosition - stateVector[lastIdx];
		point3.setBorePosition(bottomOfTaperPosition);
		point3.setBoreDiameter(footDiameter);
		
		// Set top of taper
		BorePoint point2 = new BorePoint();
		double topOfTaperPosition = bottomOfTaperPosition - stateVector[lastIdx - 1];
		point2.setBorePosition(topOfTaperPosition);
		point2.setBoreDiameter(headDiameter);
		
		borePoints.add(point1);
		borePoints.add(point2);
		borePoints.add(point3);
		borePoints.add(point4);
		instrument.setBorePoint(borePoints);

		instrument.updateComponents();
	}
}
