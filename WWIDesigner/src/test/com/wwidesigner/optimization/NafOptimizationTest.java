/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
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
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 * 
 */
public class NafOptimizationTest
{
	protected String inputInstrumentXML;
	protected String inputTuningXML;
	protected double[] lowerBound;
	protected double[] upperBound;
	protected InstrumentOptimizer.OptimizerType optimizerType;
	protected int numberOfInterpolationPoints;
	protected PhysicalParameters params;

	// Set this to true to use SimpleHolePositionAndDiameterOptimizer and the
	// associated bounds.
	// Set this to false to use HolePositionAndDiameterOptimizer and the
	// associated bounds.
	protected static boolean useSimpleOptimizer = false;

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
		Instrument instrument = getInstrumentFromXml();
		InstrumentCalculator calculator = new GordonCalculator(instrument);
		configureInstrument(instrument);

		Tuning tuning = getTuningFromXml();

		InstrumentOptimizer optimizer;
		if (useSimpleOptimizer)
		{
			optimizer = new SimpleHolePositionAndDiameterOptimizer(instrument,
					calculator, tuning);
		}
		else
		{
			optimizer = new HolePositionAndDiameterOptimizer(instrument, 
					calculator, tuning);
		}
		// InstrumentOptimizer optimizer = new
		// TuningHolePositionAndDiameterOptimizer(
		// instrument, tuning);
		optimizer.setBaseOptimizer(optimizerType, numberOfInterpolationPoints);
		setPhysicalParameters(optimizer);
		setOptimizationBounds(optimizer);

		showTuning(instrument, calculator, tuning, title + ", before optimization");

		optimizer.optimizeInstrument();

		// Convert back to the input unit-of-measure values
		instrument.convertToLengthType();

		showTuning(instrument, calculator, tuning, title + ", after optimization");

		// The optimizer modifies the input Instrument instance
		return instrument;
	}

	public void showTuning(Instrument instrument, InstrumentCalculator calculator,
			Tuning tuning, String title)
	{
		double maxFreqRatio = 1.3;
		// set accuracy to 0.1 cents
		int numberOfFrequencies = (int) (10. * InstrumentTuningTable
				.getCents(maxFreqRatio));

		InstrumentTuningTable table = new InstrumentTuningTable(title);
		instrument.updateComponents();

		for (Fingering fingering : tuning.getFingering())
		{
			Double playedFrequency = calculator.getPlayedFrequency(fingering,
					maxFreqRatio, numberOfFrequencies, params);
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
			lowerBound = new double[] { 0.25 };
			upperBound = new double[] { 0.4 };
			optimizerType = InstrumentOptimizer.OptimizerType.CMAESOptimizer;
			numberOfInterpolationPoints = 2;

			Instrument optimizedInstrument = doInstrumentOptimization("No-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.97,
					lastPoint.getBorePosition(), 0.01);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test1HoleOptimization()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/1HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			if (useSimpleOptimizer)
			{
				lowerBound = new double[] { 0.20, 0.1, 0.005 };
				upperBound = new double[] { 0.4, 0.3, 0.02 };
			}
			else
			{
				lowerBound = new double[] { 0.20, 0.05, 0.3 };
				upperBound = new double[] { 0.4, 0.15, 0.4 };
			}
			optimizerType = InstrumentOptimizer.OptimizerType.BOBYQAOptimizer;
			numberOfInterpolationPoints = 6;

			Instrument optimizedInstrument = doInstrumentOptimization("One-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.97,
					lastPoint.getBorePosition(), 0.05);

			// Test hole positions
			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
					holes);

			// This hole diameter is set based on the optimizer's return value:
			// an
			// infinite number of position/hole diameter values are possible.
			assertEquals("Hole 1 diameter incorrect", 0.42, sortedHoles.get(0)
					.getDiameter(), 0.015); // 0.398

			// This hole position derives from the actual instrument AND 2 other
			// calculation algorithms.
			assertEquals("Hole 1 position incorrect", 8.0, sortedHoles.get(0)
					.getBorePosition(), 0.1); // 8.1

			double distance = lastPoint.getBorePosition()
					- sortedHoles.get(0).getBorePosition();

			System.out.println("didtance " + distance);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test6HoleOptimization()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/6HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/6HoleNAF1Tuning.xml";
			if (useSimpleOptimizer)
			{
				lowerBound = new double[] { 0.25, 0.03, 0.04, 0.06, 0.075,
						0.09, 0.1, 0.003, 0.003, 0.003, 0.003, 0.003, 0.003 };
				upperBound = new double[] { 0.5, 0.15, 0.18, 0.21, 0.24, 0.27,
						0.3, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02 };
			}
			else
			{
				lowerBound = new double[] { 0.28, 0.01, 0.01, 0.01, 0.01, 0.01,
						0.05, 0.1, 0.15, 0.15, 0.15, 0.15, 0.15 };
				upperBound = new double[] { 0.5, 0.03, 0.03, 0.035, 0.035,
						0.035, 0.15, 0.5, 0.5, 0.5, 0.5, 0.5, 0.6 };
			}
			optimizerType = InstrumentOptimizer.OptimizerType.BOBYQAOptimizer;
			numberOfInterpolationPoints = 26;

			Instrument optimizedInstrument = doInstrumentOptimization("Six-hole");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.97,
					lastPoint.getBorePosition(), 0.05);

			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
					holes);

			assertEquals("Hole 1 diameter incorrect", 0.29, sortedHoles.get(0)
					.getDiameter(), 0.1); // 0.308
			assertEquals("Hole 2 diameter incorrect", 0.43, sortedHoles.get(1)
					.getDiameter(), 0.1); // 0.361
			assertEquals("Hole 3 diameter incorrect", 0.36, sortedHoles.get(2)
					.getDiameter(), 0.1); // 0.352
			assertEquals("Hole 4 diameter incorrect", 0.35, sortedHoles.get(3)
					.getDiameter(), 0.1); // 0.357
			assertEquals("Hole 5 diameter incorrect", 0.45, sortedHoles.get(4)
					.getDiameter(), 0.1); // 0.379
			assertEquals("Hole 6 diameter incorrect", 0.38, sortedHoles.get(5)
					.getDiameter(), 0.1); // 0.398

			assertEquals("Hole 1 position incorrect", 3.10, sortedHoles.get(0)
					.getBorePosition(), 0.5); // 3.15
			assertEquals("Hole 2 position incorrect", 4.28, sortedHoles.get(1)
					.getBorePosition(), 0.5); // 4.0
			assertEquals("Hole 3 position incorrect", 4.68, sortedHoles.get(2)
					.getBorePosition(), 0.5); // 4.85
			assertEquals("Hole 4 position incorrect", 5.54, sortedHoles.get(3)
					.getBorePosition(), 0.5); // 5.9
			assertEquals("Hole 5 position incorrect", 6.92, sortedHoles.get(4)
					.getBorePosition(), 0.5); // 7.0
			assertEquals("Hole 6 position incorrect", 7.34, sortedHoles.get(5)
					.getBorePosition(), 0.5); // 8.1

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

	protected void configureInstrument(Instrument instrument) throws Exception
	{
		// This unit-of-measure converter is called in setConfiguration(), but
		// is shown here to make it explicit. The method is efficient: it does
		// not redo the work.
		instrument.convertToMetres();
	}

	protected Tuning getTuningFromXml() throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(inputTuningXML, noteBindFactory);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return tuning;
	}

	protected void setPhysicalParameters(InstrumentOptimizer optimizer)
	{
		this.params = new PhysicalParameters(20.5, TemperatureType.C);
		optimizer.setPhysicalParams(params);
	}

	protected void setOptimizationBounds(InstrumentOptimizer optimizer)
	{
		optimizer.setLowerBnd(lowerBound);
		optimizer.setUpperBnd(upperBound);
	}

	/**
	 * This approach for get the input File is based on finding it in the
	 * classpath. The actual application will use an explicit file path - this
	 * approach will be unnecessary.
	 * 
	 * @param fileName
	 *            expressed as a package path.
	 * @param bindFactory
	 *            that manages the elements in the file.
	 * @return A file representation of the fileName, as found somewhere in the
	 *         classpath.
	 * @throws FileNotFoundException
	 */
	protected File getInputFile(String fileName, BindFactory bindFactory)
			throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}

	public static void main(String[] args)
	{
		NafOptimizationTest test = new NafOptimizationTest();
		test.testNoHoleOptimization();
//		test.test1HoleOptimization();
//		test.test6HoleOptimization();
	}
}
