package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Optimization objective function for bore point positions and diameters
 * at existing bore points:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each interior bore point, excluding top and bottom,
 * distance from prior bore point to this bore point,
 * as a fraction of the distance from the prior bore point to the bottom.</li>
 * <li>Bore diameter at the foot.</li>
 * <li>For each interior bore point, excluding top and bottom,
 * ratio of diameters at next bore point to this bore point.</li>
 * </ul>
 * The diameter at the top bore point is left invariant.
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to control
 * the direction of taper.  If lower bound is 1.0, bore flares out toward bottom;
 * if upper bound is 1.0, bore tapers inward toward bottom.
 * 
 * @author Burton Patkau
 * 
 */
public class BoreObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Bore point position and diameter optimizer";

	/**
	 * Create an optimization objective function for bore point position and
	 * diameter at existing bore points.
	 * 
	 * @param calculator
	 * @param tuning
	 * @param evaluator
	 * @param unchangedBorePoints
	 *            - Leave diameter and position unchanged for this many bore
	 *            points from the top of the bore.
	 */
	public BoreObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator, 
			int unchangedBorePoints)
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[2];
		this.components[0] = new BorePositionObjectiveFunction(calculator,
				tuning, evaluator, unchangedBorePoints);
		this.components[1] = new BoreDiameterObjectiveFunction(calculator,
				tuning, evaluator, unchangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 40000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	@Override
	public double getStoppingTrustRegionRadius()
	{
		return 0.8e-6;
	}
}
