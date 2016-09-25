/**
 * Abstract class to encapsulate processes for analyzing and optimizing instrument models.  
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
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import org.apache.commons.math3.exception.ZeroException;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.view.InstrumentComparisonTable;
import com.wwidesigner.gui.util.DataOpenException;
import com.wwidesigner.gui.util.HoleNumberMismatchException;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.modelling.PlayingRangeSpectrum;
import com.wwidesigner.modelling.SupplementaryInfoTable;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.ObjectiveFunctionOptimizer;
import com.wwidesigner.optimization.bind.OptimizationBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Abstract class to encapsulate processes for analyzing and optimizing
 * instrument models.
 * 
 * @author kort
 * 
 */
public abstract class StudyModel implements CategoryType
{
	// Parameters for plotting impedance spectrum.
	// Plot from a major 9th below to 3rd harmonic above.
	protected static final double SPECTRUM_FREQUENCY_BELOW = 0.45;
	protected static final double SPECTRUM_FREQUENCY_ABOVE = 3.17;
	protected static final int SPECTRUM_NUMBER_OF_POINTS = 2000;	// Number of points to plot.
	
	// Preferences.
	protected BaseObjectiveFunction.OptimizerType preferredOptimizerType;

	// Optimization currently active in optimizeInstrument().
	// null if not executing optimizeInstrument.
	protected BaseObjectiveFunction objective;

	// Statistics saved from the most recent call to optimizeInstrument

	protected double initialNorm; // Initial value of objective function.
	protected double finalNorm; // Final value of objective function.

	/**
	 * Tree of selectable categories that the study model supports.
	 */
	protected List<Category> categories;

	/**
	 * Map of the default ContainedXmlView for each viewable category.
	 */
	protected Map<String, Class<? extends ContainedXmlView>> defaultXmlViewMap;

	/**
	 * For each viewable category, the list of ContainedXmlViews that will be
	 * displayed.
	 */
	protected Map<String, Class<ContainedXmlView>[]> toggleXmlViewLists;

	/**
	 * Physical parameters to use for this study model.
	 */
	protected PhysicalParameters params;

	public StudyModel()
	{
		setCategories();
		preferredOptimizerType = null;
		objective = null;
	}

	protected void setCategories()
	{
		categories = new ArrayList<Category>();
		categories.add(new Category(INSTRUMENT_CATEGORY_ID));
		categories.add(new Category(TUNING_CATEGORY_ID));
		setDefaultViewClassMap();
		setToggleViewClassesMap();
	}

	/**
	 * @return List of names of the categories in this study model.
	 */
	public List<String> getCategoryNames()
	{
		List<String> names = new ArrayList<String>();
		for (Category thisCategory : categories)
		{
			names.add(thisCategory.name);
		}
		return names;
	}

	/**
	 * @param Name
	 *            of category to retrieve.
	 * @return The named category, or null if named category not found.
	 */
	protected Category getCategory(String name)
	{
		Category category = null;

		for (Category thisCategory : categories)
		{
			if (thisCategory.toString().equals(name))
			{
				category = thisCategory;
				break;
			}
		}

		return category;
	}

	/**
	 * Find the index in the Categories list of a named category.
	 * 
	 * @param name
	 *            The category name to be found
	 * @return The index of the found category, null otherwise.
	 */
	protected Integer getCategoryIndex(String name)
	{
		Integer index = null;
		int intIndex = 0;
		for (Category thisCategory : categories)
		{
			if (thisCategory.toString().equals(name))
			{
				index = intIndex;
				break;
			}
			intIndex++;
		}

		return index;
	}

	/**
	 * @param categoryName
	 *            - Name of category to select.
	 * @param subcategoryName
	 *            - Name of subcategory to select in the named category. Post:
	 *            getSelectedSub(categoryName) returns subCategoryName.
	 */
	public void setCategorySelection(String categoryName, String subcategoryName)
	{
		for (Category thisCategory : categories)
		{
			if (thisCategory.name.equals(categoryName))
			{
				thisCategory.setSelectedSub(subcategoryName);
			}
		}
	}

	/**
	 * @param categoryName
	 *            - Name of category to look up.
	 * @return Set of names of subcategories of the named category.
	 */
	public Set<String> getSubcategories(String categoryName)
	{
		for (Category thisCategory : categories)
		{
			if (thisCategory.name.equals(categoryName))
			{
				return thisCategory.getSubs().keySet();
			}
		}
		return new HashSet<String>();
	}

	/**
	 * @param categoryName
	 *            - Name of category to look up.
	 * @return Subcategory name supplied to setCategorySelection(categoryName,
	 *         subcategoryName).
	 */
	public String getSelectedSub(String categoryName)
	{
		for (Category thisCategory : categories)
		{
			if (thisCategory.name.equals(categoryName))
			{
				return thisCategory.getSelectedSub();
			}
		}
		return "";
	}

	/**
	 * Determines whether a subcategory is found under a category, either by
	 * subcategory name or value.
	 * 
	 * @param categoryId
	 *            The category ID to be searched.
	 * @param subCategory
	 *            Either the name or value of the subcategory
	 * @param useName
	 *            If true, use the subcategory name, otherwise the value.
	 * @return True if the subcategory is found.
	 */
	protected boolean isValidSubCategory(String categoryId, Object subCategory,
			boolean useName)
	{
		Category category = getCategory(categoryId);
		if (category == null)
		{
			return false;
		}

		return isSubFound(category, subCategory, useName);
	}

	protected boolean isSubFound(Category category, Object subCategory,
			boolean useName)
	{
		Map<String, Object> subs = category.getSubs();
		for (String key : subs.keySet())
		{
			if (useName)
			{
				if (key.equals(subCategory))
				{
					return true;
				}
			}
			else if (subs.get(key).equals(subCategory))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Class to encapsulate a main branch of the study model selection tree. The
	 * derived study model defines a set of main branches, typically a static
	 * set.
	 */
	protected static class Category
	{
		private String name;
		private Map<String, Object> subs;
		private String selectedSub;
		private Map<String, String> toolTips = new HashMap<String, String>();

		public Category(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public void addSub(String name, Object sub)
		{
			if (subs == null)
			{
				subs = new TreeMap<String, Object>();
			}
			subs.put(name, sub);
		}

		public void addSub(String name, Object sub, String toolTip)
		{
			addSub(name, sub);
			toolTips.put(name, toolTip);
		}

		public void removeSub(String name)
		{
			if (name.equals(selectedSub))
			{
				selectedSub = null;
			}
			subs.remove(name);
			toolTips.remove(name);
		}

		public void removeSubByValue(Object value)
		{
			int size = subs.size();
			if (value == null)
			{
				return;
			}

			if (value.equals(getSelectedSubValue()))
			{
				selectedSub = null;
			}

			Iterator<Entry<String, Object>> iterator = subs.entrySet()
					.iterator();
			while (iterator.hasNext())
			{
				Entry<String, Object> entry = iterator.next();
				if (value.equals(entry.getValue()))
				{
					iterator.remove();
					toolTips.remove(entry.getKey());
					if (size != 2)
					{
						break;
					}
				}
				else if (size == 2)
				{
					// If there's only one other entry, select it.
					selectedSub = entry.getKey();
				}
			}
		}

		public Map<String, Object> getSubs()
		{
			if (subs == null)
			{
				subs = new TreeMap<String, Object>();
			}
			return subs;
		}

		public Map<String, String> getToolTips()
		{
			return toolTips;
		}

		public void setSelectedSub(String key)
		{
			selectedSub = key;
		}

		public String getSelectedSub()
		{
			return selectedSub;
		}

		public Object getSelectedSubValue()
		{
			if (subs == null || selectedSub == null)
			{
				return null;
			}
			return subs.get(selectedSub);
		}

		/**
		 * Replaces a subcategory entry because of a name change: save as or
		 * rename
		 * 
		 * @param newName
		 *            New subcategory name
		 * @param source
		 *            DataModel value of the subcategory
		 * @return True if a successful replacement was performed, false
		 *         otherwise
		 */
		public boolean replaceSub(String newName, FileDataModel source)
		{
			// Find sub by matching dataModel reference
			String oldName = null;
			boolean isSelected = false;
			if (subs == null)
			{
				return false;
			}
			for (Map.Entry<String, Object> entry : subs.entrySet())
			{
				FileDataModel model = (FileDataModel) entry.getValue();
				if (source.equals(model))
				{
					oldName = entry.getKey();
					break;
				}
			}
			if (oldName != null)
			{
				if (oldName.equals(selectedSub))
				{
					isSelected = true;
				}
				removeSub(oldName);
			}
			else
			{
				return false;
			}
			addSub(newName, source);
			if (isSelected)
			{
				setSelectedSub(newName);
			}

			return true;
		}
	}

	/**
	 * @param xmlString
	 *            - XML defining an Instrument or a Tuning.
	 * @return Name of category that the definition of xmlString fits, either
	 *         INSTRUMENT_CATEGORY_ID or TUNING_CATEGORY_ID.
	 */
	public static String getCategoryName(String xmlString)
	{
		// Check for an Instrument
		BindFactory bindFactory = GeometryBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Instrument", true)) // TODO Make
																	// constants
																	// in
																	// binding
																	// framework
		{
			return INSTRUMENT_CATEGORY_ID;
		}

		// Check for a Tuning
		bindFactory = NoteBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Tuning", true)) // TODO Make
																// constants in
																// binding
																// framework
		{
			return TUNING_CATEGORY_ID;
		}

		// Check for a Constraints
		bindFactory = OptimizationBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Constraints", true)) // TODO Make
		// constants in
		// binding
		// framework
		{
			return CONSTRAINTS_CATEGORY_ID;
		}

		return null;
	}

	protected Integer getNumberOfHolesFromInstrument()
	{
		try
		{
			return getHoleCountFromSelected(INSTRUMENT_CATEGORY_ID);
		}
		catch (Exception e)
		{
		}

		return null;
	}

	/**
	 * Replaces a Category in the categories list.
	 * 
	 * @param categoryId
	 *            Category ID
	 * @param replacementCategory
	 */
	protected void replaceCategory(String categoryId,
			Category replacementCategory)
	{
		int categoryIndex = getCategoryIndex(CONSTRAINTS_CATEGORY_ID);
		categories.set(categoryIndex, replacementCategory);
	}

	/**
	 * Add an Instrument or Tuning to the category tree, from a JIDE
	 * FileDataModel. Post: If dataModel is valid XML, it is added to
	 * INSTRUMENT_CATEGORY_ID, or TUNING_CATEGORY_ID, as appropriate, and
	 * addDataModel returns true.
	 * 
	 * @param dataModel
	 *            - FileDataModel containing instrument or tuning XML.
	 * @return true iff the dataModel contained valid instrument or tuning XML.
	 */
	public boolean addDataModel(FileDataModel dataModel, boolean isNew)
			throws Exception
	{
		String data = (String) dataModel.getData().toString();
		if (data == null || data.length() == 0)
		{
			return false;
		}
		String categoryName = getCategoryName(data);
		if (categoryName == null)
		{
			throw new DataOpenException("Data is not a supported type",
					DataOpenException.DATA_TYPE_NOT_SUPPORTED);
		}
		dataModel.setSemanticName(categoryName);
		if (categoryName.equals(INSTRUMENT_CATEGORY_ID)
				|| categoryName.equals(TUNING_CATEGORY_ID))
		{
			Category category = getCategory(categoryName);
			category.addSub(dataModel.getName(), dataModel);
			category.setSelectedSub(dataModel.getName());
			if (!isNew)
			{
				validHoleCount();
			}
			return true;
		}

		return false;
	}

	/**
	 * Remove an Instrument or Tuning from the category tree, given a JIDE
	 * FileDataModel. Pre: Assumes that the type of XML, Instrument or Tuning,
	 * has not changed since the call to addDataModel. Post: The specified
	 * dataModel is no longer in INSTRUMENT_CATEGORY_ID, or TUNING_CATEGORY_ID,
	 * as appropriate.
	 * 
	 * @param dataModel
	 *            - FileDataModel containing instrument or tuning XML.
	 * @return true.
	 */
	public boolean removeDataModel(FileDataModel dataModel)
	{
		String data = (String) dataModel.getData();
		String categoryName = getCategoryName(data);
		Category category;
		if (categoryName == null)
		{
			// Invalid XML. Remove from both categories.
			category = getCategory(INSTRUMENT_CATEGORY_ID);
			category.removeSubByValue(dataModel);
			category = getCategory(TUNING_CATEGORY_ID);
			category.removeSubByValue(dataModel);
			return true;
		}
		category = getCategory(categoryName);
		category.removeSubByValue(dataModel);
		return true;
	}

	/**
	 * Add an Instrument or Tuning to the category tree, from a JIDE
	 * FileDataModel, replacing any existing instance. Pre: Assumes that the
	 * type of XML, Instrument or Tuning, has not changed since the call to
	 * addDataModel (if any). Post: The prior instance of dataModel is removed
	 * from INSTRUMENT_CATEGORY_ID, or TUNING_CATEGORY_ID, as appropriate If
	 * dataModel is valid XML, it is added to INSTRUMENT_CATEGORY_ID, or
	 * TUNING_CATEGORY_ID, as appropriate, and addDataModel returns true.
	 * 
	 * @param dataModel
	 *            - FileDataModel containing instrument or tuning XML.
	 * @return true if the dataModel contained valid instrument or tuning XML.
	 */
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
		Category category = getCategory(categoryName);
		if (category.replaceSub(dataModel.getName(), dataModel))
		{
			category.setSelectedSub(dataModel.getName());
			return true;
		}

		return false;
	}

	/**
	 * @return true if category selections are sufficient for calls to
	 *         calculateTuning() and graphTuning().
	 */
	public boolean canTune()
	{
		boolean canTune = false;
		try
		{
			canTune = validHoleCount();
		}
		catch (Exception e)
		{
		}

		return canTune;
	}

	protected boolean validHoleCount() throws Exception
	{
		Integer tuningHoleCount = getHoleCountFromSelected(TUNING_CATEGORY_ID);
		Integer instrumentHoleCount = getHoleCountFromSelected(INSTRUMENT_CATEGORY_ID);
		if (tuningHoleCount == null || instrumentHoleCount == null)
		{
			return false;
		}
		if (tuningHoleCount == instrumentHoleCount)
		{
			return true;
		}
		throw new HoleNumberMismatchException("Tuning file has "
				+ tuningHoleCount + " holes, Instrument has "
				+ instrumentHoleCount + " holes.");
	}

	/**
	 * Gets the hole count from the selected data of the specified data type.
	 * 
	 * @param categoryId
	 *            One of CONSTRAINTS_CATEGORY_ID, INSTRUMENT_CATEGORY_ID, or
	 *            TUNING_CATEGORY_ID
	 * @return The hole count, null if the specified data type does not have a
	 *         selected value
	 * @throws Exception
	 *             On data parse error
	 */
	protected Integer getHoleCountFromSelected(String categoryId)
			throws Exception
	{
		Integer holeCount = null;
		if (TUNING_CATEGORY_ID.equals(categoryId))
		{
			Category tuningCategory = getCategory(TUNING_CATEGORY_ID);
			String tuningSelected = tuningCategory.getSelectedSub();
			if (tuningSelected != null)
			{
				Tuning tuning = getTuning();
				holeCount = tuning.getNumberOfHoles();
			}
		}
		else if (INSTRUMENT_CATEGORY_ID.equals(categoryId))
		{
			Category instrumentCategory = getCategory(INSTRUMENT_CATEGORY_ID);
			String instrumentSelected = instrumentCategory.getSelectedSub();
			if (instrumentSelected != null)
			{
				Instrument instrument = getInstrument();
				// Don't bother checking instrument validity.
				holeCount = instrument.getHole().size();
			}
		}
		else if (CONSTRAINTS_CATEGORY_ID.equals(categoryId))
		{
			Category constraintsCategory = getCategory(CONSTRAINTS_CATEGORY_ID);
			String constraintsSelected = constraintsCategory.getSelectedSub();
			if (constraintsSelected != null)
			{
				Constraints constraints = getConstraints();
				holeCount = constraints.getNumberOfHoles();
			}
		}

		return holeCount;
	}

	/**
	 * @return true if category selections are sufficient for calls to
	 *         optimizeInstrument().
	 */
	public boolean canOptimize()
	{
		if (!canTune())
		{
			return false;
		}
		Category category = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizerSelected = category.getSelectedSub();

		return optimizerSelected != null;
	}

	/**
	 * A stub to update/change the Constraints Category upon selection changes
	 * in the other Category selections. Should be overridden in subclasses that
	 * need this functionality.
	 */
	public void updateConstraints()
	{
	}

	/**
	 * A stub to build up the current path to Constraints. Should be overridden
	 * in subclasses that need this functionality.
	 * 
	 * @param rootDirectoryPath
	 *            Root of all the constraints. May be blank, but not null.
	 * @return The File representing the leaf directory. May be null if not
	 *         overridden.
	 */
	public File getConstraintsLeafDirectory(String rootDirectoryPath)
	{
		return null;
	}

	public File getConstraintsLeafDirectory(String rootDirectoryPath,
			Constraints constraints)
	{
		return null;
	}

	/**
	 * A stub to set the Constraint menu items active. Overwrite in subclasses.
	 * 
	 * @param constraintsDirectory
	 *            The root of the constraints directory tree.
	 * @return
	 */
	public boolean isOptimizerFullySpecified(String constraintsDirectory)
	{
		return isOptimizerConstraintsDirectorySpecified(constraintsDirectory)
				&& isOptimizerCreateSpecified();
	}

	public boolean isOptimizerConstraintsDirectorySpecified(
			String constraintsDirectory)
	{
		if (constraintsDirectory == null
				|| constraintsDirectory.trim().length() == 0)
		{
			return false;
		}

		return true;
	}

	public boolean isOptimizerCreateSpecified()
	{
		boolean isSpecified;
		Integer dataNumberOfHoles = getNumberOfHolesFromInstrument();
		String optimizerSelected = getSelectedSub(OPTIMIZER_CATEGORY_ID);
		// Constraints set is fully specified.
		isSpecified = dataNumberOfHoles != null && optimizerSelected != null;

		return isSpecified;

	}

	public void calculateTuning(String title) throws Exception
	{
		InstrumentTuner tuner = getInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		tuner.setInstrument(getInstrument());

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		tuner.setTuning(getTuning());

		tuner.setCalculator(getCalculator());

		tuner.showTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}
	
	public void calculateSupplementaryInfo(String title) throws Exception
	{
		InstrumentTuner tuner = getInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		tuner.setInstrument(getInstrument());

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		tuner.setTuning(getTuning());

		tuner.setCalculator(getCalculator());

		SupplementaryInfoTable table = new SupplementaryInfoTable(title + ": " + instrumentName + "/" + tuningName);
		table.buildTable(tuner, false);
		table.showTable(false);
	}

	public void graphTuning(String title) throws Exception
	{
		InstrumentTuner tuner = getInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		tuner.setInstrument(getInstrument());

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		tuner.setTuning(getTuning());

		tuner.setCalculator(getCalculator());

		tuner.plotTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}
	
	public void graphNote(Fingering fingering) throws Exception
	{
		Instrument instrument = getInstrument();
		if (instrument.getHole().size() != fingering.getNumberOfHoles())
		{
			throw new HoleNumberMismatchException("Tuning file has "
					+ fingering.getNumberOfHoles() + " holes, Instrument has "
					+ instrument.getHole().size() + " holes.");
		}
		InstrumentCalculator calculator = getCalculator();
		calculator.setInstrument(instrument);
		PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();
		spectrum.plot(calculator, fingering, SPECTRUM_FREQUENCY_BELOW, SPECTRUM_FREQUENCY_ABOVE,
				SPECTRUM_NUMBER_OF_POINTS, false);
	}

	public String getDefaultConstraints(Object... parentFrame) throws Exception
	{
		BaseObjectiveFunction objective = getObjectiveFunction(BaseObjectiveFunction.DEFAULT_CONSTRAINTS_INTENT);
		// For the case of a cancelled hole-grouping.
		if (objective == null)
		{
			return null;
		}
		Constraints constraints = objective.getConstraints();
		constraints.setConstraintsName("Default");
		String xmlConstraints = marshal(constraints);

		return xmlConstraints;
	}

	public String getBlankConstraints(Frame parentFrame) throws Exception
	{
		BaseObjectiveFunction objective = getObjectiveFunction(BaseObjectiveFunction.BLANK_CONSTRAINTS_INTENT);
		// For the case of a cancelled hole-grouping.
		if (objective == null)
		{
			return null;
		}
		Constraints constraints = objective.getConstraints();
		constraints.setConstraintsName("Blank");
		String xmlConstraints = marshal(constraints);

		return xmlConstraints;
	}

	/**
	 * Optimize the currently-selected objective function
	 * 
	 * @return XML string defining the optimized instrument, if optimization
	 *         succeeds, or {@code null} if optimization fails.
	 */
	public String optimizeInstrument() throws Exception
	{
		if (!validHoleCount())
		{
			return null;
		}
		objective = getObjectiveFunction(BaseObjectiveFunction.OPTIMIZATION_INTENT);

		// Check to see whether there are 0 variables: an infinite loop
		// situation.
		if (objective.getNrDimensions() < 1)
		{
			throw new ZeroException();
		}

		BaseObjectiveFunction.OptimizerType optimizerType = objective
				.getOptimizerType();
		if (preferredOptimizerType != null
				&& !optimizerType
						.equals(BaseObjectiveFunction.OptimizerType.BrentOptimizer))
		{
			optimizerType = preferredOptimizerType;
		}

		initialNorm = 1.0;
		finalNorm = 1.0;
		if (ObjectiveFunctionOptimizer.optimizeObjectiveFunction(objective,
				optimizerType))
		{
			Instrument instrument = objective.getInstrument();
			// Convert back to the input unit-of-measure values
			instrument.convertToLengthType();
			String xmlString = marshal(instrument);
			initialNorm = ObjectiveFunctionOptimizer.getInitialNorm();
			finalNorm = ObjectiveFunctionOptimizer.getFinalNorm();
			objective = null;
			return xmlString;
		}
		objective = null;
		return null;
	} // optimizeInstrument
	
	public void cancelOptimization()
	{
		if (objective != null)
		{
			objective.setCancel(true);
		}
	}

	public void compareInstrument(String newName, Instrument newInstrument,
			LengthType defaultLengthType) throws Exception
	{
		String oldName = getSelectedInstrumentName();
		Instrument oldInstrument = getInstrument();
		InstrumentComparisonTable table = getInstrumentComparisonTable("",
				defaultLengthType);
		table.buildTable(oldName, oldInstrument, newName, newInstrument);
		table.showTable();
	}

	protected InstrumentComparisonTable getInstrumentComparisonTable(
			String title, LengthType defaultLengthType)
	{
		return new InstrumentComparisonTable(title, defaultLengthType);
	}

	public String getSelectedInstrumentName()
	{
		String name = "";
		Category category = getCategory(INSTRUMENT_CATEGORY_ID);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		if (model != null)
		{
			name = model.getName();
		}

		return name;
	}

	public static String marshal(Instrument instrument) throws Exception
	{
		BindFactory binder = GeometryBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		binder.marshalToXml(instrument, writer);

		return writer.toString();
	}

	public static String marshal(Tuning tuning) throws Exception
	{
		BindFactory binder = NoteBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		binder.marshalToXml(tuning, writer);

		return writer.toString();
	}

	public static String marshal(Constraints constraints) throws Exception
	{
		BindFactory bindFactory = OptimizationBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		bindFactory.marshalToXml(constraints, writer);

		return writer.toString();
	}

	protected String getSelectedXmlString(String categoryName) throws Exception
	{
		Category category = getCategory(categoryName);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		if (model == null)
		{
			return null;
		}
		if (model.getApplication() != null)
		{
			// If the file is a data view in an active application,
			// update the data in model with the latest from the application's
			// data view.
			model.getApplication().getDataView(model).updateModel(model);
		}
		return (String) model.getData();
	}

	/**
	 * Return the instrument currently selected in the study model.
	 * 
	 * @return a valid Instrument
	 * @throws Exception
	 *             if no valid instrument is selected.
	 */
	protected Instrument getInstrument() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		String xmlString = getSelectedXmlString(INSTRUMENT_CATEGORY_ID);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				xmlString, true);
		instrument.checkValidity();
		instrument.updateComponents();
		return instrument;
	}

	/**
	 * Extract an instrument from an XML string, if possible. The instrument is
	 * not checked for validity.
	 * 
	 * @param xmlString
	 *            - String containing XML for an instrument definition.
	 * @return an Instrument, or null
	 */
	public static Instrument getInstrument(String xmlString)
	{
		try
		{
			BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
			Instrument instrument = (Instrument) geometryBindFactory
					.unmarshalXml(xmlString, true);
			instrument.updateComponents();
			return instrument;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	protected Tuning getTuning() throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		String xmlString = getSelectedXmlString(TUNING_CATEGORY_ID);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(xmlString, true);
		tuning.checkValidity();

		return tuning;
	}

	protected Constraints getConstraints() throws Exception
	{
		BindFactory constraintsBindFactory = OptimizationBindFactory
				.getInstance();
		String xmlString = getSelectedXmlString(CONSTRAINTS_CATEGORY_ID);
		Constraints constraints = (Constraints) constraintsBindFactory
				.unmarshalXml(xmlString, true);
		constraints.setConstraintParent();

		return constraints;
	}

	public static Instrument getInstrumentFromFile(String fileName)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
	}

	public static Tuning getTuning(String xmlString)
	{
		try
		{
			BindFactory noteBindFactory = NoteBindFactory.getInstance();
			Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(xmlString,
					true);
			return tuning;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static Tuning getTuningFromFile(String fileName) throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return tuning;
	}

	public static Constraints getConstraints(String xmlString)
	{
		try
		{
			BindFactory constraintsBindFactory = OptimizationBindFactory
					.getInstance();
			Constraints constraints = (Constraints) constraintsBindFactory
					.unmarshalXml(xmlString, true);
			constraints.setConstraintParent();
			return constraints;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public PhysicalParameters getParams()
	{
		return params;
	}

	public void setParams(PhysicalParameters params)
	{
		this.params = params;
	}

	/**
	 * Set study model preferences from application preferences.
	 * 
	 * @param newPreferences
	 */
	public void setPreferences(Preferences newPreferences)
	{
		double currentTemperature = newPreferences.getDouble(
				OptimizationPreferences.TEMPERATURE_OPT,
				OptimizationPreferences.DEFAULT_TEMPERATURE);
		double currentPressure = newPreferences.getDouble(
				OptimizationPreferences.PRESSURE_OPT,
				OptimizationPreferences.DEFAULT_PRESSURE);
		int currentHumidity = newPreferences.getInt(
				OptimizationPreferences.HUMIDITY_OPT,
				OptimizationPreferences.DEFAULT_HUMIDITY);
		int currentCO2 = newPreferences.getInt(
				OptimizationPreferences.CO2_FRACTION_OPT,
				OptimizationPreferences.DEFAULT_CO2_FRACTION);
		double xCO2 = currentCO2 * 1.0e-6;
		getParams().setProperties(currentTemperature, currentPressure,
				currentHumidity, xCO2);
		getParams().printProperties();

		String optimizerPreference = newPreferences.get(
				OptimizationPreferences.OPTIMIZER_TYPE_OPT,
				OptimizationPreferences.OPT_DEFAULT_NAME);
		if (optimizerPreference
				.contentEquals(OptimizationPreferences.OPT_DEFAULT_NAME))
		{
			preferredOptimizerType = null;
		}
		else if (optimizerPreference
				.contentEquals(OptimizationPreferences.OPT_DIRECT_NAME))
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.DIRECTOptimizer;
		}
		else
		{
			preferredOptimizerType = null;
		}
	}

	// Methods to return statistics from an optimization.

	public double getInitialNorm()
	{
		return initialNorm;
	}

	public double getFinalNorm()
	{
		return finalNorm;
	}

	public double getResidualErrorRatio()
	{
		return finalNorm / initialNorm;
	}

	protected Class<? extends ContainedXmlView> getDefaultViewClass(
			String categoryName)
	{
		Class<? extends ContainedXmlView> defaultClass = defaultXmlViewMap
				.get(categoryName);
		defaultClass = defaultClass == null ? ContainedXmlTextView.class
				: defaultClass;

		return defaultClass;
	}

	protected Class<ContainedXmlView>[] getToggleViewClasses(String categoryName)
	{
		return toggleXmlViewLists.get(categoryName);
	}

	/**
	 * Create the default view for and XML dataModel for each type represented
	 * in the XML.
	 * 
	 * @param dataModel
	 * @return created ContainedXmlView
	 */
	public ContainedXmlView getDefaultXmlView(FileDataModel dataModel,
			DataViewPane parent)
	{
		String xmlData = (String) dataModel.getData().toString();
		String categoryName = getCategoryName(xmlData);

		Class<? extends ContainedXmlView> defaultViewClass = getDefaultViewClass(categoryName);
		ContainedXmlView defaultView = null;
		try
		{
			Constructor<? extends ContainedXmlView> constr = defaultViewClass
					.getConstructor(new Class[] { DataViewPane.class });
			defaultView = (ContainedXmlView) constr
					.newInstance(new Object[] { parent });
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}

		return defaultView;
	}

	/**
	 * Creates the next ContainedXmlView instance for a model that has multiple
	 * views configured in getToggleViewClasses. The GUI logic only allows this
	 * call to be made if there is a multiple of such views.
	 * 
	 * @param dataModel
	 *            Used to derive the data type, a CATEGORY_ID
	 * @param containedXmlView
	 *            Used to determine the next view
	 * @param parent
	 *            Needed in the constructor of the new view instance
	 * @return The new ContainedXmlView instance. May return the input
	 *         ContainedXmlView if there is a programming error.
	 */
	public ContainedXmlView getNextXmlView(BasicDataModel dataModel,
			ContainedXmlView containedXmlView, DataViewPane parent)
	{
		Class<? extends ContainedXmlView> currentViewClass = containedXmlView
				.getClass();
		ContainedXmlView nextView = null;

		String xmlData = (String) dataModel.getData().toString();
		String categoryName = getCategoryName(xmlData);

		Class<ContainedXmlView>[] toggleViews = getToggleViewClasses(categoryName);

		Class<ContainedXmlView> nextViewClass = null;
		int numberOfToggles = toggleViews == null ? 0 : toggleViews.length;
		if (numberOfToggles > 1)
		{
			for (int i = 0; i < numberOfToggles; i++)
			{
				Class<ContainedXmlView> toggleView = toggleViews[i];
				if (toggleView.equals(currentViewClass))
				{
					if (i == (numberOfToggles - 1))
					{
						nextViewClass = toggleViews[0];
					}
					else
					{
						nextViewClass = toggleViews[i + 1];
					}
					break;
				}
			}
			// This should only happen if you change study models with open data
			// views
			if (nextViewClass == null)
			{
				nextViewClass = toggleViews[0];
			}
			try
			{
				Constructor<ContainedXmlView> constr = nextViewClass
						.getConstructor(new Class[] { DataViewPane.class });
				nextView = (ContainedXmlView) constr
						.newInstance(new Object[] { parent });
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}

		// Return the original view on error
		nextView = nextView == null ? containedXmlView : nextView;
		return nextView;
	}

	/**
	 * Returns the number of alternative ContainedXmlViews configured for a
	 * specific data type, a CATEGORY_ID, in the XML.
	 * 
	 * @param dataModel
	 *            Used to determine the data type
	 * @return The number of alternative views, 0 if there are none configured
	 */
	public int getNumberOfToggleViews(BasicDataModel dataModel)
	{
		String xmlData = (String) dataModel.getData().toString();
		String categoryName = getCategoryName(xmlData);

		Class<ContainedXmlView>[] toggleViews = getToggleViewClasses(categoryName);

		int numberOfViews = 0;
		if (toggleViews != null)
		{
			numberOfViews = toggleViews.length;
		}

		return numberOfViews;
	}

	// Methods to create objects that will perform this study,
	// according to components that the user has selected.

	/**
	 * Create the selected calculator, and set its physical parameters.
	 * 
	 * @return created calculator.
	 */
	protected abstract InstrumentCalculator getCalculator();

	/**
	 * Create the instrument tuner appropriate for this study.
	 * 
	 * @return created tuner.
	 */
	protected abstract InstrumentTuner getInstrumentTuner();

	/**
	 * Create the objective function to use for either the selected
	 * optimization, to generate a blank Constraints, or to generate a default
	 * Constraints. Set the physical parameters, and set any constraints that
	 * the user has selected as appropriate for the objectiveFunctionIntent.
	 * 
	 * @param objectiveFunctionIntent
	 *            Indicates the Constraints content the objectiveFunction will
	 *            create.
	 * @return
	 * @throws Exception
	 */
	protected abstract BaseObjectiveFunction getObjectiveFunction(
			int objectiveFunctionIntent) throws Exception;

	/**
	 * Configures the array of allowed ContainedXmlView classes for each data
	 * type, a CATEGORY_ID, in the XML.
	 * 
	 * @return A Map in which the keys a the data types, and the values are
	 *         arrays of ContainedXmlView classes.
	 */
	protected abstract void setToggleViewClassesMap();

	/**
	 * Configures the default ContainedXmlView to be used for each supported
	 * data type, a CATEGORY_ID, in the XML.
	 * 
	 * @param categoryName
	 * @return The Class of the default view. The base StudyModel uses
	 *         reflection to create the instance.
	 */
	protected abstract void setDefaultViewClassMap();

	/**
	 * 
	 * @return The name to be displayed in the StudyView title.
	 */
	public abstract String getDisplayName();

}
