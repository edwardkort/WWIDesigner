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

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentComparisonTable;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.ObjectiveFunctionOptimizer;
import com.wwidesigner.optimization.bind.OptimizationBindFactory;
import com.wwidesigner.util.BindFactory;
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
	// Preferences.
	protected BaseObjectiveFunction.OptimizerType preferredOptimizerType;

	// Statistics saved from the most recent call to optimizeInstrument

	protected double initialNorm; // Initial value of objective function.
	protected double finalNorm; // Final value of objective function.

	/**
	 * Tree of selectable categories that the study model supports.
	 */
	protected List<Category> categories;

	/**
	 * Physical parameters to use for this study model.
	 */
	protected PhysicalParameters params;

	public StudyModel()
	{
		setCategories();
		preferredOptimizerType = null;
	}

	protected void setCategories()
	{
		categories = new ArrayList<Category>();
		categories.add(new Category(INSTRUMENT_CATEGORY_ID));
		categories.add(new Category(TUNING_CATEGORY_ID));
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
	 * Class to encapsulate a main branch of the study model selection tree. The
	 * derived study model defines a set of main branches, typically a static
	 * set.
	 */
	protected static class Category
	{
		private String name;
		private Map<String, Object> subs;
		private String selectedSub;

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

		public void removeSub(String name)
		{
			if (name.equals(selectedSub))
			{
				selectedSub = null;
			}
			subs.remove(name);
		}

		public Map<String, Object> getSubs()
		{
			if (subs == null)
			{
				subs = new TreeMap<String, Object>();
			}
			return subs;
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
			if (subs == null)
			{
				return null;
			}
			return subs.get(selectedSub);
		}

		public void replaceSub(String newName, FileDataModel source)
		{
			// Find sub by matching dataModel reference
			String oldName = null;
			boolean isSelected = false;
			if (subs == null)
			{
				subs = new TreeMap<String, Object>();
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
			addSub(newName, source);
			if (isSelected)
			{
				setSelectedSub(newName);
			}
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
	public boolean addDataModel(FileDataModel dataModel)
	{
		String data = (String) dataModel.getData().toString();
		String categoryName = getCategoryName(data);
		if (categoryName == null)
		{
			return false;
		}
		Category category = getCategory(categoryName);
		category.addSub(dataModel.getName(), dataModel);
		category.setSelectedSub(dataModel.getName());
		return true;
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
			category.removeSub(dataModel.getName());
			category = getCategory(TUNING_CATEGORY_ID);
			category.removeSub(dataModel.getName());
			return true;
		}
		category = getCategory(categoryName);
		category.removeSub(dataModel.getName());
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
	{
		String data = (String) dataModel.getData();
		String categoryName = getCategoryName(data);
		if (categoryName == null)
		{
			removeDataModel(dataModel);
			return false;
		}
		Category category = getCategory(categoryName);
		category.replaceSub(dataModel.getName(), dataModel);
		category.setSelectedSub(dataModel.getName());
		return true;
	}

	/**
	 * @return true if category selections are sufficient for calls to
	 *         calculateTuning() and graphTuning().
	 */
	public boolean canTune()
	{
		Category tuningCategory = getCategory(TUNING_CATEGORY_ID);
		String tuningSelected = tuningCategory.getSelectedSub();

		Category instrumentCategory = getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentSelected = instrumentCategory.getSelectedSub();

		return tuningSelected != null && instrumentSelected != null;
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

	public void calculateTuning(String title) throws Exception
	{
		InstrumentTuner tuner = getInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		tuner.setInstrument(getSelectedXmlString(INSTRUMENT_CATEGORY_ID));

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		tuner.setTuning(getSelectedXmlString(TUNING_CATEGORY_ID));

		tuner.setCalculator(getCalculator());

		tuner.showTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}

	public void graphTuning(String title) throws Exception
	{
		InstrumentTuner tuner = getInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		tuner.setInstrument(getSelectedXmlString(INSTRUMENT_CATEGORY_ID));

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		tuner.setTuning(getSelectedXmlString(TUNING_CATEGORY_ID));

		tuner.setCalculator(getCalculator());

		tuner.plotTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}

	/**
	 * Optimize the currently-selected objective function
	 * 
	 * @return XML string defining the optimized instrument, if optimization
	 *         succeeds, or {@code null} if optimization fails.
	 */
	public String optimizeInstrument() throws Exception
	{
		BaseObjectiveFunction objective = getObjectiveFunction();
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
			return xmlString;
		}
		return null;
	} // optimizeInstrument

	public void compareInstrument(String newName, Instrument newInstrument)
			throws Exception
	{
		Category category = getCategory(INSTRUMENT_CATEGORY_ID);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		String oldName = model.getName();
		if (oldName.equals(newName))
		{
			System.out.print("\nError: Current editor tab, ");
			System.out.print(newName);
			System.out.println(" is the same as the selected instrument.");
			System.out
					.println("Select the edit tab for a different instrument.");
			return;
		}
		Instrument oldInstrument = getInstrument();
		InstrumentComparisonTable table = new InstrumentComparisonTable("");
		table.buildTable(oldName, oldInstrument, newName, newInstrument);
		table.showTable(false);
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
		String xmlString = null;

		Category category = getCategory(categoryName);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		if (model.getApplication() != null)
		{
			// If the file is a data view in an active application,
			// update the data in model with the latest from the application's
			// data view.
			model.getApplication().getDataView(model).updateModel(model);
		}
		xmlString = (String) model.getData();

		return xmlString;
	}

	protected Instrument getInstrument() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		String xmlString = getSelectedXmlString(INSTRUMENT_CATEGORY_ID);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				xmlString, true);
		instrument.updateComponents();
		return instrument;
	}

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

		return tuning;
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

	public static Tuning getTuningFromFile(String fileName) throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return tuning;
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
				.contentEquals(OptimizationPreferences.OPT_BOBYQA_NAME))
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer;
		}
		else if (optimizerPreference
				.contentEquals(OptimizationPreferences.OPT_CMAES_NAME))
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.CMAESOptimizer;
		}
		else if (optimizerPreference
				.contentEquals(OptimizationPreferences.OPT_MULTISTART_NAME))
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.MultiStartOptimizer;
		}
		else if (optimizerPreference
				.contentEquals(OptimizationPreferences.OPT_SIMPLEX_NAME))
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.SimplexOptimizer;
		}
		else if (optimizerPreference
				.contentEquals(OptimizationPreferences.OPT_POWELL_NAME))
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.PowellOptimizer;
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

		Map<String, Class<ContainedXmlView>[]> toggleLists = getToggleViewClasses();
		Class<ContainedXmlView>[] toggleViews = toggleLists.get(categoryName);

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

		Map<String, Class<ContainedXmlView>[]> toggleLists = getToggleViewClasses();
		Class<ContainedXmlView>[] toggleViews = toggleLists.get(categoryName);

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
	 * Create the objective function to use for the selected optimization. set
	 * the physical parameters, and set any constraints that the user has
	 * selected.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract BaseObjectiveFunction getObjectiveFunction()
			throws Exception;

	/**
	 * Configures the array of allowed ContainedXmlView classes for each data
	 * type, a CATEGORY_ID, in the XML.
	 * 
	 * @return A Map in which the keys a the data types, and the values are
	 *         arrays of ContainedXmlView classes.
	 */
	protected abstract Map<String, Class<ContainedXmlView>[]> getToggleViewClasses();

	/**
	 * Configures the default ContainedXmlView to be used for each supported
	 * data type, a CATEGORY_ID, in the XML.
	 * 
	 * @param categoryName
	 * @return The Class of the default view. The base StudyModel uses
	 *         reflection to create the instance.
	 */
	protected abstract Class<? extends ContainedXmlView> getDefaultViewClass(
			String categoryName);

}
