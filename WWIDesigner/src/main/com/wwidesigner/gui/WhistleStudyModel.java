/**
 * 
 */
package com.wwidesigner.gui;

import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentRangeTuner;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.optimization.FippleFactorOptimizer;
import com.wwidesigner.optimization.HoleSizeOptimizer;
import com.wwidesigner.optimization.InstrumentOptimizer;
import com.wwidesigner.optimization.run.BaseOptimizationRunner;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author Burton Patkau
 * 
 */
public class WhistleStudyModel extends StudyModel
{
	public static final String WINDOW_OPT_SUB_CATEGORY_ID = "Window Height Calibrator";
	public static final String BETA_OPT_SUB_CATEGORY_ID = "Beta Calibrator";
	public static final String LENGTH_OPT_SUB_CATEGORY_ID = "Length Optimizer";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = "Hole Size Optimizer";
	public static final String HOLESPACE_OPT_SUB_CATEGORY_ID = "Hole Spacing Optimizer";

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

}
