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

import java.util.Arrays;
import java.util.prefs.Preferences;

import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.BellNoteEvaluator;
import com.wwidesigner.modelling.CentDeviationEvaluator;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.FmaxEvaluator;
import com.wwidesigner.modelling.FminEvaluator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.LinearVInstrumentTuner;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.BasicTaperObjectiveFunction;
import com.wwidesigner.optimization.BetaObjectiveFunction;
import com.wwidesigner.optimization.HoleAndTaperObjectiveFunction;
import com.wwidesigner.optimization.HoleObjectiveFunction;
import com.wwidesigner.optimization.HolePositionObjectiveFunction;
import com.wwidesigner.optimization.HoleSizeObjectiveFunction;
import com.wwidesigner.optimization.LengthObjectiveFunction;
import com.wwidesigner.optimization.WindowHeightObjectiveFunction;
import com.wwidesigner.optimization.multistart.GridRangeProcessor;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Class to encapsulate methods for analyzing and optimizing whistle models.
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
	public static final String TAPER_OPT_SUB_CATEGORY_ID = "7. Taper Optimizer";
	public static final String HOLE_TAPER_OPT_SUB_CATEGORY_ID = "8. Hole and Taper Optimizer";
	public static final String ROUGH_CUT_OPT_SUB_CATEGORY_ID = "9. Rough-Cut Optimizer";

	public static final double MIN_HOLE_DIAMETER = 0.0040;	// Minimum hole diameter, in meters.
	public static final double MAX_HOLE_DIAMETER = 0.0091;	// Maximum hole diameter, in meters.

	protected int blowingLevel;

	public WhistleStudyModel()
	{
		super();
		setLocalCategories();
		setBlowingLevel(5);
	}

	protected void setLocalCategories()
	{
		setParams(new PhysicalParameters(27, TemperatureType.C, 98.4, 100, 0.04));
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(WINDOW_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(BETA_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(LENGTH_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLESPACE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(TAPER_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLE_TAPER_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(ROUGH_CUT_OPT_SUB_CATEGORY_ID, null);
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

		BaseObjectiveFunction objective = null;
		double[] lowerBound = null;
		double[] upperBound = null;

		switch (optimizer)
		{
			case WINDOW_OPT_SUB_CATEGORY_ID:
				evaluator = new FmaxEvaluator(calculator, getInstrumentTuner());
				objective = new WindowHeightObjectiveFunction(calculator,
						tuning, evaluator);
				lowerBound = new double[] { 0.000 };
				upperBound = new double[] { 0.010 };
				break;
			case BETA_OPT_SUB_CATEGORY_ID:
				evaluator = new FminEvaluator(calculator, getInstrumentTuner());
				objective = new BetaObjectiveFunction(calculator, tuning,
						evaluator);
				lowerBound = new double[] { 0.2 };
				upperBound = new double[] { 0.5 };
				break;
			case LENGTH_OPT_SUB_CATEGORY_ID:
				evaluator = new BellNoteEvaluator(calculator);
				objective = new LengthObjectiveFunction(calculator, tuning,
						evaluator);
				lowerBound = new double[] { 0.200 };
				upperBound = new double[] { 0.700 };
				break;
			case HOLESIZE_OPT_SUB_CATEGORY_ID:
				evaluator = new CentDeviationEvaluator(calculator, getInstrumentTuner());
				objective = new HoleSizeObjectiveFunction(calculator, tuning,
						evaluator);
				// Bounds are diameters, expressed in meters.
				lowerBound = new double[tuning.getNumberOfHoles()];
				upperBound = new double[tuning.getNumberOfHoles()];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);
				break;
			case HOLESPACE_OPT_SUB_CATEGORY_ID:
			case ROUGH_CUT_OPT_SUB_CATEGORY_ID:
				evaluator = new CentDeviationEvaluator(calculator, getInstrumentTuner());
				objective = new HolePositionObjectiveFunction(calculator,
						tuning, evaluator);
				// Bounds are hole separations, expressed in meters.
				lowerBound = new double[tuning.getNumberOfHoles() + 1];
				upperBound = new double[tuning.getNumberOfHoles() + 1];
				Arrays.fill(lowerBound, 0.012);
				lowerBound[0] = 0.200;
				Arrays.fill(upperBound, 0.040);
				upperBound[0] = 0.700;
				upperBound[tuning.getNumberOfHoles()] = 0.200;
				if (tuning.getNumberOfHoles() >= 5)
				{
					// Allow extra space between hands.
					upperBound[tuning.getNumberOfHoles() - 3] = 0.100;
				}
				
				// For a rough-cut optimization, use multi-start optimization.

				if (optimizer == ROUGH_CUT_OPT_SUB_CATEGORY_ID)
				{
					int nrOfStarts = 4 * tuning.getNumberOfHoles();
					GridRangeProcessor rangeProcessor = new GridRangeProcessor(
							lowerBound, upperBound, null, nrOfStarts);
					objective.setRangeProcessor(rangeProcessor);
					objective.setMaxEvaluations(nrOfStarts * objective.getMaxEvaluations());
				}
				break;
			case HOLE_OPT_SUB_CATEGORY_ID:
			default:
				evaluator = new CentDeviationEvaluator(calculator, getInstrumentTuner());
				objective = new HoleObjectiveFunction(calculator, tuning,
						evaluator);
				// Separation and diameter bounds, expressed in meters.
				lowerBound = new double[2 * tuning.getNumberOfHoles() + 1];
				upperBound = new double[2 * tuning.getNumberOfHoles() + 1];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);		// Minimum hole diameter.
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);		// Maximum hole diameter.
				// Bounds on hole spacing.
				lowerBound[0] = 0.200;
				upperBound[0] = 0.700;
				for (int gapNr = 1; gapNr < tuning.getNumberOfHoles(); ++gapNr ) {
					lowerBound[gapNr] = 0.012;
					upperBound[gapNr] = 0.040;
				}
				lowerBound[tuning.getNumberOfHoles()] = 0.012;
				upperBound[tuning.getNumberOfHoles()] = 0.200;
				if (tuning.getNumberOfHoles() >= 5)
				{
					// Allow extra space between hands.
					upperBound[tuning.getNumberOfHoles() - 3] = 0.100;
				}
				break;
			case TAPER_OPT_SUB_CATEGORY_ID:
				evaluator = new CentDeviationEvaluator(calculator, getInstrumentTuner());
				objective = new BasicTaperObjectiveFunction(calculator, tuning,
						evaluator);
				// Dimensions are expressed as ratios: head length, foot diameter.
				lowerBound = new double[] { 0.01, 0.3 };
				upperBound = new double[] { 0.5,  2.0 };
				break;
			case HOLE_TAPER_OPT_SUB_CATEGORY_ID:
				evaluator = new CentDeviationEvaluator(calculator, getInstrumentTuner());
				objective = new HoleAndTaperObjectiveFunction(calculator, tuning,
						evaluator);
				// Separation bounds and diameter bounds, expressed in meters,
				// and two taper ratios.
				lowerBound = new double[2 * tuning.getNumberOfHoles() + 3];
				upperBound = new double[2 * tuning.getNumberOfHoles() + 3];
				Arrays.fill(lowerBound, MIN_HOLE_DIAMETER);		// Minimum hole diameter.
				Arrays.fill(upperBound, MAX_HOLE_DIAMETER);		// Maximum hole diameter.
				// Bounds on hole spacing.
				lowerBound[0] = 0.200;
				upperBound[0] = 0.700;
				for (int gapNr = 1; gapNr < tuning.getNumberOfHoles(); ++gapNr ) {
					lowerBound[gapNr] = 0.012;
					upperBound[gapNr] = 0.040;
				}
				lowerBound[tuning.getNumberOfHoles()] = 0.012;
				upperBound[tuning.getNumberOfHoles()] = 0.200;
				if (tuning.getNumberOfHoles() >= 5)
				{
					// Allow extra space between hands.
					upperBound[tuning.getNumberOfHoles() - 3] = 0.100;
				}
				// Bounds on taper.
				lowerBound[lowerBound.length-2] = 0.01;
				lowerBound[lowerBound.length-1] = 0.3;
				upperBound[upperBound.length-2] = 0.5;
				upperBound[upperBound.length-1] = 2.0;
				break;
		}

		objective.setLowerBounds(lowerBound);
		objective.setUpperBounds(upperBound);
		return objective;
	}

	@Override
	public ContainedXmlView getDefaultXmlView(FileDataModel dataModel,
			DataViewPane parent)
	{
		String data = (String) dataModel.getData().toString();
		String categoryName = getCategoryName(data);
		ContainedXmlView view;

		switch (categoryName)
		{
			case INSTRUMENT_CATEGORY_ID:
				view = new ContainedInstrumentView(parent);
				break;
			case TUNING_CATEGORY_ID:
				view = new ContainedTuningView(parent);
				break;
			case CONSTRAINTS_CATEGORY_ID:
				view = new ContainedXmlTextView(parent);
				break;
			default:
				view = new ContainedXmlTextView(parent);
				break;
		}

		return view;
	}
}
