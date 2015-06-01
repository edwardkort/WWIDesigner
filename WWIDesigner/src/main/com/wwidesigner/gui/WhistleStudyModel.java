/**
 * Study model class to analyze and optimize whistles and other fipple flutes.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.gui.util.DataOpenException;
import com.wwidesigner.modelling.BellNoteEvaluator;
import com.wwidesigner.modelling.CentDeviationEvaluator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.FmaxEvaluator;
import com.wwidesigner.modelling.FminEvaluator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.LinearVInstrumentTuner;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.BasicTaperObjectiveFunction;
import com.wwidesigner.optimization.BetaObjectiveFunction;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.HoleAndTaperObjectiveFunction;
import com.wwidesigner.optimization.HoleObjectiveFunction;
import com.wwidesigner.optimization.HolePositionObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeObjectiveFunction;
import com.wwidesigner.optimization.LengthObjectiveFunction;
import com.wwidesigner.optimization.WindowHeightObjectiveFunction;
import com.wwidesigner.optimization.bind.OptimizationBindFactory;
import com.wwidesigner.optimization.multistart.GridRangeProcessor;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Class to encapsulate methods for analyzing and optimizing whistle models.
 * 
 * @author Burton Patkau
 * 
 */
public class WhistleStudyModel extends StudyModel
{
	// Named constants for the standard set of optimizers.
	public static final String WINDOW_OPT_SUB_CATEGORY_ID = "1. Window Height Calibrator";
	public static final String BETA_OPT_SUB_CATEGORY_ID = "2. Beta Calibrator";
	public static final String LENGTH_OPT_SUB_CATEGORY_ID = "3. Length Optimizer";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = "4. Hole Size Optimizer";
	public static final String HOLESPACE_OPT_SUB_CATEGORY_ID = "5. Hole Spacing Optimizer";
	public static final String HOLE_OPT_SUB_CATEGORY_ID = "6. Hole Size+Spacing Optimizer";
	public static final String TAPER_OPT_SUB_CATEGORY_ID = "7. Taper Optimizer";
	public static final String HOLE_TAPER_OPT_SUB_CATEGORY_ID = "8. Hole and Taper Optimizer";
	public static final String ROUGH_CUT_OPT_SUB_CATEGORY_ID = "9. Rough-Cut Optimizer";

	// Default minimum hole diameter, in meters.
	public static final double MIN_HOLE_DIAMETER = 0.0040;
	// Default maximum hole diameter, in meters.
	public static final double MAX_HOLE_DIAMETER = 0.0091;

	protected int blowingLevel;

	// Map names in the optimizer category to objective function class names.
	protected Map<String, String> objectiveFunctionNames = new HashMap<String, String>();

	// Display name for this study
	private static final String DISPLAY_NAME = "Whistle Study";

	public WhistleStudyModel()
	{
		super();
		setLocalCategories();
		setBlowingLevel(5);
	}

	public String getDisplayName()
	{
		return DISPLAY_NAME;
	}

	protected void setLocalCategories()
	{
		setParams(new PhysicalParameters(27, TemperatureType.C, 98.4, 100, 0.04));

		// Add the standard set of optimizers to the Optimizer category,
		// and assign an associated objective function name to each one.
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(WINDOW_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(WINDOW_OPT_SUB_CATEGORY_ID,
				WindowHeightObjectiveFunction.class.getSimpleName());
		optimizers.addSub(BETA_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(BETA_OPT_SUB_CATEGORY_ID,
				BetaObjectiveFunction.class.getSimpleName());
		optimizers.addSub(LENGTH_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(LENGTH_OPT_SUB_CATEGORY_ID,
				LengthObjectiveFunction.class.getSimpleName());
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(HOLESIZE_OPT_SUB_CATEGORY_ID,
				HoleSizeObjectiveFunction.class.getSimpleName());
		optimizers.addSub(HOLESPACE_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(HOLESPACE_OPT_SUB_CATEGORY_ID,
				HolePositionObjectiveFunction.class.getSimpleName());
		optimizers.addSub(HOLE_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(HOLE_OPT_SUB_CATEGORY_ID,
				HoleObjectiveFunction.class.getSimpleName());
		optimizers.addSub(TAPER_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(TAPER_OPT_SUB_CATEGORY_ID,
				BasicTaperObjectiveFunction.class.getSimpleName());
		optimizers.addSub(HOLE_TAPER_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(HOLE_TAPER_OPT_SUB_CATEGORY_ID,
				HoleAndTaperObjectiveFunction.class.getSimpleName());
		optimizers.addSub(ROUGH_CUT_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(ROUGH_CUT_OPT_SUB_CATEGORY_ID,
				HolePositionObjectiveFunction.class.getSimpleName());
		categories.add(optimizers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.gui.StudyModel#setPreferences(java.util.prefs.Preferences
	 * )
	 */
	@Override
	public void setPreferences(Preferences newPreferences)
	{
		setBlowingLevel(newPreferences.getInt(
				OptimizationPreferences.BLOWING_LEVEL_OPT, 5));
		super.setPreferences(newPreferences);
	}

	public void setBlowingLevel(int blowingLevel)
	{
		this.blowingLevel = blowingLevel;
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
		InstrumentTuner tuner = new LinearVInstrumentTuner(blowingLevel);
		tuner.setParams(params);
		return tuner;
	}

	@Override
	protected Constraints getConstraints() throws Exception
	{
		Category category = getCategory(OPTIMIZER_CATEGORY_ID);
		Object selected = category.getSelectedSubValue();
		if (selected != null)
		{
			BindFactory constraintsBindFactory = OptimizationBindFactory
					.getInstance();
			String xmlString = getSelectedXmlString(OPTIMIZER_CATEGORY_ID);
			Constraints constraints = (Constraints) constraintsBindFactory
					.unmarshalXml(xmlString, true);
			constraints.setConstraintParent();
			return constraints;
		}

		return null;
	}

	@Override
	protected BaseObjectiveFunction getObjectiveFunction(
			int objectiveFunctionIntent) throws Exception
	{
		Instrument instrument = getInstrument();
		Tuning tuning = getTuning();
		if (tuning == null
				&& objectiveFunctionIntent != BaseObjectiveFunction.OPTIMIZATION_INTENT)
		{
			// Tuning is not required for creating constraints.
			tuning = new Tuning();
		}
		WhistleCalculator calculator = new WhistleCalculator();
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);

		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();
		String objectiveFunctionClass = objectiveFunctionNames.get(optimizer);
		int numberOfHoles = instrument.getHole().size();

		EvaluatorInterface evaluator;
		BaseObjectiveFunction objective = null;
		double[] lowerBound = null;
		double[] upperBound = null;

		// From the objective function class, create the objective function,
		// evaluator, and default bounds. The default bounds may be discarded
		// below.
		switch (objectiveFunctionClass)
		{
			case "WindowHeightObjectiveFunction":
				evaluator = new FmaxEvaluator(calculator, getInstrumentTuner());
				objective = new WindowHeightObjectiveFunction(calculator,
						tuning, evaluator);
				lowerBound = new double[] { 0.000 };
				upperBound = new double[] { 0.010 };
				break;

			case "BetaObjectiveFunction":
				evaluator = new FminEvaluator(calculator, getInstrumentTuner());
				objective = new BetaObjectiveFunction(calculator, tuning,
						evaluator);
				lowerBound = new double[] { 0.2 };
				upperBound = new double[] { 1.0 };
				break;

			case "LengthObjectiveFunction":
				evaluator = new BellNoteEvaluator(calculator);
				objective = new LengthObjectiveFunction(calculator, tuning,
						evaluator);
				lowerBound = new double[] { 0.200 };
				upperBound = new double[] { 0.700 };
				break;

			case "HoleSizeObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HoleSizeObjectiveFunction(calculator, tuning,
						evaluator);
				// Bounds are diameters, expressed in meters.
				lowerBound = new double[numberOfHoles];
				upperBound = new double[numberOfHoles];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				break;

			case "HolePositionObjectiveFunction":
				if (optimizer == ROUGH_CUT_OPT_SUB_CATEGORY_ID)
				{
					// For a rough-cut optimization, optimize reactance.
					// Although this is inaccurate, since it seeks fmax rather
					// than nominal playing frequency, it is much faster.
					evaluator = new ReactanceEvaluator(calculator);
				}
				else
				{
					evaluator = new CentDeviationEvaluator(calculator,
							getInstrumentTuner());
				}
				objective = new HolePositionObjectiveFunction(calculator,
						tuning, evaluator);
				// Bounds are hole separations, expressed in meters.
				lowerBound = new double[numberOfHoles + 1];
				upperBound = new double[numberOfHoles + 1];
				Arrays.fill(lowerBound, 0.012);
				lowerBound[0] = 0.200;
				Arrays.fill(upperBound, 0.040);
				upperBound[0] = 0.700;
				upperBound[numberOfHoles] = 0.200;
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands.
					upperBound[numberOfHoles - 3] = 0.100;
				}

				// For a rough-cut optimization, use multi-start optimization.

				if (optimizer == ROUGH_CUT_OPT_SUB_CATEGORY_ID)
				{
					int nrOfStarts = 4 * numberOfHoles;
					GridRangeProcessor rangeProcessor = new GridRangeProcessor(
							lowerBound, upperBound, null, nrOfStarts);
					objective.setRangeProcessor(rangeProcessor);
					objective.setMaxEvaluations(nrOfStarts
							* objective.getMaxEvaluations());
				}
				break;

			case "HoleObjectiveFunction":
			default:
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HoleObjectiveFunction(calculator, tuning,
						evaluator);
				// Separation and diameter bounds, expressed in meters.
				lowerBound = new double[2 * numberOfHoles + 1];
				upperBound = new double[2 * numberOfHoles + 1];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				// Bounds on hole spacing.
				lowerBound[0] = 0.200;
				upperBound[0] = 0.700;
				for (int gapNr = 1; gapNr < numberOfHoles; ++gapNr)
				{
					lowerBound[gapNr] = 0.012;
					upperBound[gapNr] = 0.040;
				}
				lowerBound[numberOfHoles] = 0.012;
				upperBound[numberOfHoles] = 0.200;
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands.
					upperBound[numberOfHoles - 3] = 0.100;
				}
				break;

			case "BasicTaperObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new BasicTaperObjectiveFunction(calculator, tuning,
						evaluator);
				// Dimensions are expressed as ratios: head length, foot
				// diameter.
				lowerBound = new double[] { 0.01, 0.3 };
				upperBound = new double[] { 0.5, 2.0 };
				break;

			case "HoleAndTaperObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HoleAndTaperObjectiveFunction(calculator,
						tuning, evaluator);
				// Separation bounds and diameter bounds, expressed in meters,
				// and two taper ratios.
				lowerBound = new double[2 * numberOfHoles + 3];
				upperBound = new double[2 * numberOfHoles + 3];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				// Bounds on hole spacing.
				lowerBound[0] = 0.200;
				upperBound[0] = 0.700;
				for (int gapNr = 1; gapNr < numberOfHoles; ++gapNr)
				{
					lowerBound[gapNr] = 0.012;
					upperBound[gapNr] = 0.040;
				}
				lowerBound[numberOfHoles] = 0.012;
				upperBound[numberOfHoles] = 0.200;
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands.
					upperBound[numberOfHoles - 3] = 0.100;
				}
				// Bounds on taper.
				lowerBound[lowerBound.length - 2] = 0.01;
				lowerBound[lowerBound.length - 1] = 0.3;
				upperBound[upperBound.length - 2] = 0.5;
				upperBound[upperBound.length - 1] = 2.0;
				break;
		}

		// Identify what bounds to use with the objective function.
		if (objectiveFunctionIntent == BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT)
		{
			// Ignore the default bounds, use 0..1.
			Arrays.fill(lowerBound, 0.0);
			Arrays.fill(upperBound, 1.0);
		}
		else if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
		{
			// If a constraints file is selected, and agrees with
			// the instrument, use its lower and upper bounds.
			Constraints constraints = getConstraints();
			if (constraints != null)
			{
				// Selected optimizer is associated with a constraints file.
				if (constraints.getLowerBounds().length == lowerBound.length)
				{
					lowerBound = constraints.getLowerBounds();
					upperBound = constraints.getUpperBounds();
				}
				else
				{
					System.out
							.println("Number of holes for specified constraints does not match number of holes for instrument.");
					System.out.println("Using default constraints.");
				}
			}
		}
		objective.setLowerBounds(lowerBound);
		objective.setUpperBounds(upperBound);

		return objective;
	}

	@Override
	protected void setDefaultViewClassMap()
	{
		defaultXmlViewMap = new HashMap<String, Class<? extends ContainedXmlView>>();

		defaultXmlViewMap.put(INSTRUMENT_CATEGORY_ID,
				ContainedWhistleInstrumentView.class);
		defaultXmlViewMap.put(TUNING_CATEGORY_ID, ContainedTuningView.class);
		defaultXmlViewMap.put(CONSTRAINTS_CATEGORY_ID,
				SizableConstraintsEditorView.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setToggleViewClassesMap()
	{
		toggleXmlViewLists = new HashMap<String, Class<ContainedXmlView>[]>();

		toggleXmlViewLists.put(INSTRUMENT_CATEGORY_ID, new Class[] {
				ContainedXmlTextView.class,
				ContainedWhistleInstrumentView.class });
		toggleXmlViewLists.put(TUNING_CATEGORY_ID, new Class[] {
				ContainedXmlTextView.class, ContainedTuningView.class });
		toggleXmlViewLists.put(CONSTRAINTS_CATEGORY_ID,
				new Class[] { ContainedXmlTextView.class,
						SizableConstraintsEditorView.class });
	}

	@Override
	public boolean addDataModel(FileDataModel dataModel, boolean isNew)
			throws Exception
	{
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

		// Check that constraints apply to a known optimizer.
		String objFuncClassName = constraints.getObjectiveFunctionName();
		if (!objectiveFunctionNames.containsValue(objFuncClassName))
		{
			throw new DataOpenException(
					"Whistle study model does not support required optimizer, "
							+ objFuncClassName + " ("
							+ constraints.getObjectiveDisplayName() + ")",
					DataOpenException.OPTIMIZER_NOT_SUPPORTED);
		}

		// Constraints are filed in the optimizer category.
		Category category = getCategory(OPTIMIZER_CATEGORY_ID);
		category.addSub(dataModel.getName(), dataModel);
		category.setSelectedSub(dataModel.getName());
		objectiveFunctionNames.put(dataModel.getName(), objFuncClassName);

		return true;
	}

	@Override
	public boolean removeDataModel(FileDataModel dataModel)
	{
		String data = (String) dataModel.getData();
		String categoryName = getCategoryName(data);
		if (categoryName != null
				&& categoryName.equals(CONSTRAINTS_CATEGORY_ID))
		{
			// Constraints are filed in the optimizer category.
			Category category = getCategory(OPTIMIZER_CATEGORY_ID);
			category.removeSubByValue(dataModel);
			return true;
		}
		return super.removeDataModel(dataModel);
	}

	@Override
	public boolean replaceDataModel(FileDataModel dataModel)
			throws DataOpenException
	{
		String data = (String) dataModel.getData();
		String categoryName = getCategoryName(data);
		if (categoryName == null)
		{
			removeDataModel(dataModel);
			throw new DataOpenException(
					"Data does not represent a supported type.",
					DataOpenException.DATA_TYPE_NOT_SUPPORTED);
		}
		if (categoryName.equals(CONSTRAINTS_CATEGORY_ID))
		{
			// Constraints are filed in the optimizer category.
			categoryName = OPTIMIZER_CATEGORY_ID;
			Constraints constraints = getConstraints(data);
			if (constraints == null)
			{
				throw new DataOpenException("Data are not valid constraints",
						DataOpenException.INVALID_CONSTRAINTS);
			}

			// Check that constraints apply to a known optimizer.
			String objFuncClassName = constraints.getObjectiveFunctionName();
			if (!objectiveFunctionNames.containsValue(objFuncClassName))
			{
				throw new DataOpenException(
						"Whistle study model does not support required optimizer, "
								+ objFuncClassName + " ("
								+ constraints.getObjectiveDisplayName() + ")",
						DataOpenException.OPTIMIZER_NOT_SUPPORTED);
			}
			objectiveFunctionNames.put(dataModel.getName(), objFuncClassName);
		}
		Category category = getCategory(categoryName);
		if (category.replaceSub(dataModel.getName(), dataModel))
		{
			category.setSelectedSub(dataModel.getName());
			return true;
		}
		return false;
	}

	@Override
	public File getConstraintsLeafDirectory(String rootDirectoryPath)
	{
		String studyModelName = getClass().getSimpleName();
		String optimizerSelected = getSelectedSub(OPTIMIZER_CATEGORY_ID);
		String objectiveFunctionName = objectiveFunctionNames
				.get(optimizerSelected);
		File leaf = makeConstraintsDirectoryPath(rootDirectoryPath,
				studyModelName, objectiveFunctionName, 0);
		return leaf;
	}

	@Override
	public File getConstraintsLeafDirectory(String rootDirectoryPath,
			Constraints constraints)
	{
		String studyModelName = getClass().getSimpleName();
		String objectiveFunctionName = constraints.getObjectiveFunctionName();
		File leaf = makeConstraintsDirectoryPath(rootDirectoryPath,
				studyModelName, objectiveFunctionName, 0);
		return leaf;
	}

	protected File makeConstraintsDirectoryPath(String rootPath,
			String studyModelName, String objectiveFunctionName,
			int numberOfHoles)
	{
		// numberOfHoles does not affect directory name.
		String path = rootPath + File.separator + studyModelName
				+ File.separator + objectiveFunctionName;
		File leaf = new File(path);
		if (!leaf.exists())
		{
			leaf.mkdirs();
		}
		return leaf;
	}

}
