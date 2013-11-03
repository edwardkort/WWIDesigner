/**
 * 
 */
package com.wwidesigner.gui;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentComparisonTable;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.ObjectiveFunctionOptimizer;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class StudyModel
{
	public static final String INSTRUMENT_CATEGORY_ID = "Instrument";
	public static final String TUNING_CATEGORY_ID = "Tuning";
	public static final String CALCULATOR_CATEGORY_ID = "Instrument calculator";
	public static final String MULTI_START_CATEGORY_ID = "Multi-start optimization";
	public static final String OPTIMIZER_CATEGORY_ID = "Optimizer";
	public static final String CONSTRAINT_CATEGORY_ID = "Constraint set";

	// Preferences.
	protected BaseObjectiveFunction.OptimizerType preferredOptimizerType;

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

	public List<Category> getCategories()
	{
		return categories;
	}

	public Category getCategory(String name)
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

	public void setCategorySelection(Category category, String subCategoryName)
	{
		for (Category thisCategory : categories)
		{
			if (thisCategory.name.equals(category.name))
			{
				thisCategory.setSelectedSub(subCategoryName);
			}
		}
	}

	public static class Category
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
			return subs == null ? new TreeMap<String, Object>() : subs;
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
			return subs.get(selectedSub);
		}

		public void replaceSub(String newName, FileDataModel source)
		{
			// Find sub by matching dataModel reference
			String oldName = null;
			boolean isSelected = false;
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

	public boolean canTune()
	{
		Category tuningCategory = getCategory(TUNING_CATEGORY_ID);
		String tuningSelected = tuningCategory.getSelectedSub();

		Category instrumentCategory = getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentSelected = instrumentCategory.getSelectedSub();

		return tuningSelected != null && instrumentSelected != null;
	}

	public boolean canOptimize()
	{
		if ( ! canTune() )
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
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		tuner.setInstrument((String) model.getData());

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		tuner.setTuning((String) model.getData());

		tuner.setCalculator(getCalculator());

		tuner.showTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}
	
	public void graphTuning(String title) throws Exception
	{
		InstrumentTuner tuner = getInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		tuner.setInstrument((String) model.getData());

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		tuner.setTuning((String) model.getData());

		tuner.setCalculator(getCalculator());

		tuner.plotTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}

	/**
	 * Optimize the currently-selected objective function
	 * @return XML string defining the optimized instrument, if optimization succeeds,
	 * or {@code null} if optimization fails.
	 */
	public String optimizeInstrument() throws Exception
	{
		BaseObjectiveFunction objective = getObjectiveFunction();
		BaseObjectiveFunction.OptimizerType optimizerType = objective.getOptimizerType();
		if ( preferredOptimizerType != null 
				&& ! optimizerType.equals(BaseObjectiveFunction.OptimizerType.BrentOptimizer))
		{
			optimizerType = preferredOptimizerType;
		}
		
		if ( ObjectiveFunctionOptimizer.optimizeObjectiveFunction(objective, optimizerType) )
		{
			Instrument instrument = objective.getInstrument();
			// Convert back to the input unit-of-measure values
			instrument.convertToLengthType();
			String xmlString = marshal(instrument);
			return xmlString;
		}
		return null;
	} // optimizeInstrument
	
	public void compareInstrument(String newName, Instrument newInstrument) throws Exception
	{
		Category category = getCategory(INSTRUMENT_CATEGORY_ID);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		String oldName = model.getName();
		if (oldName.equals(newName))
		{
			System.out.print("\nError: Current editor tab, ");
			System.out.print(newName);
			System.out.println(" is the same as the selected instrument.");
			System.out.println("Select the edit tab for a different instrument.");
			return;
		}
		Instrument oldInstrument = getInstrument();
		InstrumentComparisonTable table = new InstrumentComparisonTable("");
		table.buildTable(oldName, oldInstrument, newName, newInstrument);
		table.showTable(false);
	}
	
	protected String marshal(Instrument instrument) throws Exception
	{
		BindFactory binder = GeometryBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		binder.marshalToXml(instrument, writer);

		return writer.toString();
	}

	protected String getSelectedXmlString(String categoryName) throws Exception
	{
		String xmlString = null;

		Category category = getCategory(categoryName);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		xmlString = (String) model.getData();

		return xmlString;
	}

	protected Instrument getInstrument() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		String xmlString = getSelectedXmlString(INSTRUMENT_CATEGORY_ID);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(xmlString, true);
		instrument.updateComponents();
		return instrument;
	}

	protected Instrument getInstrument(String xmlString)
	{
		try
		{
			BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
			Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(xmlString, true);
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
	 * @param newPreferences
	 */
	public void setPreferences(Preferences newPreferences)
	{
		String optimizerPreference = newPreferences.get(
				OptimizationPreferences.OPTIMIZER_TYPE_OPT, OptimizationPreferences.OPT_DEFAULT_NAME);
		if ( optimizerPreference.contentEquals(OptimizationPreferences.OPT_DEFAULT_NAME) )
		{
			preferredOptimizerType = null;
		}
		else if ( optimizerPreference.contentEquals(OptimizationPreferences.OPT_POWELL_NAME) )
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.PowellOptimizer;
		}
		else if ( optimizerPreference.contentEquals(OptimizationPreferences.OPT_SIMPLEX_NAME) )
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.SimplexOptimizer;
		}
		else if ( optimizerPreference.contentEquals(OptimizationPreferences.OPT_BOBYQA_NAME) )
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer;
		}
		else if ( optimizerPreference.contentEquals(OptimizationPreferences.OPT_CMAES_NAME) )
		{
			preferredOptimizerType = BaseObjectiveFunction.OptimizerType.CMAESOptimizer;
		}
		else
		{
			preferredOptimizerType = null;
		}
	}

	// Methods to create objects that will perform this study,
	// according to components that the user has selected.

	/**
	 * Create the selected calculator, and set its physical parameters.
	 * @return created calculator.
	 */
	protected abstract InstrumentCalculator getCalculator();

	/**
	 * Create the instrument tuner appropriate for this study.
	 * @return created tuner.
	 */
	protected abstract InstrumentTuner getInstrumentTuner();

	/**
	 * Create the objective function to use for the selected optimization.
	 * set the physical parameters,
	 * and set any constraints that the user has selected.
	 * @return
	 * @throws Exception 
	 */
	protected abstract BaseObjectiveFunction getObjectiveFunction() throws Exception;
}
