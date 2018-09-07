package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters, and
 * positions of existing bore points:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing from last hole
 * to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>For each interior bore point between top and bottom, distance from prior
 * bore point to this bore point, as a fraction of the distance from the prior
 * bore point to the bottom.</li>
 * </ul>
 * The diameter at the top bore point is left invariant.
 * 
 * @author Burton Patkau
 * 
 */
public class HoleAndBorePositionObjectiveFunction extends
		MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole, plus bore-point position, optimizer";

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point position at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param unchangedBorePoints
	 *            - Leave position unchanged for this many bore points from the
	 *            top of the bore.
	 */
	public HoleAndBorePositionObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int unchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		// Since BorePositionObjectiveFunction uses ratios from the bottom
		// (intra-bell ratios), PRESERVE_BELL may have less impact on those
		// geometry dimensions than MOVE_BOTTOM.
		this.components[0] = new HolePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, BoreLengthAdjustmentType.PRESERVE_BELL);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new BorePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, unchangedBorePoints, true);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 50000;
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
