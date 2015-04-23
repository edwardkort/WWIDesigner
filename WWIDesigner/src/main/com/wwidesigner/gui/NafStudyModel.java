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

import java.awt.Frame;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.view.HoleGroupSpacingDialog;
import com.wwidesigner.geometry.view.InstrumentComparisonTable;
import com.wwidesigner.geometry.view.NafInstrumentComparisonTable;
import com.wwidesigner.gui.util.DataOpenException;
import com.wwidesigner.modelling.CentDeviationEvaluator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReflectionEvaluator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.FippleFactorObjectiveFunction;
import com.wwidesigner.optimization.HoleFromTopObjectiveFunction;
import com.wwidesigner.optimization.HoleGroupFromTopObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeObjectiveFunction;
import com.wwidesigner.optimization.NafHoleSizeObjectiveFunction;
import com.wwidesigner.optimization.SingleTaperHoleGroupFromTopObjectiveFunction;
import com.wwidesigner.optimization.SingleTaperNoHoleGroupingFromTopObjectiveFunction;
import com.wwidesigner.optimization.multistart.GridRangeProcessor;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class NafStudyModel extends StudyModel
{
	public static final String FIPPLE_OPT_SUB_CATEGORY_ID = FippleFactorObjectiveFunction.DISPLAY_NAME;
	public static final String FIPPLE_OPT_TOOL_TIP = "Determine the parameter that defines the mouthpiece geometry's effect on tuning";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = HoleSizeObjectiveFunction.DISPLAY_NAME;
	public static final String HOLESIZE_OPT_TOOL_TIP = "Determine the hole sizes that minimize tuning error";
	public static final String NO_GROUP_OPT_SUB_CATEGORY_ID = HoleFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String NO_GROUP_OPT_TOOL_TIP = "Determine the hole sizes and positions, and bore length, that minimize tuning error";
	public static final String GROUP_OPT_SUB_CATEGORY_ID = HoleGroupFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String GROUP_OPT_TOOL_TIP = "<html>Determine the hole sizes and positions, and bore length, that minimize tuning error.<br/>"
			+ "Supports grouping sets of adjacent holes to have the same inter-hole spacing</html>";
	public static final String TAPER_GROUP_OPT_SUB_CATEGORY_ID = SingleTaperHoleGroupFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String TAPER_GROUP_TOOL_TIP = "<html>Determine the hole sizes and positions, and bore length, that minimize tuning error.<br/>"
			+ "Supports grouping sets of adjacent holes to have the same inter-hole spacing.<br/>"
			+ "Creates a linear taper in the bore, with constrained start, end, and bore-diameter<br/>"
			+ "changes. The diameter at the bore end is not changed</html>";
	public static final String TAPER_NO_GROUP_OPT_SUB_CATEGORY_ID = SingleTaperNoHoleGroupingFromTopObjectiveFunction.DISPLAY_NAME;
	public static final String TAPER_NO_GROUP_TOOL_TIP = "<html>Determine the hole sizes and positions, and bore length, that minimize tuning error.<br/>"
			+ "Creates a linear taper in the bore, with constrained start, end, and bore-diameter<br/>"
			+ "changes. The diameter at the bore end is not changed</html>";

	public static final String HOLE_0_CONS_SUB_CATEGORY_ID = "0 holes";
	public static final String HOLE_6_1_125_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/8\" max spacing";
	public static final String HOLE_6_1_25_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/4\" max spacing";
	public static final String HOLE_6_40_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1.40\" max spacing";
	public static final String HOLE_6_1_5_SPACING_CONS_SUB_CATEGORY_ID = "6 holes, 1-1/2\" max spacing";
	public static final String HOLE_7_CONS_SUB_CATEGORY_ID = "7 holes";

	public static final String NO_MULTI_START_SUB_CATEGORY_ID = "No multi-start optimization";
	public static final String NO_MULTI_START_TOOL_TIP = "Run optimizer once";
	public static final String VARY_FIRST_MULTI_START_SUB_CATEGORY_ID = "Vary first bound variable";
	public static final String VARY_FIRST_MULTI_START_TOOL_TIP = "Run optimizer multiple times, starting with different bore lengths (typically)";
	public static final String VARY_ALL_MULTI_START_SUB_CATEGORY_ID = "Vary all dimensions";
	public static final String VARY_ALL_MULTI_START_TOOL_TIP = "Run optimizer multiple times, starting with different values for all parameters";

	protected int numberOfStarts = 30;

	// Key is a concatenation of the ObjectiveFunction and the number of holes.
	// Values is a Category containing the list of Constraints (as XML) for the
	// key.
	protected Map<String, Category> constraintsByOptimizer = new HashMap<String, Category>();
	protected Frame parentFrame; // for displaying dialogs not appropriate to
									// pass to the StudyView.

	public NafStudyModel(Frame parentFrame)
	{
		super();
		this.parentFrame = parentFrame;
		setLocalCategories();
	}

	protected void setLocalCategories()
	{
		setParams(new PhysicalParameters(72.0, TemperatureType.F));
		Category multiStart = new Category(MULTI_START_CATEGORY_ID);
		multiStart.addSub(NO_MULTI_START_SUB_CATEGORY_ID, null,
				NO_MULTI_START_TOOL_TIP);
		multiStart.addSub(VARY_FIRST_MULTI_START_SUB_CATEGORY_ID, null,
				VARY_FIRST_MULTI_START_TOOL_TIP);
		multiStart.addSub(VARY_ALL_MULTI_START_SUB_CATEGORY_ID, null,
				VARY_ALL_MULTI_START_TOOL_TIP);
		// Default to no multi-start
		multiStart.setSelectedSub(NO_MULTI_START_SUB_CATEGORY_ID);
		categories.add(multiStart);
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(FIPPLE_OPT_SUB_CATEGORY_ID,
				FippleFactorObjectiveFunction.NAME, FIPPLE_OPT_TOOL_TIP);
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID,
				NafHoleSizeObjectiveFunction.NAME, HOLESIZE_OPT_TOOL_TIP);
		optimizers.addSub(NO_GROUP_OPT_SUB_CATEGORY_ID,
				HoleFromTopObjectiveFunction.NAME, NO_GROUP_OPT_TOOL_TIP);
		optimizers.addSub(GROUP_OPT_SUB_CATEGORY_ID,
				HoleGroupFromTopObjectiveFunction.NAME, GROUP_OPT_TOOL_TIP);
		optimizers.addSub(TAPER_NO_GROUP_OPT_SUB_CATEGORY_ID,
				SingleTaperNoHoleGroupingFromTopObjectiveFunction.NAME,
				TAPER_NO_GROUP_TOOL_TIP);
		optimizers.addSub(TAPER_GROUP_OPT_SUB_CATEGORY_ID,
				SingleTaperHoleGroupFromTopObjectiveFunction.NAME,
				TAPER_GROUP_TOOL_TIP);
		categories.add(optimizers);
		Category constraints = new Category(CONSTRAINTS_CATEGORY_ID);
		categories.add(constraints);
	}

	@Override
	public boolean canOptimize()
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
	 * Ignores preferences for pressure, co2, and optimizer type, using the
	 * defaults instead.
	 */
	@Override
	public void setPreferences(Preferences newPreferences)
	{
		double currentTemperature = newPreferences.getDouble(
				OptimizationPreferences.TEMPERATURE_OPT,
				OptimizationPreferences.DEFAULT_TEMPERATURE);
		double currentPressure = OptimizationPreferences.DEFAULT_PRESSURE;
		int currentHumidity = newPreferences.getInt(
				OptimizationPreferences.HUMIDITY_OPT,
				OptimizationPreferences.DEFAULT_HUMIDITY);
		int currentCO2 = OptimizationPreferences.DEFAULT_CO2_FRACTION;
		double xCO2 = currentCO2 * 1.0e-6;
		getParams().setProperties(currentTemperature, currentPressure,
				currentHumidity, xCO2);
		getParams().printProperties();

		preferredOptimizerType = null;
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
		InstrumentCalculator calculator = new NAFCalculator();
		calculator.setPhysicalParameters(params);

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
		int numberOfHoles = instrument.getHole().size();

		BaseObjectiveFunction objective = null;
		double[] lowerBound = null;
		double[] upperBound = null;
		int[][] holeGroups = null;
		Constraints constraints = null;
		if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
		{
			constraints = getConstraints();
		}

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
				objective = new NafHoleSizeObjectiveFunction(calculator,
						tuning, evaluator);
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
						lowerBound = new double[] { 0.002032, 0.003175,
								0.003175, 0.003175, 0.003175, 0.002032,
								0.002032 };
						upperBound = new double[] { 0.0127, 0.0127, 0.0127,
								0.0127, 0.0127, 0.00635, 0.00635 };
					}
					else if (numberOfHoles == 6)
					{
						lowerBound = new double[] { 0.002032, 0.003175,
								0.003175, 0.003175, 0.003175, 0.003175 };
						upperBound = new double[] { 0.0127, 0.0127, 0.0127,
								0.0127, 0.0127, 0.0127 };
					}
					else
					// Create blank constraints for the no-default scenario
					{
						objectiveFunctionIntent = BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT;
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
						lowerBound = new double[] { 0.1905 };
						upperBound = new double[] { 0.6985 };
					}
					else if (numberOfHoles == 7)
					{
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.02032, 0.02032, 0.0,
								0.002032, 0.003175, 0.003175, 0.003175,
								0.003175, 0.002032, 0.002032 };
						upperBound = new double[] { 0.6985, 0.50, 0.03175,
								0.03175, 0.0762, 0.03175, 0.03175, 0.003175,
								0.0127, 0.0127, 0.0127, 0.0127, 0.0127,
								0.00635, 0.00635 };
					}
					else if (numberOfHoles == 6)
					{
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.02032, 0.02032, 0.002032,
								0.003175, 0.003175, 0.003175, 0.003175,
								0.003175 };
						upperBound = new double[] { 0.6985, 0.50, 0.03175,
								0.03175, 0.0762, 0.03175, 0.03175, 0.0127,
								0.0127, 0.0127, 0.0127, 0.0127, 0.0127 };
					}
					else
					// Create blank constraints for the no-default scenario
					{
						objectiveFunctionIntent = BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT;
					}
				}
				break;
			case GROUP_OPT_SUB_CATEGORY_ID:
				// Length bounds are expressed in meters, diameter bounds as
				// ratios.
				if (objectiveFunctionIntent == BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT)
				{
					if (numberOfHoles == 0)
					{
						holeGroups = new int[][] { {} };
					}
					else
					{
						holeGroups = getUserHoleGroups(numberOfHoles);
						if (holeGroups == null)
						{
							return null;
						}
					}
				}
				else if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
				{
					holeGroups = constraints.getHoleGroupsArray();
				}
				else if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
				{
					if (numberOfHoles == 0)
					{
						holeGroups = new int[][] { {} };
						lowerBound = new double[] { 0.1905 };
						upperBound = new double[] { 0.6985 };
					}
					else if (numberOfHoles == 7)
					{
						holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 },
								{ 6 } };
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.0, 0.002032, 0.003175,
								0.003175, 0.003175, 0.003175, 0.002032,
								0.002032 };
						upperBound = new double[] { 0.6985, 0.5, 0.03175,
								0.0762, 0.03175, 0.003175, 0.0127, 0.0127,
								0.0127, 0.0127, 0.0127, 0.00635, 0.00635 };
					}
					else if (numberOfHoles == 6)
					{
						holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.002032, 0.003175, 0.003175,
								0.003175, 0.003175, 0.003175 };
						upperBound = new double[] { 0.6985, 0.5, 0.03175,
								0.0762, 0.03175, 0.0127, 0.0127, 0.0127,
								0.0127, 0.0127, 0.0127 };
					}
					else
					// Create blank constraints for the no-default scenario
					{
						objectiveFunctionIntent = BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT;
						holeGroups = getUserHoleGroups(numberOfHoles);
						if (holeGroups == null)
						{
							return null;
						}
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
						lowerBound = new double[] { 0.1905, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.6985, 1.2, 1.0, 1.0 };
					}
					else if (numberOfHoles == 7)
					{
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.02032, 0.02032, 0.0,
								0.002032, 0.003175, 0.003175, 0.003175,
								0.003175, 0.002032, 0.002032, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.6985, 0.50, 0.03175,
								0.03175, 0.0762, 0.03175, 0.03175, 0.003175,
								0.0127, 0.0127, 0.0127, 0.0127, 0.0127,
								0.00635, 0.00635, 1.2, 1.0, 1.0 };
					}
					else if (numberOfHoles == 6)
					{
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.02032, 0.02032, 0.002032,
								0.003175, 0.003175, 0.003175, 0.003175,
								0.003175, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.6985, 0.50, 0.03175,
								0.03175, 0.0762, 0.03175, 0.03175, 0.0127,
								0.0127, 0.0127, 0.0127, 0.0127, 0.0127, 1.2,
								1.0, 1.0 };
					}
					else
					// Create blank constraints for the no-default scenario
					{
						objectiveFunctionIntent = BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT;
					}
				}
				break;
			case TAPER_GROUP_OPT_SUB_CATEGORY_ID:
				// Length bounds are expressed in meters, diameter bounds as
				// ratios,
				// taper bounds as ratios.
				if (objectiveFunctionIntent == BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT)
				{
					if (numberOfHoles == 0)
					{
						holeGroups = new int[][] { {} };
					}
					else
					{
						holeGroups = getUserHoleGroups(numberOfHoles);
						if (holeGroups == null)
						{
							return null;
						}
					}
				}
				else if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
				{
					holeGroups = constraints.getHoleGroupsArray();
				}
				else if (objectiveFunctionIntent == BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT)
				{
					if (numberOfHoles == 0)
					{
						holeGroups = new int[][] { {} };
						lowerBound = new double[] { 0.1905, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.6985, 1.2, 1.0, 1.0 };
					}
					else if (numberOfHoles == 7)
					{
						holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 },
								{ 6 } };
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.0, 0.002032, 0.003175,
								0.003175, 0.003175, 0.003175, 0.002032,
								0.002032, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.6985, 0.5, 0.03175,
								0.0762, 0.03175, 0.003175, 0.0127, 0.0127,
								0.0127, 0.0127, 0.0127, 0.00635, 0.00635, 1.2,
								1.0, 1.0 };
					}
					else if (numberOfHoles == 6)
					{
						holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 } };
						lowerBound = new double[] { 0.1905, 0.25, 0.02032,
								0.02032, 0.02032, 0.002032, 0.003175, 0.003175,
								0.003175, 0.003175, 0.003175, 0.8, 0.0, 0.0 };
						upperBound = new double[] { 0.6985, 0.5, 0.03175,
								0.0762, 0.03175, 0.0127, 0.0127, 0.0127,
								0.0127, 0.0127, 0.0127, 1.2, 1.0, 1.0 };
					}
					else
					// Create blank constraints for the no-default scenario
					{
						objectiveFunctionIntent = BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT;
						holeGroups = getUserHoleGroups(numberOfHoles);
						if (holeGroups == null)
						{
							return null;
						}
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
			objective.setConstraintsBounds(constraints);
			objective.setRunTwoStageOptimization(true);
			objective
					.setFirstStageEvaluator(new ReflectionEvaluator(calculator));
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

	protected int[][] getUserHoleGroups(int numberOfHoles)
	{
		HoleGroupSpacingDialog spacingDialog = new HoleGroupSpacingDialog(
				parentFrame, numberOfHoles);
		spacingDialog.pack();
		spacingDialog.setVisible(true);
		if (spacingDialog.getDialogResult() == HoleGroupSpacingDialog.RESULT_AFFIRMED)
		{
			return spacingDialog.getHoleSpacingGroups();
		}
		else
		{
			return null;
		}
	}

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
	public boolean addDataModel(FileDataModel dataModel, boolean isNew)
			throws Exception
	{
		// When invoked by an Activity with a new dataModel, this methods is
		// called twice, first with a bogus "Untitled" dataModel name, then with
		// the correctly sequenced dataModel.
		// This if statement short circuits the bogus call.
		if ("Untitled".equals(dataModel.getName()))
		{
			return false;
		}

		// Process Instrument and Tuning
		if (super.addDataModel(dataModel, isNew))
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

	@Override
	protected InstrumentComparisonTable getInstrumentComparisonTable(
			String title, LengthType defaultLengthType)
	{
		return new NafInstrumentComparisonTable(title, defaultLengthType);
	}

}
