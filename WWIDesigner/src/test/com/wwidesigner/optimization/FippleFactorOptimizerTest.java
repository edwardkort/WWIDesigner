/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.GordonCalculator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.InstrumentTuningTable;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class FippleFactorOptimizerTest
{
	protected String inputInstrumentXML;
	protected String inputTuningXML;
	protected double[] lowerBound;
	protected double[] upperBound;
	protected InstrumentOptimizer.OptimizerType optimizerType;
	protected int numberOfInterpolationPoints;

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
		PhysicalParameters params = new PhysicalParameters(22.22,
				TemperatureType.C);
		Instrument instrument = getInstrumentFromXml();
		InstrumentCalculator calculator = new GordonCalculator(instrument,
				params);
		instrument.convertToMetres();

		Tuning tuning = getTuningFromXml();

		InstrumentOptimizer optimizer = new FippleFactorOptimizer(instrument,
				calculator, tuning);
		optimizer.setBaseOptimizer(optimizerType, numberOfInterpolationPoints);
		setOptimizationBounds(optimizer);

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

	public void showTuning(Instrument instrument,
			InstrumentCalculator calculator, Tuning tuning, String title)
	{
		double maxFreqRatio = 2.;
		// set accuracy to 0.1 cents
		int numberOfFrequencies = (int) (10. * InstrumentTuningTable
				.getCents(maxFreqRatio));

		InstrumentTuningTable table = new InstrumentTuningTable(title);
		// instrument.updateComponents();

		for (Fingering fingering : tuning.getFingering())
		{
			Double playedFrequency = calculator.getPlayedFrequency(fingering,
					maxFreqRatio, numberOfFrequencies);
			table.addTuning(fingering, playedFrequency);
		}

		table.showTuning();
	}

	@Test
	public final void testNoHoleOptimization()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/NoHoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml";
			lowerBound = new double[] { 0.2 };
			upperBound = new double[] { 1.5 };
			optimizerType = InstrumentOptimizer.OptimizerType.CMAESOptimizer;
			numberOfInterpolationPoints = 2;

			Instrument optimizedInstrument = doInstrumentOptimization("No-hole");

			// Test fipple factor
			double fippleFactor = optimizedInstrument.getMouthpiece()
					.getFipple().getFippleFactor();
			assertEquals("Fipple factor incorrect", 0.80, fippleFactor, 0.01);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
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

	public static void main(String[] args)
	{
		FippleFactorOptimizerTest test = new FippleFactorOptimizerTest();
		test.testNoHoleOptimization();
	}
}
