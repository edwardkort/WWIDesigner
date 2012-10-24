/**
 * 
 */
package com.wwidesigner.gui;

import com.wwidesigner.modelling.GordonCalculator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.optimization.FippleFactorOptimizer;
import com.wwidesigner.optimization.HoleGroupSpacingOptimizer;
import com.wwidesigner.optimization.HolePosAndDiamImpedanceOptimizer;
import com.wwidesigner.optimization.HoleSizeOptimizer;
import com.wwidesigner.optimization.InstrumentOptimizer;
import com.wwidesigner.optimization.SingleTaperHoleGroupingOptimizer;
import com.wwidesigner.optimization.run.BaseOptimizationRunner;
import com.wwidesigner.optimization.run.HoleGroupSpacingOptimizationRunnner;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class NafStudyModel extends StudyModel
{
	public static final String GORDON_CALC_SUB_CATEGORY_ID = "Gordon calculator";
	public static final String NAF_CALC_SUB_CATEGORY_ID = "NAF calculator";

	public static final String FIPPLE_OPT_SUB_CATEGORY_ID = "Fipple-factor Optimizer";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = "Hole-size Optimizer";
	public static final String NO_GROUP_OPT_SUB_CATEGORY_ID = "No-hole-grouping Optimizer";
	public static final String GROUP_OPT_SUB_CATEGORY_ID = "Hole-grouping Optimizer";
	public static final String TAPER_GROUP_OPT_SUB_CATEGORY_ID = "Taper, hole-grouping Optimizer";

	public static final String HOLE_0_CONS_SUB_CATEGORY_ID = "0 holes";
	public static final String HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/8\" max spacing";
	public static final String HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/4\" max spacing";
	public static final String HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/2\" max spacing";
	public static final String HOLE_7_CONS_SUB_CATEGORY_ID = "7 holes";

	public NafStudyModel()
	{
		super();
		setLocalCategories();
	}

	protected void setLocalCategories()
	{
		setParams(new PhysicalParameters(72.0, TemperatureType.F));
		Category calculators = new Category(CALCULATOR_CATEGORY_ID);
		calculators.addSub(GORDON_CALC_SUB_CATEGORY_ID, null);
		calculators.addSub(NAF_CALC_SUB_CATEGORY_ID, null);
		categories.add(calculators);
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(FIPPLE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(NO_GROUP_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(GROUP_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(TAPER_GROUP_OPT_SUB_CATEGORY_ID, null);
		categories.add(optimizers);
		Category constraints = new Category(CONSTRAINT_CATEGORY_ID);
		constraints.addSub(HOLE_0_CONS_SUB_CATEGORY_ID, null);
		constraints.addSub(HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID, null);
		constraints.addSub(HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID, null);
		constraints.addSub(HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID, null);
		constraints.addSub(HOLE_7_CONS_SUB_CATEGORY_ID, null);
		categories.add(constraints);
	}

	@Override
	protected InstrumentCalculator getCalculator()
	{
		Category calculatorCategory = getCategory(CALCULATOR_CATEGORY_ID);
		String calculatorSelected = calculatorCategory.getSelectedSub();
		InstrumentCalculator calculator = null;

		switch (calculatorSelected)
		{
			case GORDON_CALC_SUB_CATEGORY_ID:
				calculator = new GordonCalculator();
				break;
			case NAF_CALC_SUB_CATEGORY_ID:
				calculator = new NAFCalculator();
				break;
		}

		if ( calculator != null )
		{
		    calculator.setPhysicalParameters(params);
		}
		return calculator;
	}

	@Override
	protected InstrumentTuner getInstrumentTuner()
	{
		InstrumentTuner tuner = new SimpleInstrumentTuner();
		tuner.setParams(params);
		return tuner;
	}

	@Override
	protected BaseOptimizationRunner getOptimizationRunner()
	{
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		Category constraintCategory = getCategory(CONSTRAINT_CATEGORY_ID);
		BaseOptimizationRunner  runner;

		if ((GROUP_OPT_SUB_CATEGORY_ID.equals(optimizerCategory
				.getSelectedSub()) || TAPER_GROUP_OPT_SUB_CATEGORY_ID
				.equals(optimizerCategory.getSelectedSub()))
				&& !HOLE_0_CONS_SUB_CATEGORY_ID.equals(constraintCategory
						.getSelectedSub()))
		{
			runner = new HoleGroupSpacingOptimizationRunnner();
		}
		else {
			runner = new BaseOptimizationRunner();
		}
		
		runner.setParams(params);
		runner.setCalculator( getCalculator() );
		setConstraints(runner);

		return runner;
	}

	protected void setConstraints(BaseOptimizationRunner runner)
	{
		Category constraintCategory = getCategory(CONSTRAINT_CATEGORY_ID);
		String constraint = constraintCategory.getSelectedSub();
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();
		switch (optimizer)
		{
			case FIPPLE_OPT_SUB_CATEGORY_ID:
				runner.setOptimizerClass(FippleFactorOptimizer.class);
				runner.setLowerBound(new double[] { 0.2 });
				runner.setUpperBound(new double[] { 1.5 });
				runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
				runner.setNumberOfInterpolationPoints(2);
				break;
			case HOLESIZE_OPT_SUB_CATEGORY_ID:
				runner.setOptimizerClass(HoleSizeOptimizer.class);
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[0]);
						runner.setUpperBound(new double[0]);
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(1);
						break;
					case HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.1, 0.15, 0.15,
								0.15, 0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.5, 0.5, 0.5, 0.5,
								0.5, 0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(12);
						break;
					case HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.1, 0.15, 0.15,
								0.15, 0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.5, 0.5, 0.5, 0.5,
								0.5, 0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(12);
						break;
					case HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.1, 0.15, 0.15,
								0.15, 0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.5, 0.5, 0.5, 0.5,
								0.5, 0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(12);
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.1, 0.1, 0.1, 0.1,
								0.1, 0.05, 0.05 });
						runner.setUpperBound(new double[] { 0.7, 0.7, 0.7, 0.7,
								0.7, 0.4, 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(14);
						break;
				}
				break;
			case NO_GROUP_OPT_SUB_CATEGORY_ID:
				runner.setOptimizerClass(HolePosAndDiamImpedanceOptimizer.class);
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.25 });
						runner.setUpperBound(new double[] { 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(2);
						break;
					case HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.25, 0.01, 0.01,
								0.01, 0.01, 0.01, 0.01, 0.1, 0.15, 0.15, 0.15,
								0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.6, 0.029, 0.029,
								0.07, 0.029, 0.029, 0.3, 0.5, 0.5, 0.5, 0.5, 0.5,
								0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(26);
						break;
					case HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.25, 0.01, 0.01,
								0.01, 0.01, 0.01, 0.01, 0.1, 0.15, 0.15, 0.15,
								0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.6, 0.032, 0.032,
								0.07, 0.032, 0.032, 0.3, 0.5, 0.5, 0.5, 0.5, 0.5,
								0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(26);
						break;
					case HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.25, 0.01, 0.01,
								0.01, 0.01, 0.01, 0.01, 0.1, 0.15, 0.15, 0.15,
								0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.6, 0.038, 0.038,
								0.07, 0.038, 0.038, 0.3, 0.5, 0.5, 0.5, 0.5, 0.5,
								0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(26);
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.012, 0.012, 0.0005, 0.05, 0.1, 0.1,
								0.1, 0.1, 0.1, 0.05, 0.05 });
						runner.setUpperBound(new double[] { 0.5, 0.05, 0.05,
								0.1, 0.05, 0.05, 0.003, 0.2, 0.7, 0.7, 0.7,
								0.7, 0.7, 0.4, 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(30);
						break;
				}
				break;
			case GROUP_OPT_SUB_CATEGORY_ID:
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HolePosAndDiamImpedanceOptimizer.class);
						runner.setLowerBound(new double[] { 0.25 });
						runner.setUpperBound(new double[] { 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(2);
						break;
					case HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HoleGroupSpacingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 });
						runner.setUpperBound(new double[] { 0.8, 0.029, 0.1,
								0.029, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(22);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HoleGroupSpacingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 });
						runner.setUpperBound(new double[] { 0.8, 0.032, 0.1,
								0.032, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(22);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HoleGroupSpacingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 });
						runner.setUpperBound(new double[] { 0.8, 0.038, 0.1,
								0.038, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(22);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HoleGroupSpacingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.0005, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1,
								0.05, 0.05 });
						runner.setUpperBound(new double[] { 0.5, 0.05, 0.05,
								0.1, 0.003, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7, 0.4,
								0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(26);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 }, { 6 } });
						break;
				}
				break;
			case TAPER_GROUP_OPT_SUB_CATEGORY_ID:
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HolePosAndDiamImpedanceOptimizer.class);
						runner.setLowerBound(new double[] { 0.25 });
						runner.setUpperBound(new double[] { 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(2);
						break;
					case HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(SingleTaperHoleGroupingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.01, 0.01,
								0.01, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.5,
								0.0, 0.0 });
						runner.setUpperBound(new double[] { 0.8, 0.029, 0.1,
								0.029, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7, 2.0,
								0.8, 0.8 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(28);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(SingleTaperHoleGroupingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.01, 0.01,
								0.01, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.5,
								0.0, 0.0 });
						runner.setUpperBound(new double[] { 0.8, 0.032, 0.1,
								0.032, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7, 2.0,
								0.8, 0.8 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(28);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(SingleTaperHoleGroupingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.01, 0.01,
								0.01, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.5,
								0.0, 0.0 });
						runner.setUpperBound(new double[] { 0.8, 0.038, 0.1,
								0.038, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7, 2.0,
								0.8, 0.8 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(28);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(SingleTaperHoleGroupingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.0005, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1,
								0.05, 0.05, 0.5, 0.0, 0.0 });
						runner.setUpperBound(new double[] { 0.5, 0.032, 0.05,
								0.032, 0.003, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7,
								0.4, 0.4, 2.0, 0.5, 0.5 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(32);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 }, { 6 } });
						break;
				}
				break;
		}
	}

}
