package com.wwidesigner.optimization;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;

/**
 * Optimization objective function for hole positions and diameters, and
 * positions of existing bore points:
 * <ul>
 * <li>Position of end of bore,</li>
 * <li>For each hole, spacing to next hole, ending with spacing from last hole
 * to end of bore.</li>
 * <li>For each hole, hole diameter.</li>
 * <li>For bore points from the top down, spacing from this bore point
 * to next bore point.</li>
 * </ul>
 * The bore points to vary can be specified as a number of bore points or with a
 * bore point name. The positions of bore points below these are left unchanged.
 * Bore point diameters are unchanged. <br>
 * 
 * @author Burton Patkau
 * 
 */
public class HoleAndBoreSpacingFromTopObjectiveFunction extends
		MergedObjectiveFunction
{
	public static final String DISPLAY_NAME = "Hole, plus bore-point spacing from top, optimizer";

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point spacing at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param aChangedBorePoints
	 *            - Number of bore points to optimize, from top.
	 */
	public HoleAndBoreSpacingFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator, int aChangedBorePoints)
	{
		super(aCalculator, tuning, aEvaluator);
		this.components = new BaseObjectiveFunction[3];
		this.components[0] = new HolePositionObjectiveFunction(aCalculator,
				tuning, aEvaluator, BoreLengthAdjustmentType.PRESERVE_TAPER);
		this.components[1] = new HoleSizeObjectiveFunction(aCalculator, tuning,
				aEvaluator);
		this.components[2] = new BoreSpacingFromTopObjectiveFunction(aCalculator,
				tuning, aEvaluator, aChangedBorePoints);
		optimizerType = OptimizerType.BOBYQAOptimizer; // MultivariateOptimizer
		maxEvaluations = 50000;
		sumDimensions();
		constraints.setObjectiveDisplayName(DISPLAY_NAME);
		constraints.setObjectiveFunctionName(this.getClass().getSimpleName());
		constraints.setConstraintsName("Default");
	}

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point spacing at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 * @param pointName
	 *            - The lowest bore point moved will be the lowest
	 *            bore point with a name that contains pointName.
	 */
	public HoleAndBoreSpacingFromTopObjectiveFunction(InstrumentCalculator aCalculator,
			TuningInterface tuning, EvaluatorInterface aEvaluator, String pointName)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction.getLowestPoint(
						aCalculator.getInstrument(), pointName));
	}

	/**
	 * Create an optimization objective function for hole positions and
	 * diameters, and bore point spacing at existing bore points.
	 * 
	 * @param aCalculator
	 * @param tuning
	 * @param aEvaluator
	 */
	public HoleAndBoreSpacingFromTopObjectiveFunction(
			InstrumentCalculator aCalculator, TuningInterface tuning,
			EvaluatorInterface aEvaluator)
	{
		this(aCalculator, tuning, aEvaluator,
				BoreDiameterFromTopObjectiveFunction.getLowestPoint(
						aCalculator.getInstrument(), "Head"));
	}

	@Override
	public double getStoppingTrustRegionRadius()
	{
		return 0.9e-6;
	}

}
