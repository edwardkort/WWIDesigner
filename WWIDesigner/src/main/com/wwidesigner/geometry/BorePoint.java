/**
 * 
 */
package com.wwidesigner.geometry;

import java.util.List;

import com.wwidesigner.util.InvalidFieldHandler;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 * 
 */
public class BorePoint implements BorePointInterface
{
	protected double borePosition;
	protected double boreDiameter;

	public BorePoint()
	{
	}

	public BorePoint(double position, double diameter)
	{
		this.borePosition = position;
		this.boreDiameter = diameter;
	}

	/**
	 * @return the borePosition
	 */
	public double getBorePosition()
	{
		return borePosition;
	}

	/**
	 * @param aBorePosition
	 *            the borePosition to set
	 */
	public void setBorePosition(double aBorePosition)
	{
		this.borePosition = aBorePosition;
	}

	/**
	 * @return the boreDiameter
	 */
	public double getBoreDiameter()
	{
		return boreDiameter;
	}

	/**
	 * @param aBoreDiameter
	 *            the boreDiameter to set
	 */
	public void setBoreDiameter(double aBoreDiameter)
	{
		this.boreDiameter = aBoreDiameter;
	}

	public void convertDimensions(double multiplier)
	{
		borePosition *= multiplier;
		boreDiameter *= multiplier;
	}

	public void checkValidity(InvalidFieldHandler handler)
	{
		if (Double.isNaN(borePosition))
		{
			handler.logError("Bore point position must be specified.");
		}
		if (Double.isNaN(boreDiameter))
		{
			handler.logError("Bore point diameter must be specified.");
		}
		else if (boreDiameter <= 0.0)
		{
			handler.logError("Bore point must have a positive diameter.");
		}
	}

	public static double getInterpolatedExtrapolatedBoreDiameter(
			List<BorePoint> borePoints, double position)
	{
		SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(
				borePoints);
		BorePoint beforePoint = null;
		BorePoint afterPoint = null;

		for (BorePoint currentPoint : sortedPoints)
		{
			double currentPosition = currentPoint.getBorePosition();
			if (currentPosition < position)
			{
				beforePoint = currentPoint;
			}
			else if (currentPosition > position)
			{
				afterPoint = currentPoint;
				break;
			}
			else
			{
				return currentPoint.getBoreDiameter();
			}
		}

		if (beforePoint == null)
		{
			return getExtrapolatedDiameter(sortedPoints.get(0),
					sortedPoints.get(1), position, true);
		}
		else if (afterPoint == null)
		{
			return getExtrapolatedDiameter(
					sortedPoints.get(sortedPoints.size() - 2),
					sortedPoints.getLast(), position, false);
		}

		double positionFraction = (afterPoint.getBorePosition() - position)
				/ (afterPoint.getBorePosition() - beforePoint.getBorePosition());

		return afterPoint.getBoreDiameter() * (1. - positionFraction)
				+ beforePoint.getBoreDiameter() * positionFraction;
	}

	private static double getExtrapolatedDiameter(BorePoint firstPoint,
			BorePoint secondPoint, double position, boolean offTop)
	{
		double firstPosition = firstPoint.getBorePosition();
		double firstDiameter = firstPoint.getBoreDiameter();
		double secondPosition = secondPoint.getBorePosition();
		double secondDiameter = secondPoint.getBoreDiameter();

		if (offTop)
		{
			double positionRatio = (secondPosition - position)
					/ (secondPosition - firstPosition);
			return secondDiameter - (secondDiameter - firstDiameter)
					* positionRatio;
		}
		else
		{
			double positionRatio = (position - firstPosition)
					/ (secondPosition - firstPosition);
			return firstDiameter - (firstDiameter - secondDiameter)
					* positionRatio;
		}
	}
}
