/**
 * 
 */
package com.wwidesigner.optimization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.modelling.TuningComparisonTable;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class AbstractOptimizationTest
{
	// Basic variables that derived class should initialize
	// before calling base setup() function.
	protected InstrumentCalculator calculator;
	protected String inputInstrumentXML;
	protected String inputTuningXML;
	protected PhysicalParameters params;
	
	// Intermediate values that base setup() function initializes.
	protected Instrument instrument;
	protected Tuning tuning;

	// Intermediate variables, to build objective function for optimization
	protected double[] lowerBound;
	protected double[] upperBound;
	protected EvaluatorInterface evaluator;
	protected BaseObjectiveFunction objective;
	
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
	 * @param calculator
	 *            the calculator to set
	 */
	public void setCalculator(InstrumentCalculator calculator)
	{
		this.calculator = calculator;
	}

	protected void setup() throws Exception
	{
		instrument = getInstrumentFromXml();
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);
		instrument.convertToMetres();
		tuning = getTuningFromXml();
	}

	/**
	 * Complete workflow for optimizing an XML-defined instrument with the
	 * current objective function.
	 * 
	 * @return An Instrument object after optimization, with all dimensions in
	 *         the original units.
	 * @throws Exception
	 */
	public Instrument doInstrumentOptimization(String title) throws Exception
	{
		objective.setLowerBounds(lowerBound);
		objective.setUpperBounds(upperBound);

		showTuning(instrument, calculator, tuning, title
				+ ", before optimization");
		
		ObjectiveFunctionOptimizer.optimizeObjectiveFunction(objective, objective.getOptimizerType());

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

	protected File getInputFile(String fileName, BindFactory bindFactory)
			throws FileNotFoundException
	{
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}

}
