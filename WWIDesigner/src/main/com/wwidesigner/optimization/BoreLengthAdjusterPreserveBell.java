/**
 * 
 */
package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 *
 */
public class BoreLengthAdjusterPreserveBell
		implements BoreLengthAdjustmentInterface
{
	private BaseObjectiveFunction mParent;
	protected int mBellIndex;
	protected InstrumentCalculator mCalculator;

	public BoreLengthAdjusterPreserveBell(BaseObjectiveFunction parent)
	{
		mParent = parent;
		mCalculator = mParent.calculator;
		mBellIndex = findBell(mCalculator.getInstrument());
	}

	/**
	 * Find the beginning of the instrument bell, for use with PRESERVE_BELL.
	 * Chooses the bore point that follows the longest bore segment.
	 * 
	 * @param boreList
	 * @return index of bore point at start of bell.
	 */
	protected static int findBell(Instrument instrument)
	{
		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				instrument.getBorePoint());
		double longestSegment = 0;
		double lastPosition = boreList.get(0).getBorePosition();
		int bellIndex = boreList.size() - 1;
		for (int idx = 1; idx < boreList.size(); ++idx)
		{
			if (boreList.get(idx).getBorePosition()
					- lastPosition >= longestSegment)
			{
				bellIndex = idx;
				longestSegment = boreList.get(idx).getBorePosition();
			}
			lastPosition = boreList.get(idx).getBorePosition();
		}
		return bellIndex;
	}

	@Override
	public void setBore(double[] point)
	{
		int nrDimensions = mParent.nrDimensions;
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}

		SortedPositionList<BorePoint> boreList = new SortedPositionList<BorePoint>(
				mCalculator.getInstrument().getBorePoint());
		BorePoint endPoint = boreList.getLast();

		double netChange = point[0] - endPoint.getBorePosition();
		double priorBorePoint = boreList.get(mBellIndex - 1).getBorePosition();
		for (int i = mBellIndex; i < boreList.size(); ++i)
		{
			BorePoint borePoint = boreList.get(i);
			double oldPosition = borePoint.getBorePosition();
			if (oldPosition + netChange <= priorBorePoint
					+ MINIMUM_BORE_POINT_SPACING)
			{
				// Squeeze bore points together if necessary.
				// This has undesirable consequences: it will not be undone
				// in
				// subsequent optimization geometries even if it is no
				// longer required.
				borePoint.setBorePosition(
						priorBorePoint + MINIMUM_BORE_POINT_SPACING);
			}
			else
			{
				borePoint.setBorePosition(oldPosition + netChange);
			}
			priorBorePoint = borePoint.getBorePosition();
		}

	}

}
