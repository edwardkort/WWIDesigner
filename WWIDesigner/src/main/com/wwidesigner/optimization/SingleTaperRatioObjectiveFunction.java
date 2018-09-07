package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.Constraint.ConstraintType;

/**
 * Optimization objective function for a simple three-section bore with a single
 * tapered section. The foot diameter remains invariant. The position of the top
 * and bottom bore points remain unchanged. The optimization dimensions are:
 * <ul>
 * <li>Taper ratio head diameter / foot diameter.</li>
 * <li>Fraction of bore that is tapered.</li>
 * <li>Fraction of untapered length at head end.</li>
 * </ul>
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class SingleTaperRatioObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Single bore taper";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONLESS;

	public SingleTaperRatioObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator)
			throws Exception
	{
		super(aCalculator, tuning, aEvaluator);
		nrDimensions = 3;
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		setConstraints();
		setStartingGeometry();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Bore diameter ratio (top/bottom)", CONSTR_TYPE));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Taper length ratio to bore length", CONSTR_TYPE));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Untapered top length ratio to total untapered length",
				CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints
				.setObjectiveDisplayName("Single taper (dimensionless) optimizer");
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
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint nextPoint = (BorePoint) sortedPoints[1];
		BorePoint penultimatePoint = (BorePoint) sortedPoints[sortedPoints.length - 2];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double boreLength = bottomPoint.getBorePosition()
				- topPoint.getBorePosition();
		double taperStart;
		double taperEnd;

		geometry[0] = topPoint.getBoreDiameter()
				/ bottomPoint.getBoreDiameter();
		if (topPoint.getBoreDiameter() == bottomPoint.getBoreDiameter())
		{
			// Bore doesn't really taper.
			taperStart = topPoint.getBorePosition();
			taperEnd = bottomPoint.getBorePosition();
		}
		else
		{
			if (topPoint.getBoreDiameter() == nextPoint.getBoreDiameter())
			{
				// Taper starts on second point.
				taperStart = nextPoint.getBorePosition();
			}
			else
			{
				// Taper starts on first point.
				taperStart = topPoint.getBorePosition();
			}
			if (bottomPoint.getBoreDiameter() == penultimatePoint
					.getBoreDiameter())
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
		if (taperEnd - taperStart >= boreLength)
		{
			geometry[1] = 1.0;
			geometry[2] = 0.0;
		}
		else
		{
			geometry[1] = (taperEnd - taperStart) / boreLength;
			geometry[2] = (taperStart - topPoint.getBorePosition())
					/ (boreLength - (taperEnd - taperStart));
		}

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		// Replace existing bore points with a new list of up to 4 points.
		List<BorePoint> borePoints = new ArrayList<BorePoint>();

		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double footDiameter = bottomPoint.getBoreDiameter();
		double headDiameter = footDiameter * point[0];
		double boreLength = bottomPoint.getBorePosition()
				- topPoint.getBorePosition();
		double taperLength = boreLength * point[1];
		double taperStart = (boreLength - taperLength) * point[2];

		BorePoint newPoint = new BorePoint();
		newPoint.setBoreDiameter(headDiameter);
		newPoint.setBorePosition(topPoint.getBorePosition());
		borePoints.add(newPoint);
		if (taperStart > 0)
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
		newPoint.setBorePosition(topPoint.getBorePosition() + taperStart
				+ taperLength);
		borePoints.add(newPoint);
		if (taperStart + taperLength < boreLength)
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

	/**
	 * Reset the instrument borePoints, creating 4 borePoints:<br>
	 * 4. Last borePoint in original instrument profile - tail<br>
	 * 1. First borePoint in original profile, but with a bore diameter of 1.05
	 * of the tail<br>
	 * 2. Second borePoint a third of the way down the bore, with the first
	 * borePoint's diameter<br>
	 * 3. Third borePoint two-thirds down the bore, with the tail's diameter
	 * <p>
	 * These starting values seem to make the optimizer happy regardless of the
	 * final solution.
	 */
	public void setStartingGeometry()
	{
		Instrument instrument = calculator.getInstrument();
		PositionInterface[] sortedPoints = Instrument.sortList(instrument
				.getBorePoint());

		BorePoint head = (BorePoint) sortedPoints[0];
		BorePoint tail = (BorePoint) sortedPoints[sortedPoints.length - 1];

		double startPosition = head.getBorePosition();
		double boreLength = tail.getBorePosition() - startPosition;

		double tailDiameter = tail.getBoreDiameter();
		double headDiameter = tailDiameter * 1.05;

		head.setBoreDiameter(headDiameter);

		BorePoint taperStart = new BorePoint();
		taperStart.setBoreDiameter(headDiameter);
		taperStart.setBorePosition(boreLength / 3. + startPosition);

		BorePoint taperEnd = new BorePoint();
		taperEnd.setBoreDiameter(tailDiameter);
		taperEnd.setBorePosition(2. * boreLength / 3. + startPosition);

		List<BorePoint> points = new ArrayList<BorePoint>();
		points.add(head);
		points.add(taperStart);
		points.add(taperEnd);
		points.add(tail);
		instrument.setBorePoint(points);
	}

}
