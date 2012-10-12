/**
 * 
 */
package com.wwidesigner.optimization.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.optimization.InstrumentOptimizer;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class BaseOptimizationRunner
{
	protected String inputInstrumentXML;
	protected boolean isInstrumentXmlFileName;
	protected String inputTuningXML;
	protected boolean isTuningXmlFileName;
	protected double[] lowerBound;
	protected double[] upperBound;
	protected InstrumentOptimizer.OptimizerType optimizerType;
	protected int numberOfInterpolationPoints;
	protected PhysicalParameters params;
	protected InstrumentCalculator calculator;
	protected Class<? extends InstrumentOptimizer> optimizerClass;
	protected InstrumentOptimizer optimizer;
	protected Instrument instrument;
	protected Tuning tuning;

	public void setOptimizerClass(
			Class<? extends InstrumentOptimizer> optimizerClass)
	{
		this.optimizerClass = optimizerClass;
	}

	public InstrumentOptimizer setInstrumentOptimizer() throws Exception
	{
		Constructor<? extends InstrumentOptimizer> optConstructor = optimizerClass
				.getConstructor(new Class[] { Instrument.class,
						InstrumentCalculator.class, TuningInterface.class });
		InstrumentOptimizer opt = optConstructor.newInstance(instrument,
				calculator, tuning);

		return opt;
	}

	/**
	 * @param params
	 *            the params to set
	 */
	public void setParams(PhysicalParameters params)
	{
		this.params = params;
	}

	/**
	 * @param inputInstrumentXML
	 *            the inputInstrumentXML to set
	 */
	public void setInputInstrumentXML(String inputInstrumentXML,
			boolean isFileName)
	{
		this.inputInstrumentXML = inputInstrumentXML;
		isInstrumentXmlFileName = isFileName;
	}

	/**
	 * @param inputTuningXML
	 *            the inputTuningXML to set
	 */
	public void setInputTuningXML(String inputTuningXML, boolean isFileName)
	{
		this.inputTuningXML = inputTuningXML;
		isTuningXmlFileName = isFileName;
	}

	/**
	 * @param lowerBound
	 *            the lowerBound to set
	 */
	public void setLowerBound(double[] lowerBound)
	{
		this.lowerBound = lowerBound;
	}

	/**
	 * @param upperBound
	 *            the upperBound to set
	 */
	public void setUpperBound(double[] upperBound)
	{
		this.upperBound = upperBound;
	}

	/**
	 * @param optimizerType
	 *            the optimizerType to set
	 */
	public void setOptimizerType(InstrumentOptimizer.OptimizerType optimizerType)
	{
		this.optimizerType = optimizerType;
	}

	/**
	 * @param numberOfInterpolationPoints
	 *            the numberOfInterpolationPoints to set
	 */
	public void setNumberOfInterpolationPoints(int numberOfInterpolationPoints)
	{
		this.numberOfInterpolationPoints = numberOfInterpolationPoints;
	}

	/**
	 * @param calculator
	 *            the calculator to set
	 */
	public void setCalculator(InstrumentCalculator calculator)
	{
		this.calculator = calculator;
	}

	/**
	 * Complete workflow for optimizing an XML-defined instrument with the
	 * InstrumentOptimizer2 algorithm.
	 * 
	 * @return An Instrument object after optimization, with all dimensions in
	 *         the original units.
	 * @throws Exception
	 */
	public Instrument doInstrumentOptimization(String title) throws Exception
	{
		instrument = getInstrumentFromXml();
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);
		instrument.convertToMetres();

		tuning = getTuningFromXml();

		optimizer = setInstrumentOptimizer();
		optimizer.setBaseOptimizer(optimizerType, numberOfInterpolationPoints);
		setOptimizationBounds(optimizer);
		setupCustomOptimizer();

		optimizer.optimizeInstrument();

		// Convert back to the input unit-of-measure values
		instrument.convertToLengthType();

		BindFactory bindFactory = GeometryBindFactory.getInstance();
		bindFactory
				.marshalToXml(instrument, new OutputStreamWriter(System.out));

		// The optimizer modifies the input Instrument instance
		return instrument;
	}

	protected void setupCustomOptimizer() throws Exception
	{
		// Override this in a runner with a custom optimizer that has setup
		// methods not in the InstrumentOptimizerInterface.
	}

	protected Instrument getInstrumentFromXml() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		Instrument instrument = null;

		if (isInstrumentXmlFileName)
		{
			File inputFile = getInputFile(inputInstrumentXML,
					geometryBindFactory);
			instrument = (Instrument) geometryBindFactory.unmarshalXml(
					inputFile, true);
		}
		else
		{
			instrument = (Instrument) geometryBindFactory.unmarshalXml(
					inputInstrumentXML, true);
		}

		instrument.updateComponents();

		return instrument;
	}

	protected Tuning getTuningFromXml() throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		Tuning tuning = null;

		if (isTuningXmlFileName)
		{
			File inputFile = getInputFile(inputTuningXML, noteBindFactory);
			tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);
		}
		else
		{
			tuning = (Tuning) noteBindFactory
					.unmarshalXml(inputTuningXML, true);
		}

		return tuning;
	}

	protected void setOptimizationBounds(InstrumentOptimizer optimizer)
	{
		optimizer.setLowerBnd(lowerBound);
		optimizer.setUpperBnd(upperBound);
	}

	protected File getInputFile(String fileName, BindFactory bindFactory)
			throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}

}
