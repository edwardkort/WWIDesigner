package com.wwidesigner.geometry.calculation;

import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.PositionInterface;

public class HemisphericalBoreHead
{
	private static final int NUM_HEMI_POINTS = 10;

	/**
	 * Adds a set of BorePoints that define the hemispherical head of the bore.
	 * 
	 * @param origin
	 *            The position of the top of the bore.
	 * @param headDiameter
	 *            The diameter at the equator, of the hemisphere.
	 * @param borePoints
	 *            An empty list to hold the new BorePoints.
	 */
	public static void addHemiHead(double origin, double headDiameter,
			List<BorePoint> borePoints)
	{
		BorePoint point = new BorePoint();
		// Make top point
		point.setBorePosition(origin);
		point.setBoreDiameter(0.00001d); // Bore diameter must be non-zero
		borePoints.add(point);

		for (int i = 1; i <= NUM_HEMI_POINTS; i++)
		{
			point = new BorePoint();
			double heightInterval = (double) i / NUM_HEMI_POINTS;
			double boreDiameter = headDiameter * heightInterval;
			point.setBoreDiameter(boreDiameter);
			double position = (headDiameter - Math.sqrt(headDiameter
					* headDiameter - boreDiameter * boreDiameter))
					/ 2.d + origin;
			point.setBorePosition(position);
			borePoints.add(point);
		}
	}

	/**
	 * Determine the BorePoint representing the equator of the hemisphere to be
	 * created. This method makes no assumptions on the regularity of the bore
	 * profile.
	 * 
	 * @param sortedPoints
	 *            The array of BorePoints in the flute before adding the
	 *            hemispherical head.
	 * @return A new BorePoint representing the equator.
	 */
	public static BorePoint getHemiTopPoint(PositionInterface[] sortedPoints)
	{
		BorePoint hemiTopPoint = new BorePoint();
		double topPosition = sortedPoints[0].getBorePosition();
		for (int i = 1; i < sortedPoints.length; i++)
		{
			BorePoint point = (BorePoint) sortedPoints[i];
			double position = point.getBorePosition();
			double diameter = point.getBoreDiameter();
			if ((position - topPosition) >= diameter / 2.d)
			{
				hemiTopPoint.setBorePosition(diameter / 2.d + topPosition);
				hemiTopPoint.setBoreDiameter(diameter);
				break;
			}
		}

		return hemiTopPoint;
	}

}
