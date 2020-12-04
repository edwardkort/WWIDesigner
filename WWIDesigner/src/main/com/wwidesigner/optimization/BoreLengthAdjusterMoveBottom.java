/**
 * 
 */
package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 *
 */
public class BoreLengthAdjusterMoveBottom
		implements BoreLengthAdjustmentInterface
{
	private BaseObjectiveFunction mParent;

	public BoreLengthAdjusterMoveBottom(BaseObjectiveFunction parent)
	{
		mParent = parent;
	}

	@Override
	public void setBore(double[] point)
	{
		int nrDimensions = mParent.nrDimensions;
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		InstrumentCalculator calculator = mParent.calculator;
		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				calculator.getInstrument().getBorePoint());
		BorePoint endPoint = boreList.getLast();

		// Don't let optimizer delete a borePoint.
		// Instead, move them up the bore a bit.
		double newEndPosition = point[0];
		int lastPointIndex = boreList.size() - 1;
		for (int i = lastPointIndex - 1; i >= 0; i--)
		{
			BorePoint borePoint = boreList.get(i);
			double currentPosition = borePoint.getBorePosition();
			if (currentPosition >= newEndPosition)
			{
				// Squeeze bore points together if necessary.
				// This has undesirable consequences: it will not be undone
				// in
				// subsequent optimization geometries even if it is no
				// longer required.
				newEndPosition -= MINIMUM_BORE_POINT_SPACING;
				borePoint.setBorePosition(newEndPosition);
			}
			else
			{
				break;
			}
		}

		endPoint.setBorePosition(point[0]);
	}

}
