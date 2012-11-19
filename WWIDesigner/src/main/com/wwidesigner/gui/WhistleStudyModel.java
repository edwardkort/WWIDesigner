/**
 * 
 */
package com.wwidesigner.gui;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.BellNoteEvaluator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.FmaxEvaluator;
import com.wwidesigner.modelling.FminEvaluator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentRangeTuner;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.modelling.WhistleEvaluator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.BetaObjectiveFunction;
import com.wwidesigner.optimization.FippleFactorOptimizer;
import com.wwidesigner.optimization.HoleObjectiveFunction;
import com.wwidesigner.optimization.HolePositionObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeOptimizer;
import com.wwidesigner.optimization.InstrumentOptimizer;
import com.wwidesigner.optimization.LengthObjectiveFunction;
import com.wwidesigner.optimization.WindowHeightObjectiveFunction;
import com.wwidesigner.optimization.run.BaseOptimizationRunner;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author Burton Patkau
 * 
 */
public class WhistleStudyModel extends StudyModel
{
	public static final String WINDOW_OPT_SUB_CATEGORY_ID = "1. Window Height Calibrator";
	public static final String BETA_OPT_SUB_CATEGORY_ID = "2. Beta Calibrator";
	public static final String LENGTH_OPT_SUB_CATEGORY_ID = "3. Length Optimizer";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = "4. Hole Size Optimizer";
	public static final String HOLESPACE_OPT_SUB_CATEGORY_ID = "5. Hole Spacing Optimizer";
	public static final String HOLE_OPT_SUB_CATEGORY_ID = "6. Hole Size+Spacing Optimizer";

	public WhistleStudyModel()
	{
		super();
		setLocalCategories();
	}

	protected void setLocalCategories()
	{
		setParams(new PhysicalParameters(28.2, TemperatureType.C));
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(WINDOW_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(BETA_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(LENGTH_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLESPACE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLE_OPT_SUB_CATEGORY_ID, null);
		categories.add(optimizers);
	}

	@Override
	protected InstrumentCalculator getCalculator()
	{
		InstrumentCalculator calculator = new WhistleCalculator();
		calculator.setPhysicalParameters(params);

		return calculator;
	}

	@Override
	protected InstrumentTuner getInstrumentTuner()
	{
		InstrumentTuner tuner = new InstrumentRangeTuner();
		tuner.setParams(params);
		return tuner;
	}

	@Override
	protected BaseOptimizationRunner getOptimizationRunner()
	{
		// Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		// Category constraintCategory = getCategory(CONSTRAINT_CATEGORY_ID);
		BaseOptimizationRunner  runner;

		runner = new BaseOptimizationRunner();
		runner.setParams(params);
		runner.setCalculator( getCalculator() );
		setConstraints(runner);

		return runner;
	}

	protected void setConstraints(BaseOptimizationRunner runner)
	{
		// Category constraintCategory = getCategory(CONSTRAINT_CATEGORY_ID);
		// String constraint = constraintCategory.getSelectedSub();
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();
		Class<? extends InstrumentOptimizer> optimizerClass = null;
		double[] lowerBound = null;
		double[] upperBound = null;
		InstrumentOptimizer.OptimizerType optimizerType = null;
		int nrInterpolations = 0;

		switch (optimizer)
		{
			case WINDOW_OPT_SUB_CATEGORY_ID:
				optimizerClass = FippleFactorOptimizer.class;
				lowerBound = new double[] { 0.2 };
				upperBound = new double[] { 1.5 };
				optimizerType = InstrumentOptimizer.OptimizerType.CMAESOptimizer;
				nrInterpolations = 0;
				break;
			case BETA_OPT_SUB_CATEGORY_ID:
				optimizerClass = FippleFactorOptimizer.class;
				lowerBound = new double[] { 0.3 };
				upperBound = new double[] { 0.4 };
				optimizerType = InstrumentOptimizer.OptimizerType.CMAESOptimizer;
				nrInterpolations = 0;
				break;
			case LENGTH_OPT_SUB_CATEGORY_ID:
				optimizerClass = FippleFactorOptimizer.class;
				lowerBound = new double[] { 0.0 };
				upperBound = new double[] { 1000.0 };
				optimizerType = InstrumentOptimizer.OptimizerType.CMAESOptimizer;
				nrInterpolations = 0;
				break;
			case HOLESIZE_OPT_SUB_CATEGORY_ID:
				optimizerClass = HoleSizeOptimizer.class;
				lowerBound = new double[] { 0.1, 0.15, 0.15, 0.15, 0.15, 0.15 };
				upperBound = new double[] { 0.5, 0.5,  0.5,  0.5,  0.5,  0.6 };
				optimizerType = InstrumentOptimizer.OptimizerType.BOBYQAOptimizer;
				nrInterpolations = 28;
				break;
		}

		runner.setOptimizerClass(optimizerClass);
		runner.setOptimizerType(optimizerType);
		runner.setLowerBound(lowerBound);
		runner.setUpperBound(upperBound);
		runner.setNumberOfInterpolationPoints(nrInterpolations);

	} // setConstraints

	@Override
	protected BaseObjectiveFunction getObjectiveFunction() throws Exception
	{
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();
		Instrument instrument = getInstrument();
		Tuning tuning = getTuning();
		WhistleCalculator calculator = new WhistleCalculator();
		EvaluatorInterface evaluator;
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);

		BaseObjectiveFunction  objective = null;
		double[] lowerBound = null;
		double[] upperBound = null;

		switch (optimizer)
		{
			case WINDOW_OPT_SUB_CATEGORY_ID:
				evaluator = new FmaxEvaluator(calculator);
				objective = new WindowHeightObjectiveFunction(calculator, tuning, evaluator);
				lowerBound = new double[] { 0.000 };
				upperBound = new double[] { 0.010 };
				break;
			case BETA_OPT_SUB_CATEGORY_ID:
				evaluator = new FminEvaluator(calculator);
				objective = new BetaObjectiveFunction(calculator, tuning, evaluator);
				lowerBound = new double[] { 0.2 };
				upperBound = new double[] { 0.5 };
				break;
			case LENGTH_OPT_SUB_CATEGORY_ID:
				evaluator = new BellNoteEvaluator(calculator);
				objective = new LengthObjectiveFunction(calculator, tuning, evaluator);
				// LengthObjectiveFunction calculates its own lower bound, from the instrument geometry.
				lowerBound = objective.getLowerBounds();
				upperBound = new double[] { 2.000 };
				break;
			case HOLESIZE_OPT_SUB_CATEGORY_ID:
				evaluator = new WhistleEvaluator(calculator);
				objective = new HoleSizeObjectiveFunction(calculator, tuning, evaluator);
				// Bounds are expressed as diameter ratios, relative to bore diameter.
				lowerBound = new double[] { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };
				upperBound = new double[] { 0.9, 0.9, 0.9, 0.9, 0.9, 0.9 };
				break;
			case HOLESPACE_OPT_SUB_CATEGORY_ID:
				evaluator = new WhistleEvaluator(calculator);
				objective = new HolePositionObjectiveFunction(calculator, tuning, evaluator);
				// Bounds are expressed in meters.
				lowerBound = new double[] { 0.010, 0.010, 0.010, 0.010, 0.010, 0.010, 0.010 };
				upperBound = new double[] { 1.000, 0.050, 0.050, 0.050, 0.050, 0.050, 0.200 };
				break;
			case HOLE_OPT_SUB_CATEGORY_ID:
				evaluator = new WhistleEvaluator(calculator);
				objective = new HoleObjectiveFunction(calculator, tuning, evaluator);
				// Length bounds are expressed in meters, diameter bounds as ratios.
				lowerBound = new double[] { 0.010, 0.010, 0.010, 0.010, 0.010, 0.010, 0.010,
						0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };
				upperBound = new double[] { 1.000, 0.050, 0.050, 0.050, 0.050, 0.050, 0.200,
						0.9, 0.9, 0.9, 0.9, 0.9, 0.9 };
				break;
		}

		objective.setLowerBounds(lowerBound);
		objective.setUpperBounds(upperBound);
		return objective;
	}

}
