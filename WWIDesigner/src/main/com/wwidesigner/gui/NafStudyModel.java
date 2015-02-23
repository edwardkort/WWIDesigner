/**
 * Study model class to analyze and optimize Native American Flutes.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.gui.util.DataOpenException;
import com.wwidesigner.modelling.CentDeviationEvaluator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.GordonCalculator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.FippleFactorObjectiveFunction;
import com.wwidesigner.optimization.HoleFromTopObjectiveFunction;
import com.wwidesigner.optimization.HoleGroupFromTopObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeObjectiveFunction;
import com.wwidesigner.optimization.SingleTaperHoleGroupFromTopObjectiveFunction;
import com.wwidesigner.optimization.SingleTaperNoHoleGroupingFromTopObjectiveFunction;
import com.wwidesigner.optimization.multistart.GridRangeProcessor;
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

	public static final String FIPPLE_OPT_SUB_CATEGORY_ID = FippleFactorObjectiveFunction.DISPLAY_NAME;
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = HoleSizeObjectiveFunction.DISPLAY_NAME;
	public static final String NO_GROUP_OPT_SUB_CATEGORY_ID = HoleFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String GROUP_OPT_SUB_CATEGORY_ID = HoleGroupFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String TAPER_GROUP_OPT_SUB_CATEGORY_ID = SingleTaperHoleGroupFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String TAPER_NO_GROUP_OPT_SUB_CATEGORY_ID = SingleTaperNoHoleGroupingFromTopObjectiveFunction.DISPLAY_NAME;

	public static final String HOLE_0_CONS_SUB_CATEGORY_ID = "0 holes";
	public static final String HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/8\" max spacing";
	public static final String HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/4\" max spacing";
	public static final String HOLE_6_40_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1.40\" max spacing";
	public static final String HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/2\" max spacing";
	public static final String HOLE_7_CONS_SUB_CATEGORY_ID = "7 holes";

	public static final String NO_MULTI_START_SUB_CATEGORY_ID = "No multi-start optimization";
	public static final String VARY_FIRST_MULTI_START_SUB_CATEGORY_ID = "Vary first bound variable";
	public static final String VARY_ALL_MULTI_START_SUB_CATEGORY_ID = "Vary all dimensions";

	protected int numberOfStarts = 30;

	// Key is a concatenation of the ObjectiveFunction and the number of holes.
	// Values is a Category containing the list of Constraints (as XML) for the
	// key.
	protected Map<String, Category> constraintsByOptimizer = new HashMap<String, Category>();

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
		calculators.setSelectedSub(NAF_CALC_SUB_CATEGORY_ID);
		categories.add(calculators);
		Category multiStart = new Category(MULTI_START_CATEGORY_ID);
		multiStart.addSub(NO_MULTI_START_SUB_CATEGORY_ID, null);
		multiStart.addSub(VARY_FIRST_MULTI_START_SUB_CATEGORY_ID, null);
		multiStart.addSub(VARY_ALL_MULTI_START_SUB_CATEGORY_ID, null);
		// Default to no multi-start
		multiStart.setSelectedSub(NO_MULTI_START_SUB_CATEGORY_ID);
		categories.add(multiStart);
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(FIPPLE_OPT_SUB_CATEGORY_ID,
				FippleFactorObjectiveFunction.NAME);
		optimizers.addSub(NO_GROUP_OPT_SUB_CATEGORY_ID,
				HoleFromTopObjectiveFunction.NAME);
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID,
				HoleSizeObjectiveFunction.NAME);
		optimizers.addSub(GROUP_OPT_SUB_CATEGORY_ID,
				HoleGroupFromTopObjectiveFunction.NAME);
		optimizers.addSub(TAPER_NO_GROUP_OPT_SUB_CATEGORY_ID,
				SingleTaperNoHoleGroupingFromTopObjectiveFunction.NAME);
		optimizers.addSub(TAPER_GROUP_OPT_SUB_CATEGORY_ID,
				SingleTaperHoleGroupFromTopObjectiveFunction.NAME);
		categories.add(optimizers);
		Category constraints = new Category(CONSTRAINTS_CATEGORY_ID);
		// constraints.addSub(HOLE_0_CONS_SUB_CATEGORY_ID, null);
		// constraints.addSub(HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID, null);
		// constraints.addSub(HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID, null);
		// constraints.addSub(HOLE_6_40_SPACING_CONS_SUB_CATEGORY_ID, null);
		// constraints.addSub(HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID, null);
		// constraints.addSub(HOLE_7_CONS_SUB_CATEGORY_ID, null);
		categories.add(constraints);
	}

	@Override
	public boolean canTune() throws Exception
	{
		boolean tuningReady = super.canTune();
		if (tuningReady)
		{
			Category calculatorCategory = getCategory(CALCULATOR_CATEGORY_ID);
			String calculatorSelected = calculatorCategory.getSelectedSub();
			tuningReady = calculatorSelected != null;
		}

		return tuningReady;
	}

	@Override
	public boolean canOptimize() throws Exception
	{
		boolean optimizeReady = super.canOptimize();
		if (optimizeReady)
		{
			Category category = getCategory(CONSTRAINTS_CATEGORY_ID);
			String constraintsSelected = category.getSelectedSub();
			optimizeReady = constraintsSelected != null;
		}

		return optimizeReady;
	}

	@Override
	public boolean isOptimizerFullySpecified(String constraintsDirectory)
	{
		boolean isSpecified;
		if (constraintsDirectory == null
				|| constraintsDirectory.trim().length() == 0)
		{
			return false;
		}
		Integer dataNumberOfHoles = getNumberOfHolesFromInstrument();
		String optimizerSelected = getSelectedSub(OPTIMIZER_CATEGORY_ID);
		// Constraints set is fully specified.
		isSpecified = dataNumberOfHoles != null && optimizerSelected != null;

		return isSpecified;
	}

	/**
	 * Update/change the Constraints Category upon selection changes in the
	 * other Category selections.
	 */
	@Override
	public void updateConstraints()
	{
		super.updateConstraints();

		Integer dataNumberOfHoles = getNumberOfHolesFromInstrument();
		String optimizerSelected = getSelectedSub(OPTIMIZER_CATEGORY_ID);

		// Constraints set is fully specified. Retrieve it.
		if (dataNumberOfHoles != null && optimizerSelected != null)
		{
			resetConstraints(optimizerSelected, dataNumberOfHoles);
		}
		// If the optimizer matches the constraints set, leave it. Otherwise,
		// put in a dummy blank one: there is not enough information to select
		// the correct one.
		else if (optimizerSelected != null)
		{
			if (!constraintsAndOptimizerCompatible(
					getCategory(CONSTRAINTS_CATEGORY_ID), optimizerSelected,
					true))
			{
				replaceCategory(CONSTRAINTS_CATEGORY_ID, new Category(
						CONSTRAINTS_CATEGORY_ID));
			}
		}
		// If the number of holes match the constraints set, leave it.
		// Otherwise, put in a dummy one.
		else if (dataNumberOfHoles != null)
		{
			if (!constraintsAndHoleNumberCompatible(
					getCategory(CONSTRAINTS_CATEGORY_ID), dataNumberOfHoles))
			{
				replaceCategory(CONSTRAINTS_CATEGORY_ID, new Category(
						CONSTRAINTS_CATEGORY_ID));
			}
		}
		// If there is neither hole number nor optimizer information, leave
		// whatever Constraints category is already displayed.
	}

	protected boolean constraintsAndHoleNumberCompatible(
			Category constraintsCategory, int numberOfHoles)
	{
		// If the category is empty, it is compatible
		Map<String, Object> subs = constraintsCategory.getSubs();
		if (subs == null || subs.isEmpty())
		{
			return true;
		}
		// Check the first sub for number of holes
		FileDataModel dataModel = (FileDataModel) subs.values().iterator()
				.next();
		Constraints constraints = getConstraints((String) dataModel.getData());

		return numberOfHoles == constraints.getNumberOfHoles();
	}

	protected boolean constraintsAndOptimizerCompatible(
			Category constraintsCategory, String optimizerName,
			boolean isDisplayName)
	{
		// If the category is empty, it is compatible
		Map<String, Object> subs = constraintsCategory.getSubs();
		if (subs == null || subs.isEmpty())
		{
			return true;
		}
		// Check the first sub for associated optimizer
		FileDataModel dataModel = (FileDataModel) subs.values().iterator()
				.next();
		Constraints constraints = getConstraints((String) dataModel.getData());
		String constraintsOptimizerName = isDisplayName ? constraints
				.getObjectiveDisplayName() : constraints
				.getObjectiveFunctionName();

		return constraintsOptimizerName.equals(optimizerName);
	}

	protected void resetConstraints(String optimizerSelected,
			Integer dataNumberOfHoles)
	{
		String mappedKey = makeMappedConstraintsKey(optimizerSelected, true,
				dataNumberOfHoles);
		Category category = getMappedCategory(mappedKey);
		replaceCategory(CONSTRAINTS_CATEGORY_ID, category);
	}

	private String makeMappedConstraintsKey(String optimizerName,
			boolean isDisplayName, int numberOfHoles)
	{
		String optimizerClassName;
		if (isDisplayName)
		{
			optimizerClassName = (String) getCategory(OPTIMIZER_CATEGORY_ID)
					.getSelectedSubValue();
		}
		else
		{
			optimizerClassName = optimizerName;
		}

		return optimizerClassName + "_" + numberOfHoles;
	}

	@Override
	public File getConstraintsLeafDirectory(String rootDirectoryPath)
	{
		String studyModelName = getClass().getSimpleName();
		String objectiveFunctionName = (String) getCategory(
				OPTIMIZER_CATEGORY_ID).getSelectedSubValue();
		int numberOfHoles = getNumberOfHolesFromInstrument();
		File leaf = makeConstraintsDirectoryPath(rootDirectoryPath,
				studyModelName, objectiveFunctionName, numberOfHoles);

		return leaf;
	}

	@Override
	public File getConstraintsLeafDirectory(String rootDirectoryPath,
			Constraints constraints)
	{
		String studyModelName = getClass().getSimpleName();
		String objectiveFunctionName = constraints.getObjectiveFunctionName();
		int numberOfHoles = constraints.getNumberOfHoles();
		File leaf = makeConstraintsDirectoryPath(rootDirectoryPath,
				studyModelName, objectiveFunctionName, numberOfHoles);

		return leaf;
	}

	protected File makeConstraintsDirectoryPath(String rootPath,
			String studyModelName, String objectiveFunctionName,
			int numberOfHoles)
	{
		String path = rootPath + File.separator + studyModelName
				+ File.separator + objectiveFunctionName + File.separator
				+ numberOfHoles;
		File leaf = new File(path);
		if (!leaf.exists())
		{
			leaf.mkdirs();
		}

		return leaf;
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

		if (calculator != null)
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

	protected BaseObjectiveFunction getObjectiveFunction(
			int objectiveFunctionIntent) throws Exception
	{
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();

		Instrument instrument = getInstrument();
		Tuning tuning;
		if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
		{
			tuning = getTuning();
		}
		else
		{
			tuning = new Tuning();
		}
		InstrumentCalculator calculator = getCalculator();
		calculator.setInstrument(instrument);
		EvaluatorInterface evaluator = new CentDeviationEvaluator(calculator);
		// EvaluatorInterface evaluator = new ReflectionEvaluator(calculator);
		int numberOfHoles = instrument.getHole().size();

		BaseObjectiveFunction objective = null;
		double[] lowerBound = null;
		double[] upperBound = null;
		int[][] holeGroups = null;

		switch (optimizer)
		{
			case FIPPLE_OPT_SUB_CATEGORY_ID:
				objective = new FippleFactorObjectiveFunction(calculator,
						tuning, evaluator);
				if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
				{
					lowerBound = new double[] { 0.2 };
					upperBound = new double[] { 1.5 };
				}
				break;
			case HOLESIZE_OPT_SUB_CATEGORY_ID:
				objective = new HoleSizeObjectiveFunction(calculator, tuning,
						evaluator);
				if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
				{
					// Bounds are hole diameters expressed in meters.
					if (numberOfHoles == 0)
					{
						lowerBound = new double[0];
						upperBound = new double[0];
					}
					else if (numberOfHoles == 7)
					{
						lowerBound = new double[] { 0.002, 0.002, 0.002, 0.002,
								0.002, 0.002, 0.002 };
						upperBound = new double[] { 0.014, 0.014, 0.014, 0.014,
								0.014, 0.008, 0.008 };
					}
					else if (numberOfHoles == 6)
					// Assume 6 holes.
					{
						lowerBound = new double[] { 0.002, 0.003, 0.003, 0.003,
								0.003, 0.003 };
						upperBound = new double[] { 0.0102, 0.0102, 0.010,
								0.010, 0.010, 0.012 };
					}
				}
				break;
			case NO_GROUP_OPT_SUB_CATEGORY_ID:
				objective = new HoleFromTopObjectiveFunction(calculator,
						tuning, evaluator);
				if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
				{
					// Length bounds are expressed in meters, diameter bounds as
					// ratios.
					if (numberOfHoles == 0)
					{
						lowerBound = new double[] { 0.2 };
						upperBound = new double[] { 0.7 };
					}
					else if (numberOfHoles == 7)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.0203, 0.0203, 0.0005, 0.012, 0.002,
								0.002, 0.002, 0.002, 0.002, 0.002, 0.002 };
						upperBound = new double[] { 0.7, 0.50, 0.05, 0.05, 0.1,
								0.05, 0.05, 0.003, 0.014, 0.014, 0.014, 0.014,
								0.014, 0.008, 0.008 };
					}
					else if (numberOfHoles == 6)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.0203, 0.0203, 0.002, 0.003, 0.003,
								0.003, 0.003, 0.003 };
						upperBound = new double[] { 0.7, 0.50, 0.032, 0.032,
								0.07, 0.032, 0.032, 0.0102, 0.0102, 0.010,
								0.010, 0.010, 0.012 };
					}
				}
				break;
			case GROUP_OPT_SUB_CATEGORY_ID:
				// Length bounds are expressed in meters, diameter bounds as
				// ratios.
				if (numberOfHoles == 0)
				{
					holeGroups = new int[][] { {} };
					if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
					{
						lowerBound = new double[] { 0.2 };
						upperBound = new double[] { 0.7 };
					}
				}
				else if (numberOfHoles == 7)
				{
					holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
					if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.0005, 0.002, 0.002, 0.002, 0.002,
								0.002, 0.002, 0.002 };
						upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1,
								0.003, 0.014, 0.014, 0.014, 0.014, 0.014,
								0.008, 0.008 };
					}
				}
				else if (numberOfHoles == 6)
				{
					holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
					if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.002, 0.003, 0.003, 0.003, 0.003,
								0.003 };
						upperBound = new double[] { 0.7, 1.0, 0.032, 0.07,
								0.032, 0.0102, 0.0102, 0.010, 0.010, 0.010,
								0.012 };
					}
				}
				objective = new HoleGroupFromTopObjectiveFunction(calculator,
						tuning, evaluator, holeGroups);
				break;
			case TAPER_NO_GROUP_OPT_SUB_CATEGORY_ID:
				objective = new SingleTaperNoHoleGroupingFromTopObjectiveFunction(
						calculator, tuning, evaluator);
				if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
				{
					// Length bounds are expressed in meters, diameter bounds as
					// ratios,
					// taper bounds as ratios.
					if (numberOfHoles == 0)
					{
						lowerBound = new double[] { 0.2, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.7, 1.2, 1.0, 1.0 };
					}
					else if (numberOfHoles == 7)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.0203, 0.0203, 0.0005, 0.012, 0.002,
								0.002, 0.002, 0.002, 0.002, 0.002, 0.002, 0.8,
								0.0, 0.0 };
						upperBound = new double[] { 0.7, 0.50, 0.05, 0.05, 0.1,
								0.05, 0.05, 0.003, 0.014, 0.014, 0.014, 0.014,
								0.014, 0.008, 0.008, 1.2, 1.0, 1.0 };
					}
					else if (numberOfHoles == 6)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.0203, 0.0203, 0.002, 0.003, 0.003,
								0.003, 0.003, 0.003, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.7, 0.50, 0.032, 0.032,
								0.07, 0.032, 0.032, 0.0102, 0.0102, 0.010,
								0.010, 0.010, 0.012, 1.15, 1.0, 1.0 };
					}
				}
				break;
			case TAPER_GROUP_OPT_SUB_CATEGORY_ID:
				// Length bounds are expressed in meters, diameter bounds as
				// ratios,
				// taper bounds as ratios.
				if (numberOfHoles == 0)
				{
					holeGroups = new int[][] { {} };
					if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
					{
						lowerBound = new double[] { 0.2, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.7, 1.2, 1.0, 1.0 };
					}
				}
				else if (numberOfHoles == 7)
				{
					holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
					if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.0005, 0.002, 0.002, 0.002, 0.002,
								0.002, 0.002, 0.002, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.7, 1.0, 0.05, 0.05, 0.1,
								0.003, 0.014, 0.014, 0.014, 0.014, 0.014,
								0.008, 0.008, 1.2, 1.0, 1.0 };
					}
				}
				else if (numberOfHoles == 6)
				{
					holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
					if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
					{
						lowerBound = new double[] { 0.2, 0.25, 0.0203, 0.0203,
								0.0203, 0.002, 0.003, 0.003, 0.003, 0.003,
								0.003, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.7, 1.0, 0.032, 0.07,
								0.032, 0.0102, 0.0102, 0.010, 0.010, 0.010,
								0.012, 1.2, 1.0, 1.0 };
					}
				}
				objective = new SingleTaperHoleGroupFromTopObjectiveFunction(
						calculator, tuning, evaluator, holeGroups);
				break;
		}

		if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
		{
			objective.setLowerBounds(lowerBound);
			objective.setUpperBounds(upperBound);
		}
		else if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
		{
			Constraints constraints = getConstraints();
			objective.setLowerBounds(constraints.getLowerBounds());
			objective.setUpperBounds(constraints.getUpperBounds());
			Category multiStartCategory = getCategory(MULTI_START_CATEGORY_ID);
			String multiStartSelected = multiStartCategory.getSelectedSub();
			if (multiStartSelected == VARY_FIRST_MULTI_START_SUB_CATEGORY_ID)
			{
				GridRangeProcessor rangeProcessor = new GridRangeProcessor(
						objective.getLowerBounds(), objective.getUpperBounds(),
						new int[] { 0 }, 30);
				objective.setRangeProcessor(rangeProcessor);
				objective.setMaxEvaluations(30 * objective.getMaxEvaluations());
			}
			else if (multiStartSelected == VARY_ALL_MULTI_START_SUB_CATEGORY_ID)
			{
				GridRangeProcessor rangeProcessor = new GridRangeProcessor(
						objective.getLowerBounds(), objective.getUpperBounds(),
						null, 30);
				objective.setRangeProcessor(rangeProcessor);
				objective.setMaxEvaluations(30 * objective.getMaxEvaluations());
			}
		}

		return objective;
	} // getObjectiveFunction

	@Override
	protected Class<? extends ContainedXmlView> getDefaultViewClass(
			String categoryName)
	{
		Map<String, Class<? extends ContainedXmlView>> defaultMap = new HashMap<String, Class<? extends ContainedXmlView>>();

		defaultMap.put(INSTRUMENT_CATEGORY_ID, ContainedNafView.class);
		defaultMap.put(TUNING_CATEGORY_ID, ContainedNafTuningView.class);
		defaultMap.put(CONSTRAINTS_CATEGORY_ID, ConstraintsEditorView.class);

		Class<? extends ContainedXmlView> defaultClass = defaultMap
				.get(categoryName);
		defaultClass = defaultClass == null ? ContainedXmlTextView.class
				: defaultClass;

		return defaultClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Map<String, Class<ContainedXmlView>[]> getToggleViewClasses()
	{
		Map<String, Class<ContainedXmlView>[]> toggleLists = new HashMap<String, Class<ContainedXmlView>[]>();

		toggleLists.put(CONSTRAINTS_CATEGORY_ID, new Class[] {
				ContainedXmlTextView.class, ConstraintsEditorView.class });
		toggleLists.put(TUNING_CATEGORY_ID, new Class[] {
				ContainedNafTuningView.class, ContainedXmlTextView.class });
		toggleLists.put(INSTRUMENT_CATEGORY_ID, new Class[] {
				ContainedNafView.class, ContainedXmlTextView.class });

		return toggleLists;
	}

	@Override
	public boolean addDataModel(FileDataModel dataModel) throws Exception
	{
		// Process Instrument and Tuning
		if (super.addDataModel(dataModel))
		{
			return true;
		}

		// Process Constraints. May move to super.
		String data = (String) dataModel.getData().toString();
		if (data == null || data.length() == 0)
		{
			return false;
		}
		Constraints constraints = getConstraints(data);
		if (constraints == null)
		{
			throw new DataOpenException("Data are not valid constraints",
					DataOpenException.INVALID_CONSTRAINTS);
		}

		// Check that constraints is for a represented optimizer
		String objFuncDisplayName = constraints.getObjectiveDisplayName();
		if (!isValidSubCategory(OPTIMIZER_CATEGORY_ID, objFuncDisplayName, true))
		{
			throw new DataOpenException("Required optimizer, "
					+ objFuncDisplayName + ", is not supported",
					DataOpenException.OPTIMIZER_NOT_SUPPORTED);
		}

		// Make constraintsByOptimizer key
		int numberOfHoles = constraints.getNumberOfHoles();
		String mapKey = makeMappedConstraintsKey(
				constraints.getObjectiveFunctionName(), false, numberOfHoles);

		Category mappedCategory = getMappedCategory(mapKey);

		String constraintsName = dataModel.getName();
		mappedCategory.addSub(constraintsName, dataModel);
		mappedCategory.setSelectedSub(constraintsName);

		Integer dataNumberOfHoles = getNumberOfHolesFromInstrument();
		// If there is no selected Instrument, or it has the
		// same number of holes as the contraints, show the constraints in the
		// studyView and select the optimizer that matches the constraints.
		if (dataNumberOfHoles == null || dataNumberOfHoles == numberOfHoles)
		{
			replaceCategory(CONSTRAINTS_CATEGORY_ID, mappedCategory);
			setCategorySelection(OPTIMIZER_CATEGORY_ID, objFuncDisplayName);
			return true;
		}

		// If the holes don't match, display a warning and reset nothing.
		throw new DataOpenException(
				"Number of holes in constraints set does not match the other data.",
				DataOpenException.CONSTRAINTS_NOT_SHOWN);
	}

	/**
	 * Returns a mapped Constraints Category
	 * 
	 * @param mapKey
	 *            The key to this Category
	 * @return The found Category, or a new empty one
	 */
	protected Category getMappedCategory(String mapKey)
	{
		// Add constraints to category in map, making category if necessary
		Category mappedCategory = constraintsByOptimizer.get(mapKey);
		if (mappedCategory == null)
		{
			mappedCategory = new Category(CONSTRAINTS_CATEGORY_ID);
			constraintsByOptimizer.put(mapKey, mappedCategory);
		}
		return mappedCategory;
	}

	@Override
	public boolean removeDataModel(FileDataModel dataModel)
	{
		super.removeDataModel(dataModel);

		// Remove cached subCategories by dataModel, not name
		for (Category category : constraintsByOptimizer.values())
		{
			category.removeSubByValue(dataModel);
		}

		return true;
	}

	@Override
	public boolean replaceDataModel(FileDataModel dataModel)
			throws DataOpenException
	{
		if (super.replaceDataModel(dataModel))
		{
			return true;
		}

		// Replace in all the cached Categories
		for (Category category : constraintsByOptimizer.values())
		{
			if (category.replaceSub(dataModel.getName(), dataModel))
			{
				category.setSelectedSub(dataModel.getName());
				return true;
			}
		}
		return false;
	}

}
