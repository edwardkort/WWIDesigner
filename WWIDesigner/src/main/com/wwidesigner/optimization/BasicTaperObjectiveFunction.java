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
 * Optimization objective function for a simple two-section tapered bore.
 * The bore has two sections.  The diameter at the head, and diameter between the two
 * sections are left invariant.  The optimization dimensions are:
 * <ul>
 * <li>Length of head section, as a fraction of total bore length.</li>
 * <li>Taper ratio foot diameter / middle diameter.</li>
 * </ul>
 * 
 * @author Edward Kort, Burton Patkau
 * 
 */
public class BasicTaperObjectiveFunction extends BaseObjectiveFunction
{
	public static final String CONSTR_CAT = "Simple taper";
	public static final ConstraintType CONSTR_TYPE = ConstraintType.DIMENSIONLESS;

	public BasicTaperObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
		nrDimensions = 2;
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		setConstraints();
	}

	protected void setConstraints()
	{
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Head length ratio (to bore length)", CONSTR_TYPE));
		constraints.addConstraint(new Constraint(CONSTR_CAT,
				"Foot diameter ratio (foot/middle)", CONSTR_TYPE));
		constraints.setNumberOfHoles(calculator.getInstrument().getHole()
				.size());
		constraints
				.setObjectiveDisplayName("Basic taper (dimensionless) optimizer");
	}

	@Override
	public double[] getGeometryPoint()
	{
		double[] geometry = new double[nrDimensions];
		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		// Assume there are at least two points, taper to be optimized starts on the
		// second, and ends on the last. (bottomPoint and middlePoint may be the
		// same point.)
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint middlePoint = (BorePoint) sortedPoints[1];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		geometry[0] = (middlePoint.getBorePosition() - topPoint.getBorePosition())
				/ (bottomPoint.getBorePosition() - topPoint.getBorePosition());
		geometry[1] = bottomPoint.getBoreDiameter()	/ middlePoint.getBoreDiameter();

		return geometry;
	}

	@Override
	public void setGeometryPoint(double[] point)
	{
		// Replace existing bore points with a new list of 3 points.
		List<BorePoint> borePoints = new ArrayList<BorePoint>();

		PositionInterface[] sortedPoints = Instrument.sortList(calculator
				.getInstrument().getBorePoint());
		BorePoint topPoint = (BorePoint) sortedPoints[0];
		BorePoint middlePoint = (BorePoint) sortedPoints[1];
		BorePoint bottomPoint = (BorePoint) sortedPoints[sortedPoints.length - 1];
		double boreLength = bottomPoint.getBorePosition()
				- topPoint.getBorePosition();

		// First point doesn't change at all.
		borePoints.add(topPoint);

		// Second point changes position, but not diameter.
		middlePoint.setBorePosition( boreLength * point[0] + topPoint.getBorePosition() );
		borePoints.add(middlePoint);

		// Create a new bottom point in case bottomPoint was identical to middlePoint.
		// Bottom point changes diameter, but not position.
		BorePoint newPoint = new BorePoint();
		newPoint.setBoreDiameter(middlePoint.getBoreDiameter() * point[1]);
		newPoint.setBorePosition(boreLength + topPoint.getBorePosition());
		borePoints.add(newPoint);

		calculator.getInstrument().setBorePoint(borePoints);
		calculator.getInstrument().updateComponents();
	}

}
