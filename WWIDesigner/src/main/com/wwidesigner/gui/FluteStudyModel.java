/**
 * Study model class to analyze and optimize transverse flutes.
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

import java.util.HashMap;

import com.wwidesigner.modelling.FluteCalculator;
import com.wwidesigner.modelling.InstrumentCalculator;

/**
 * Class to encapsulate methods for analyzing and optimizing transverse flute models.
 * 
 * @author Burton Patkau
 * 
 */
public class FluteStudyModel extends WhistleStudyModel
{
	// Display name for this study
	private static final String FLUTE_DISPLAY_NAME = "Flute Study";

	public FluteStudyModel()
	{
		super();
	}

	public String getDisplayName()
	{
		return FLUTE_DISPLAY_NAME;
	}

	protected void setLocalCategories()
	{
		super.setLocalCategories();
		// Use the airstream length calibrator, which depends on the position of
		// the players lips, instead of the window height calibrator, which is
		// typically more easy to measure on a flute.
		Category optimizers = getCategory(OPTIMIZER_CATEGORY_ID);
		if (optimizers != null)
		{
			optimizers.removeSub(WINDOW_OPT_SUB_CATEGORY_ID);
			optimizers.addSub(AIRSTREAM_OPT_SUB_CATEGORY_ID, null);
		}
	}

	@Override
	protected InstrumentCalculator getCalculator()
	{
		InstrumentCalculator calculator = new FluteCalculator();
		calculator.setPhysicalParameters(params);

		return calculator;
	}

	@Override
	protected void setDefaultViewClassMap()
	{
		defaultXmlViewMap = new HashMap<String, Class<? extends ContainedXmlView>>();

		defaultXmlViewMap.put(INSTRUMENT_CATEGORY_ID,
				ContainedFluteView.class);
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
				ContainedFluteView.class });
		toggleXmlViewLists.put(TUNING_CATEGORY_ID, new Class[] {
				ContainedXmlTextView.class, ContainedTuningView.class });
		toggleXmlViewLists.put(CONSTRAINTS_CATEGORY_ID,
				new Class[] { ContainedXmlTextView.class,
						SizableConstraintsEditorView.class });
	}
}
