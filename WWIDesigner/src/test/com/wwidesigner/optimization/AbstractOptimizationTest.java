/**
 * 
 */
package com.wwidesigner.optimization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.gui.util.InstrumentTypeException;
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
	 * @param aParams
	 *            the params to set
	 */
	public void setParams(PhysicalParameters aParams)
	{
		this.params = aParams;
	}

	/**
	 * @param aInputInstrumentXML
	 *            the inputInstrumentXML to set
	 */
	public void setInputInstrumentXML(String aInputInstrumentXML)
	{
		this.inputInstrumentXML = aInputInstrumentXML;
	}

	/**
	 * @param aInputTuningXML
	 *            the inputTuningXML to set
	 */
	public void setInputTuningXML(String aInputTuningXML)
	{
		this.inputTuningXML = aInputTuningXML;
	}

	/**
	 * @param aLowerBound
	 *            the lowerBound to set
	 */
	public void setLowerBound(double[] aLowerBound)
	{
		this.lowerBound = aLowerBound;
	}

	/**
	 * @param aUpperBound
	 *            the upperBound to set
	 */
	public void setUpperBound(double[] aUpperBound)
	{
		this.upperBound = aUpperBound;
	}

	/**
	 * @param aCalculator
	 *            the calculator to set
	 */
	public void setCalculator(InstrumentCalculator aCalculator)
	{
		this.calculator = aCalculator;
	}

	protected void setup() throws Exception
	{
		instrument = getInstrumentFromXml();
		if (! calculator.isCompatible(instrument))
		{
			throw new InstrumentTypeException("The instrument in " + inputInstrumentXML
					+ " is not compatible with this test.");
		}
		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);
		instrument.convertToMetres();
		tuning = getTuningFromXml();
		lowerBound = new double[0];
		upperBound = new double[0];
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

	public void showTuning(Instrument aInstrument,
			InstrumentCalculator aCalculator, Tuning aTuning, String title)
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();
		tuner.setInstrument(aInstrument);
		tuner.setCalculator(aCalculator);
		tuner.setTuning(aTuning);
		Tuning predicted = tuner.getPredictedTuning();
		TuningComparisonTable table = new TuningComparisonTable(title);
		table.buildTable(aTuning, predicted);
		table.showTuning();
	}

	protected Instrument getInstrumentFromXml() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument thisInstrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		thisInstrument.updateComponents();

		return thisInstrument;
	}

	protected Tuning getTuningFromXml() throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(inputTuningXML, noteBindFactory);
		Tuning thisTuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return thisTuning;
	}

	protected File getInputFile(String fileName, BindFactory bindFactory)
			throws FileNotFoundException
	{
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}

	protected static double getTopHolePosition(Instrument instrument)
	{
		PositionInterface[] sortedHoles = Instrument.sortList(instrument
				.getHole());
		Hole topHole = (Hole) sortedHoles[0];
		double holePosition = topHole.getBorePosition();

		return holePosition;
	}

	protected static double getBoreLength(Instrument instrument)
	{
		List<BorePoint> borePoints = instrument.getBorePoint();
		PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
		PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
		double boreLength = lastPoint.getBorePosition();

		return boreLength;
	}

}
