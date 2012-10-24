/**
 * 
 */
package com.wwidesigner.gui;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.optimization.run.BaseOptimizationRunner;
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
	public static final String OPTIMIZER_CATEGORY_ID = "Optimizer";
	public static final String CONSTRAINT_CATEGORY_ID = "Constraint set";

	protected List<Category> categories;
	
	protected PhysicalParameters params;

	public StudyModel()
	{
		setCategories();
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

		Category calculatorCategory = getCategory(CALCULATOR_CATEGORY_ID);
		String calculatorSelected = calculatorCategory.getSelectedSub();

		return tuningSelected != null && instrumentSelected != null
				&& calculatorSelected != null;
	}

	public boolean canOptimize()
	{
		Category category = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizerSelected = category.getSelectedSub();

		category = getCategory(CONSTRAINT_CATEGORY_ID);
		String constraintsSelected = category.getSelectedSub();

		return optimizerSelected != null && constraintsSelected != null
				&& canTune();
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

	public String optimizeInstrument() throws Exception
	{
		BaseOptimizationRunner runner = getOptimizationRunner();

		String xmlString = getSelectedXmlString(INSTRUMENT_CATEGORY_ID);
		runner.setInputInstrumentXML(xmlString, false);

		xmlString = getSelectedXmlString(TUNING_CATEGORY_ID);
		runner.setInputTuningXML(xmlString, false);

		Instrument instrument = runner.doInstrumentOptimization(null);
		xmlString = marshal(instrument);

		return xmlString;
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

	public PhysicalParameters getParams()
	{
		return params;
	}

	public void setParams(PhysicalParameters params)
	{
		this.params = params;
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
	 * Create the selected optimization runner,
	 * set the physical parameters,
	 * and set any constraints that the user has selected.
	 * @return
	 */
	protected abstract BaseOptimizationRunner getOptimizationRunner();
}
