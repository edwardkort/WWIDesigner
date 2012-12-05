/**
 * 
 */
package com.wwidesigner.optimization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.modelling.TuningComparisonTable;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class AbstractOptimizationTest
{
	protected String inputInstrumentXML;
	protected String inputTuningXML;
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
	public void setInputInstrumentXML(String inputInstrumentXML)
	{
		this.inputInstrumentXML = inputInstrumentXML;
	}

	/**
	 * @param inputTuningXML
	 *            the inputTuningXML to set
	 */
	public void setInputTuningXML(String inputTuningXML)
	{
		this.inputTuningXML = inputTuningXML;
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

		showTuning(instrument, calculator, tuning, title
				+ ", before optimization");

		optimizer.optimizeInstrument();
		showTuning(instrument, calculator, tuning, title
				+ ", after optimization");

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
		// Override this in a test with a custom optimizer that has setup
		// methods not in the InstrumentOptimizerInterface.
	}

	public void showTuning(Instrument instrument,
			InstrumentCalculator calculator, Tuning tuning, String title)
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
		tuner.setInstrument(instrument);
		tuner.setCalculator(calculator);
		tuner.setTuning(tuning);
		Tuning predicted = tuner.getPredictedTuning();
		TuningComparisonTable table = new TuningComparisonTable(title);
		table.buildTable(tuning, predicted);
		table.showTuning();
	}

	protected Instrument getInstrumentFromXml() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
	}

	protected Tuning getTuningFromXml() throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(inputTuningXML, noteBindFactory);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

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
