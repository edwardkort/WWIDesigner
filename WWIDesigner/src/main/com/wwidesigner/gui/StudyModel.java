/**
 * 
 */
package com.wwidesigner.gui;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.optimization.BaseMultivariateSimpleBoundsOptimizer;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.MultivariateOptimizer;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;
import org.apache.commons.math3.optimization.direct.CMAESOptimizer;
import org.apache.commons.math3.optimization.direct.PowellOptimizer;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.InstrumentTuner;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.optimization.BaseObjectiveFunction;
import com.wwidesigner.optimization.multistart.MultivariateMultiStartBoundsOptimizer;
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
	
	protected static void printErrors(String description, double errorNorm, double[] errorVector)
	{
		boolean firstPass = true;
		System.out.print(description);
		System.out.print(errorNorm);
		System.out.print(" from [");
		for (double err: errorVector)
		{
			if (! firstPass)
			{
				System.out.print(",  ");
			}
			else
			{
				firstPass = false;
			}
			System.out.print(err);
		}
		System.out.println("].");
	}

	public String optimizeInstrument() throws Exception
	{
		BaseObjectiveFunction objective = getObjectiveFunction();
		
		double[] startPoint = objective.getGeometryPoint();
		double[] errorVector = objective.getErrorVector(startPoint);
		
		// Ensure startPoint is within bounds.
		for (int i = 0; i < startPoint.length; i++)
		{
			if ( startPoint[i] < objective.getLowerBounds()[i] )
			{
				startPoint[i] = objective.getLowerBounds()[i];
			}
			else if ( startPoint[i] > objective.getUpperBounds()[i] )
			{
				startPoint[i] = objective.getUpperBounds()[i];
			} 
		}
		double initialNorm = objective.calcNorm(errorVector);
		System.out.println();
		printErrors("Initial error: ", initialNorm, errorVector);
		
		try
		{
			if ( objective.getOptimizerType() == BaseObjectiveFunction.OptimizerType.BrentOptimizer )
			{
				// Univariate optimization.
				BrentOptimizer optimizer = new BrentOptimizer(0.00001, 0.00001);
				UnivariatePointValuePair  outcome;
				outcome = optimizer.optimize(objective.getMaxIterations(),objective,GoalType.MINIMIZE,
						objective.getLowerBounds()[0], objective.getUpperBounds()[0],
						startPoint[0]);
				double[] geometry = new double[1];
				geometry[0] = outcome.getPoint();
				objective.setGeometryPoint(geometry);
			}
			else if ( objective.getOptimizerType() == BaseObjectiveFunction.OptimizerType.PowellOptimizer )
			{
				// Multivariate optimization, without bounds.
				PowellOptimizer optimizer = new PowellOptimizer(0.00001, 0.00001);
				PointValuePair  outcome;
				outcome = optimizer.optimize(objective.getMaxIterations(),objective,GoalType.MINIMIZE,
						startPoint);
				objective.setGeometryPoint(outcome.getPoint());
			}
			else {
				// Multivariate optimization, with bounds.
				BaseMultivariateSimpleBoundsOptimizer<MultivariateFunction> optimizer;
				PointValuePair  outcome;
				if ( objective.getOptimizerType() == BaseObjectiveFunction.OptimizerType.CMAESOptimizer )
				{
					optimizer = new CMAESOptimizer(objective.getNrInterpolations());
				}
				else {
					optimizer = new BOBYQAOptimizer(objective.getNrInterpolations());
				}
				if ( objective.isMultiStart())
				{
					MultivariateOptimizer baseOptimizer = (MultivariateOptimizer) optimizer;
					optimizer = new MultivariateMultiStartBoundsOptimizer(
							baseOptimizer, objective.getRangeProcessor().getNumberOfStarts(),
							objective.getRangeProcessor());
				}
				outcome = optimizer.optimize(objective.getMaxIterations(),objective,GoalType.MINIMIZE,
						startPoint, objective.getLowerBounds(), objective.getUpperBounds());
				objective.setGeometryPoint(outcome.getPoint());
			}
		}
		catch (TooManyEvaluationsException e)
		{
			System.out.println("Exception: " + e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.print("Performed ");
		System.out.print(objective.getEvaluationsDone());
		System.out.print(" error evaluations in ");
		System.out.print(objective.getIterationsDone());
		System.out.println(" iterations.");
		errorVector = objective.getErrorVector(objective.getGeometryPoint());
		double finalNorm = objective.calcNorm(errorVector);
		printErrors("Final error:  ", finalNorm, errorVector);
		System.out.print("Residual error ratio: ");
		System.out.println(finalNorm/initialNorm);

		Instrument instrument = objective.getInstrument();
		// Convert back to the input unit-of-measure values
		instrument.convertToLengthType();
		String xmlString = marshal(instrument);

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

	protected Instrument getInstrument() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		String xmlString = getSelectedXmlString(INSTRUMENT_CATEGORY_ID);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(xmlString, true);
		instrument.updateComponents();
		return instrument;
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
