/**
 * 
 */
package com.wwidesigner.optimization;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.util.BoreProfileOptimizationException;
import com.wwidesigner.util.DoubleFormatter;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author Edward Kort
 *
 */
public class BoreLengthAdjusterPreserveBore
		implements BoreLengthAdjustmentInterface
{
	private BaseObjectiveFunction mParent;

	public BoreLengthAdjusterPreserveBore(BaseObjectiveFunction parent)
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
		double newEndPosition = point[0];
		int lastPointIndex = boreList.size() - 1;
		ArrayList<BorePoint> errorPoints = new ArrayList<BorePoint>();
		for (int i = lastPointIndex - 1; i >= 0; i--)
		{
			BorePoint borePoint = boreList.get(i);
			double currentPosition = borePoint.getBorePosition();
			if (currentPosition >= newEndPosition)
			{
				errorPoints.add(borePoint);
			}
			else
			{
				break;
			}
		}
		if (errorPoints.size() > 0)
		{
			throw new BoreProfileOptimizationException(
					formatErrors(errorPoints));
		}

		// Extrapolate/interpolate the bore diameter of end point
		double endDiameter = BorePoint
				.getInterpolatedExtrapolatedBoreDiameter(boreList, point[0]);
		endPoint.setBoreDiameter(endDiameter);
		endPoint.setBorePosition(point[0]);
	}

	protected String formatErrors(List<BorePoint> errorPoints)
	{
		InstrumentCalculator calculator = mParent.calculator;
		calculator.getInstrument().convertToLengthType();

		DoubleFormatter formatter = new DoubleFormatter(false);
		formatter.setDecimalPrecision(3);
		String message = "Illegal attempt to move bore point at position";
		if (errorPoints.size() > 1)
		{
			message += "s";
		}
		BorePoint[] errors = errorPoints.toArray(new BorePoint[0]);
		int lastIdx = errors.length - 1;
		for (int i = 0; i <= lastIdx; i++)
		{
			if (i == 0)
			{
				message += " ";
			}
			else
			{
				message += ", ";
			}
			try
			{
				message += formatter
						.valueToString(errors[lastIdx - i].getBorePosition());
			}
			catch (ParseException ex)
			{
			}
		}
		message += ".";

		try
		{
			message += "\n\nThe likely cause is a final bore length shorter than the"
					+ "\nabove bore points. Delete all bore points below the point"
					+ "\nat position "
					+ formatter.valueToString(errors[lastIdx].getBorePosition())
					+ "."
					+ "\n\nRarely, you may have to increase the lower bore length "
					+ "\nbound (in the constraints) so it excludes the bore points "
					+ "\nyou want to protect";
		}
		catch (ParseException ex)
		{
		}

		return message;
	}

}
