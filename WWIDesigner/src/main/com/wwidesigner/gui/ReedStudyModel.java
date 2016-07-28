/**
 * Study model class to analyze and optimize reed instruments.
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
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.modelling.SimpleReedCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.BoreDiameterObjectiveFunction;
import com.wwidesigner.optimization.HoleAndBoreDiameterObjectiveFunction;
import com.wwidesigner.optimization.ReedCalibratorObjectiveFunction;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.ConicalBoreObjectiveFunction;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.HoleAndConicalBoreObjectiveFunction;
import com.wwidesigner.optimization.HoleObjectiveFunction;
import com.wwidesigner.optimization.HolePositionObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeObjectiveFunction;
import com.wwidesigner.optimization.LengthObjectiveFunction;
import com.wwidesigner.optimization.HolePositionObjectiveFunction.BoreLengthAdjustmentType;
import com.wwidesigner.optimization.bind.OptimizationBindFactory;
import com.wwidesigner.optimization.multistart.GridRangeProcessor;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Class to encapsulate methods for analyzing and optimizing reed instrument models.
 * 
 * @author Burton Patkau
 * 
 */
public class ReedStudyModel extends StudyModel
{
	// Named constants for the standard set of optimizers.
	public static final String CALIBRATOR_SUB_CATEGORY_ID = "1. Reed Calibrator";
	// Reed calibrator optimizes both alpha and beta.
	public static final String LENGTH_OPT_SUB_CATEGORY_ID = "2. Length Optimizer";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = "3. Hole Size Optimizer";
	public static final String HOLESPACE_OPT_SUB_CATEGORY_ID = "4. Hole Spacing Optimizer";
	public static final String HOLE_OPT_SUB_CATEGORY_ID = "5. Hole Size+Spacing Optimizer";
	public static final String CONE_OPT_SUB_CATEGORY_ID = "6. Conical Bore Optimizer";
	public static final String HOLE_CONE_OPT_SUB_CATEGORY_ID = "7. Hole and Conical Bore Optimizer";
	public static final String BORE_DIAMETER_OPT_SUB_CATEGORY_ID = "8. Bore Diameter Optimizer";
	public static final String HOLE_BOREDIAMETER_OPT_SUB_CATEGORY_ID = "9. Hole and Bore Optimizer";
	public static final String ROUGH_CUT_OPT_SUB_CATEGORY_ID = "Rough-Cut Optimizer";

	// Default minimum bore diameter, in meters.
	public static final double MIN_BORE_LENGTH = 0.200;
	// Default maximum bore diameter, in meters.
	public static final double MAX_BORE_LENGTH = 1.000;

	// Default minimum hole diameter, in meters.
	public static final double MIN_HOLE_DIAMETER = 0.0032;
	// Default maximum hole diameter, in meters.
	public static final double MAX_HOLE_DIAMETER = 0.0091;

	// Default minimum bore diameter at bottom, in meters.
	public static final double MIN_BORE_DIAMETER = 0.0030;
	// Default maximum bore diameter at bottom, in meters.
	public static final double MAX_BORE_DIAMETER = 0.1000;

	protected int blowingLevel;

	// Map names in the optimizer category to objective function class names.
	protected Map<String, String> objectiveFunctionNames = new HashMap<String, String>();

	// Display name for this study
	private static final String DISPLAY_NAME = "Reed Study";

	public ReedStudyModel()
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
		optimizers.addSub(CALIBRATOR_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(CALIBRATOR_SUB_CATEGORY_ID,
				ReedCalibratorObjectiveFunction.class.getSimpleName());
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
		optimizers.addSub(CONE_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(CONE_OPT_SUB_CATEGORY_ID,
				ConicalBoreObjectiveFunction.class.getSimpleName());
		optimizers.addSub(HOLE_CONE_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(HOLE_CONE_OPT_SUB_CATEGORY_ID,
				HoleAndConicalBoreObjectiveFunction.class.getSimpleName());
		optimizers.addSub(BORE_DIAMETER_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(BORE_DIAMETER_OPT_SUB_CATEGORY_ID,
				BoreDiameterObjectiveFunction.class.getSimpleName());
		optimizers.addSub(HOLE_BOREDIAMETER_OPT_SUB_CATEGORY_ID, null);
		objectiveFunctionNames.put(HOLE_BOREDIAMETER_OPT_SUB_CATEGORY_ID,
				HoleAndBoreDiameterObjectiveFunction.class.getSimpleName());
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
		InstrumentCalculator calculator = new SimpleReedCalculator();
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
		Tuning tuning;
		if (objectiveFunctionIntent == BaseObjectiveFunction.OPTIMIZATION_INTENT)
		{
			tuning = getTuning();
		}
		else
		{
			// Tuning is not required for creating constraints, and may not be
			// selected.
			tuning = new Tuning();
		}
		InstrumentCalculator calculator = getCalculator();
		calculator.setInstrument(instrument);

		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();
		String objectiveFunctionClass = objectiveFunctionNames.get(optimizer);
		int numberOfHoles = instrument.getHole().size();
		int nrDimensions;

		EvaluatorInterface evaluator;
		BaseObjectiveFunction objective = null;
		double[] lowerBound = null;
		double[] upperBound = null;

		// From the objective function class, create the objective function,
		// evaluator, and default bounds. The default bounds may be discarded
		// below.
		switch (objectiveFunctionClass)
		{
			case "ReedCalibratorObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator, getInstrumentTuner());
				objective = new ReedCalibratorObjectiveFunction(calculator,
						tuning, evaluator);
				lowerBound = new double[] { 0.00, 0.00 };
				upperBound = new double[] { 10.0, 10.0 };
				break;

			case "LengthObjectiveFunction":
				evaluator = new BellNoteEvaluator(calculator);
				objective = new LengthObjectiveFunction(calculator, tuning,
						evaluator);
				lowerBound = new double[] { MIN_BORE_LENGTH };
				upperBound = new double[] { MAX_BORE_LENGTH };
				break;

			case "HoleSizeObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HoleSizeObjectiveFunction(calculator, tuning,
						evaluator);
				nrDimensions = objective.getNrDimensions();
				// Bounds are diameters, expressed in meters.
				lowerBound = new double[nrDimensions];
				upperBound = new double[nrDimensions];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				break;

			case "HolePositionObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HolePositionObjectiveFunction(calculator,
						tuning, evaluator, BoreLengthAdjustmentType.PRESERVE_BELL);
				nrDimensions = objective.getNrDimensions();
				// Bounds are overall length, and hole separations, expressed in meters.
				lowerBound = new double[nrDimensions];
				upperBound = new double[nrDimensions];
				Arrays.fill(lowerBound, 0.012);
				lowerBound[0] = MIN_BORE_LENGTH;
				Arrays.fill(upperBound, 0.040);
				upperBound[0] = MAX_BORE_LENGTH;
				if (numberOfHoles > 0)
				{
					upperBound[numberOfHoles] = 0.200;
				}
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands, assuming upper hand
					// uses same number or one more finger than lower hand.
					upperBound[(numberOfHoles+1)/2] = 0.100;
				}
				if (numberOfHoles >= 7)
				{
					// Assume top hole is a thumb hole.
					lowerBound[1] = 0.0005;
				}
				if (numberOfHoles == 10)
				{
					// Assume a thumb hole for the lower hand.
					lowerBound[6] = 0.0005;
				}

				// For a rough-cut optimization, use multi-start optimization.
				// Optimize reactance in the first stage. Although this is
				// inaccurate, since it seeks fmax rather than nominal playing
				// frequency, it is much faster.

				if (optimizer == ROUGH_CUT_OPT_SUB_CATEGORY_ID)
				{
					objective.setRunTwoStageOptimization(true);
					objective.setFirstStageEvaluator(
							new ReactanceEvaluator(calculator));
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
						evaluator, BoreLengthAdjustmentType.PRESERVE_BELL);
				nrDimensions = objective.getNrDimensions();
				// Separation and diameter bounds, expressed in meters.
				lowerBound = new double[nrDimensions];
				upperBound = new double[nrDimensions];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				// Bounds on hole spacing.
				lowerBound[0] = MIN_BORE_LENGTH;
				upperBound[0] = MAX_BORE_LENGTH;
				for (int gapNr = 1; gapNr < numberOfHoles; ++gapNr)
				{
					lowerBound[gapNr] = 0.012;
					upperBound[gapNr] = 0.040;
				}
				if (numberOfHoles > 0)
				{
					lowerBound[numberOfHoles] = 0.012;
					upperBound[numberOfHoles] = 0.200;
				}
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands, assuming upper hand
					// uses same number or one more finger than lower hand.
					upperBound[(numberOfHoles+1)/2] = 0.100;
				}
				if (numberOfHoles >= 7)
				{
					// Assume top hole is a thumb hole.
					lowerBound[1] = 0.0005;
				}
				if (numberOfHoles == 10)
				{
					// Assume a thumb hole for the lower hand.
					lowerBound[6] = 0.0005;
				}
				break;

			case "ConicalBoreObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new ConicalBoreObjectiveFunction(calculator, tuning,
						evaluator);
				// Diameter at foot, in meters.
				lowerBound = new double[] { MIN_BORE_DIAMETER };
				upperBound = new double[] { MAX_BORE_DIAMETER };
				break;

			case "HoleAndConicalBoreObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HoleAndConicalBoreObjectiveFunction(calculator,
						tuning, evaluator);
				nrDimensions = objective.getNrDimensions();
				// Separation bounds and diameter bounds, expressed in meters,
				// and bore diameter at foot, also in meters.
				lowerBound = new double[nrDimensions];
				upperBound = new double[nrDimensions];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				// Bounds on hole spacing.
				lowerBound[0] = MIN_BORE_LENGTH;
				upperBound[0] = MAX_BORE_LENGTH;
				for (int gapNr = 1; gapNr < numberOfHoles; ++gapNr)
				{
					lowerBound[gapNr] = 0.012;
					upperBound[gapNr] = 0.040;
				}
				if (numberOfHoles > 0)
				{
					lowerBound[numberOfHoles] = 0.012;
					upperBound[numberOfHoles] = 0.200;
				}
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands, assuming upper hand
					// uses same number or one more finger than lower hand.
					upperBound[(numberOfHoles+1)/2] = 0.100;
				}
				if (numberOfHoles >= 7)
				{
					// Assume top hole is a thumb hole.
					lowerBound[1] = 0.0005;
				}
				if (numberOfHoles == 10)
				{
					// Assume a thumb hole for the lower hand.
					lowerBound[6] = 0.0005;
				}
				// Bounds on conical bore.
				lowerBound[lowerBound.length - 1] = 0.002;
				upperBound[upperBound.length - 1] = 0.100;
				break;

			case "BoreDiameterObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new BoreDiameterObjectiveFunction(calculator, tuning,
						evaluator);
				nrDimensions = objective.getNrDimensions();
				// First bound is bottom bore diameter, expressed in meters.
				// Remaining bounds are diameter ratios.
				// Bore taper flares out toward bottom.
				lowerBound = new double[nrDimensions];
				upperBound = new double[nrDimensions];
				Arrays.fill(lowerBound, 1.0);
				Arrays.fill(upperBound, 5.0);
				lowerBound[0] = MIN_BORE_DIAMETER;
				upperBound[0] = MAX_BORE_DIAMETER;
				break;

			case "HoleAndBoreDiameterObjectiveFunction":
				evaluator = new CentDeviationEvaluator(calculator,
						getInstrumentTuner());
				objective = new HoleAndBoreDiameterObjectiveFunction(calculator,
						tuning, evaluator);
				nrDimensions = objective.getNrDimensions();
				// Separation bounds and diameter bounds, expressed in meters,
				// and bore diameter at foot, also in meters.
				lowerBound = new double[nrDimensions];
				upperBound = new double[nrDimensions];
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
				if (numberOfHoles > 0)
				{
					lowerBound[numberOfHoles] = 0.012;
					upperBound[numberOfHoles] = 0.200;
				}
				if (numberOfHoles >= 5)
				{
					// Allow extra space between hands, assuming upper hand
					// uses same number or one more finger than lower hand.
					upperBound[(numberOfHoles+1)/2] = 0.100;
				}
				if (numberOfHoles >= 7)
				{
					// Assume top hole is a thumb hole.
					lowerBound[1] = 0.0005;
				}
				if (numberOfHoles == 10)
				{
					// Assume a thumb hole for the lower hand.
					lowerBound[6] = 0.0005;
				}
				// Bounds on bore diameters.
				lowerBound[2*numberOfHoles + 1] = MIN_BORE_DIAMETER;
				upperBound[2*numberOfHoles + 1] = MAX_BORE_DIAMETER;
				// Bore taper flares out toward bottom.
				Arrays.fill(lowerBound, 2*numberOfHoles + 2, 
						lowerBound.length, 1.0);
				Arrays.fill(upperBound, 2*numberOfHoles + 2, 
						upperBound.length, 5.0);
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
				ContainedReedInstrumentView.class);
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
				ContainedReedInstrumentView.class });
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
					"This study model does not support required optimizer, "
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
						"This study model does not support required optimizer, "
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
