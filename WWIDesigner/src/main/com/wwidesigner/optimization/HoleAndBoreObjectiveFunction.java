package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters, and bore
 * positions and diameters for existing bore points:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing from last hole
 * to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>Bore diameter at the foot.</li>
 * <li>For each interior bore point, excluding top and bottom, distance from
 * prior bore point to this bore point,as a fraction of the distance from the
 * prior bore point to the bottom.</li>
 * <li>For each interior bore point, excluding top and bottom, ratio of
 * diameters at next bore point to this bore point.</li>
 * </ul>
 * The position and diameter at the top <i>N</i> bore points are left unchanged.
 * 
 * Use of diameter ratios rather than absolute diameters allows constraints to
 * control the direction of taper. If lower bound is 1.0, bore flares out toward
 * bottom; if upper bound is 1.0, bore tapers inward toward bottom.
 * 
 * @author Burton Patkau
 * 
 */
public class HoleAndBoreObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole and bore optimizer";

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point position and diameter at existing bore points.
	 * 
	 * @param calculator
	 * @param tuning
	 * @param evaluator
	 * @param unchangedBorePoints
	 *            - Leave diameter and position unchanged for this many bore
	 *            points from the top of the bore.
	 */
	public HoleAndBoreObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator,
			int unchangedBorePoints)
	{
		super(calculator, tuning, evaluator);
		this.components = new BaseObjectiveFunction[4];
		// Since BorePositionObjectiveFunction uses ratios from the bottom
		// (intra-bell ratios), PRESERVE_BELL may have less impact on those
		// geometry dimensions than MOVE_BOTTOM.
		this.components[0] = new HolePositionObjectiveFunction(calculator,
				tuning, evaluator, BoreLengthAdjustmentType.PRESERVE_BELL);
		this.components[1] = new HoleSizeObjectiveFunction(calculator, tuning,
				evaluator);
		this.components[2] = new BorePositionObjectiveFunction(calculator,
				tuning, evaluator, unchangedBorePoints, true);
		this.components[3] = new BoreDiameterObjectiveFunction(calculator,
				tuning, evaluator, unchangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 60000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	@Override
	public double getStoppingTrustRegionRadius()
	{
		return 0.9e-6;
	}
}
