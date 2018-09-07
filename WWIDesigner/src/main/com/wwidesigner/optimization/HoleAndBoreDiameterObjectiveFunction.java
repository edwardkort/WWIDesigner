package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters,
 * and bore diameters at existing bore points:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing
 * from last hole to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
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
public class HoleAndBoreDiameterObjectiveFunction extends MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole and bore diameter optimizer";

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore diameters at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param unchangedBorePoints
	 *            - Leave diameter unchanged for this many bore points from the
	 *            top of the bore.
	 */
	public HoleAndBoreDiameterObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, 
			int unchangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, BoreLengthAdjustmentType.PRESERVE_BELL);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new BoreDiameterObjectiveFunction(aCalculator, tuning,
				aEvaluator, unchangedBorePoints);
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
