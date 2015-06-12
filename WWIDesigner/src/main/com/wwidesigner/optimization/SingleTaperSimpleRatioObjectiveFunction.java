package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.calculation.Tube;
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
 * <li>Length of the taper.</li>
 * <li>Length of un-tapered section at head end.</li>
 * </ul>
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class SingleTaperSimpleRatioObjectiveFunction extends
		BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Single bore taper";

	public SingleTaperSimpleRatioObjectiveFunction(
			InstrumentCalculator calculator, TuningInterface tuning,
			EvaluatorInterface evaluator) throws Exception
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 3;
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Bore diameter ratio (top/bottom)",
				ConstraintType.DIMENSIONLESS));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Taper start (from top), fraction of bore length",
				ConstraintType.DIMENSIONLESS));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Taper length, fraction of bore below start",
				ConstraintType.DIMENSIONLESS));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints
				.setObjectiveDisplayName("Single taper (simple ratios) optimizer");
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
		if (Math.abs(topPoint.getBoreDiameter() - bottomPoint.getBoreDiameter()) < 0.0001)
		{
			// Bore doesn't really taper.
			taperStart = topPoint.getBorePosition();
			taperEnd = bottomPoint.getBorePosition();
		}
		else
		{
			if (Math.abs(topPoint.getBoreDiameter()
					- nextPoint.getBoreDiameter()) < 0.0001)
			{
				// Taper starts on second point.
				taperStart = nextPoint.getBorePosition();
			}
			else
			{
				// Taper starts on first point.
				taperStart = topPoint.getBorePosition();
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
		double taperStart = point[1] * boreLength;
		double taperLength = Math.max(point[2] * (boreLength - taperStart),
				Tube.MINIMUM_CONE_LENGTH);

		BorePoint newPoint = new BorePoint();
		newPoint.setBoreDiameter(headDiameter);
		newPoint.setBorePosition(topPoint.getBorePosition());
		borePoints.add(newPoint);
		if (taperStart > 0)
		{
			// Taper begins on second point rather than first.
			newPoint = new BorePoint();
			newPoint.setBoreDiameter(headDiameter);
			newPoint.setBorePosition(topPoint.getBorePosition() + taperStart > boreLength ? boreLength
					: taperStart);
			borePoints.add(newPoint);
		}
		// Add point for end of taper.
		newPoint = new BorePoint();
		newPoint.setBoreDiameter(footDiameter);
		double taperEnd = taperStart + taperLength;
		taperEnd = taperEnd > boreLength ? boreLength : taperEnd;
		newPoint.setBorePosition(topPoint.getBorePosition() + taperEnd);
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
}
