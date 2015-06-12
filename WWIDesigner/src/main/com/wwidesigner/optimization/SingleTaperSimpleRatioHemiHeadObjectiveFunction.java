package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.calculation.HemisphericalBoreHead;
import com.wwidesigner.geometry.calculation.Tube;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for a simple three-section bore with a single
 * tapered section. The foot diameter remains invariant. The position of the top
 * and bottom bore points remain unchanged. Creates a hemispherical top of the
 * bore. The optimization dimensions are:
 * <ul>
 * <li>Taper ratio head diameter (measured a hemispherical equator) / foot
 * diameter.</li>
 * <li>Length of the taper.</li>
 * <li>Length of un-tapered section at head end.</li>
 * </ul>
 * 
 * @author Edward Kort
 * 
 */
public class SingleTaperSimpleRatioHemiHeadObjectiveFunction extends
		SingleTaperSimpleRatioObjectiveFunction
{
	protected final static int NUM_HEMI_POINTS = 10;

	public SingleTaperSimpleRatioHemiHeadObjectiveFunction(
			InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator) throws Exception
	{
		super(calculator, tuning, evaluator);
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Bore diameter ratio (top/bottom)",
				ConstraintType.DIMENSIONLESS));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Taper start (from hemi top), fraction of bore length",
				ConstraintType.DIMENSIONLESS));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Taper length, fraction of bore below start",
				ConstraintType.DIMENSIONLESS));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints
				.setObjectiveDisplayName("Single taper (simple ratios), hemi-head, optimizer");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		// Assume there are at least two points, taper starts on either the
		// first or second,
		// and ends on either the last or second last.
		// The position of the hemisphere is the bore top, the diameter is at
		// its equator.
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint hemiTopPoint = HemisphericalBoreHead
				.getHemiTopPoint(sortedPoints);
		BorePoint nextPoint = getNextPoint(hemiTopPoint.getBorePosition(),
				sortedPoints);
		BorePoint penultimatePoint = (BorePoint) sortedPoints[sortedPoints.length - 2];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double boreLength = bottomPoint.getBorePosition()
				- topPoint.getBorePosition();
		double taperStart;
		double taperEnd;

		geometry[0] = hemiTopPoint.getBoreDiameter()
				/ bottomPoint.getBoreDiameter();
		if (Math.abs(hemiTopPoint.getBoreDiameter()
				- bottomPoint.getBoreDiameter()) < 0.0001)
		{
			// Bore doesn't really taper.
			taperStart = hemiTopPoint.getBorePosition();
			taperEnd = bottomPoint.getBorePosition();
		}
		else
		{
			if (Math.abs(hemiTopPoint.getBoreDiameter()
					- nextPoint.getBoreDiameter()) < 0.0001)
			{
				// Taper starts on second point.
				taperStart = nextPoint.getBorePosition();
			}
			else
			{
				// Taper starts on first point.
				taperStart = hemiTopPoint.getBorePosition();
			}
			if (Math.abs(bottomPoint.getBoreDiameter()
					- penultimatePoint.getBoreDiameter()) < 0.0001)
			{
				// Taper ends on second-last point.
				taperEnd = penultimatePoint.getBorePosition();
			}
			else
			{
				// Taper ends on bottom point.
				taperEnd = bottomPoint.getBorePosition();
			}
		}

		geometry[1] = (taperStart / boreLength);
		geometry[2] = ((taperEnd - taperStart) / (boreLength - taperStart));

		return geometry;
	}

	/**
	 * Finds the next BorePoint below a specified position.
	 * 
	 * @param borePosition
	 * @param sortedPoints
	 * @return Null if no down-bore point is found.
	 */
	protected BorePoint getNextPoint(double borePosition,
			PositionInterface[] sortedPoints)
	{
		for (PositionInterface point : sortedPoints)
		{
			if (point.getBorePosition() > borePosition)
			{
				return (BorePoint) point;
			}
		}
		return null;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		// Replace existing bore points with a new list of up to 4 points plus a
		// hemispherical head.
		List<BorePoint> borePoints = new ArrayList<BorePoint>();

		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double footDiameter = bottomPoint.getBoreDiameter();
		double headDiameter = footDiameter * point[0];
		double topPosition = topPoint.getBorePosition();
		HemisphericalBoreHead
				.addHemiHead(topPosition, headDiameter, borePoints);
		double hemiTopPosition = borePoints.get(borePoints.size() - 1)
				.getBorePosition();
		double boreLength = bottomPoint.getBorePosition() - hemiTopPosition;
		double taperStart = point[1] * boreLength;
		double taperLength = Math.max(point[2] * (boreLength - taperStart),
				Tube.MINIMUM_CONE_LENGTH);

		BorePoint newPoint;
		if (taperStart > 0)
		{
			// Taper begins on second point rather than first.
			newPoint = new BorePoint();
			newPoint.setBoreDiameter(headDiameter);
			taperStart = taperStart > boreLength ? boreLength : taperStart;
			newPoint.setBorePosition(hemiTopPosition + taperStart);
			borePoints.add(newPoint);
		}
		// Add point for end of taper.
		newPoint = new BorePoint();
		newPoint.setBoreDiameter(footDiameter);
		double taperEnd = taperStart + taperLength;
		taperEnd = taperEnd > boreLength ? boreLength : taperEnd;
		newPoint.setBorePosition(hemiTopPosition + taperEnd);
		borePoints.add(newPoint);
		if (taperStart + taperLength < boreLength)
		{
			// Taper ends on second last point rather than last.
			newPoint = new BorePoint();
			newPoint.setBoreDiameter(footDiameter);
			newPoint.setBorePosition(hemiTopPosition + boreLength);
			borePoints.add(newPoint);
		}
		calculator.getInstrument().setBorePoint(borePoints);
		calculator.getInstrument().updateComponents();
	}

}
