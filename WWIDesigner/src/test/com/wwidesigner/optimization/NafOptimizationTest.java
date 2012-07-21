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
import com.wwidesigner.geometry.InstrumentConfigurator;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.calculation.SimpleFippleMouthpieceConfigurator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.SortedPositionList;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

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

	/**
	 * Complete workflow for optimizing an XML-defined instrument with the
	 * InstrumentOptimizer2 algorithm.
	 * 
	 * @return An Instrument object after optimization, with all dimensions in
	 *         the original units.
	 * @throws Exception
	 */
	public Instrument doInstrumentOptimization() throws Exception
	{
		Instrument instrument = getInstrumentFromXml();
		configureInstrument(instrument);

		Tuning tuning = getTuningFromXml();

		InstrumentOptimizer optimizer = new HolePositionAndDiameterOptimizer(
				instrument, tuning);
		optimizer.setBaseOptimizer(optimizerType, numberOfInterpolationPoints);
		setPhysicalParameters(optimizer);
		setOptimizationBounds(optimizer);
		optimizer.optimizeInstrument();

		// Convert back to the input unit-of-measure values
		instrument.convertToLengthType();

		// The optimizer modifies the input Instrument instance
		return instrument;
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
			numberOfInterpolationPoints = 10;

			Instrument optimizedInstrument = doInstrumentOptimization();

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
			lowerBound = new double[] { 0.20, 0.05, 0.3 };
			upperBound = new double[] { 0.4, 0.15, 0.4 };
			optimizerType = InstrumentOptimizer.OptimizerType.BOBYQAOptimizer;
			numberOfInterpolationPoints = 10;

			Instrument optimizedInstrument = doInstrumentOptimization();

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
			assertEquals("Hole 1 diameter incorrect", 0.363, sortedHoles.get(0)
					.getDiameter(), 0.01);
			
			// This hole position derives from the actual instrument AND 2 other
			// calculation algorithms.
			assertEquals("Hole 1 position incorrect", 7.85, sortedHoles.get(0)
					.getBorePosition(), 0.01);

			double distance = lastPoint.getBorePosition() - sortedHoles.get(0).getBorePosition();
			
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
			lowerBound = new double[] { 0.28, 0.01, 0.01, 0.01, 0.01, 0.01,
					0.05, 0.1, 0.15, 0.15, 0.15, 0.15, 0.15 };
			upperBound = new double[] { 0.5, 0.03, 0.03, 0.035, 0.035, 0.035,
					0.15, 0.5, 0.5, 0.5, 0.5, 0.5, 0.6 };
			optimizerType = InstrumentOptimizer.OptimizerType.BOBYQAOptimizer;
			numberOfInterpolationPoints = 60;

			Instrument optimizedInstrument = doInstrumentOptimization();

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 11.97,
					lastPoint.getBorePosition(), 0.05);

			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
					holes);

			assertEquals("Hole 1 diameter incorrect", 0.282, sortedHoles.get(0)
					.getDiameter(), 0.01);
			assertEquals("Hole 2 diameter incorrect", 0.453, sortedHoles.get(1)
					.getDiameter(), 0.01);
			assertEquals("Hole 3 diameter incorrect", 0.372, sortedHoles.get(2)
					.getDiameter(), 0.01);
			assertEquals("Hole 4 diameter incorrect", 0.351, sortedHoles.get(3)
					.getDiameter(), 0.01);
			assertEquals("Hole 5 diameter incorrect", 0.450, sortedHoles.get(4)
					.getDiameter(), 0.01);
			assertEquals("Hole 6 diameter incorrect", 0.378, sortedHoles.get(5)
					.getDiameter(), 0.01);

			assertEquals("Hole 1 position incorrect", 2.935, sortedHoles.get(0)
					.getBorePosition(), 0.01);
			assertEquals("Hole 2 position incorrect", 4.116, sortedHoles.get(1)
					.getBorePosition(), 0.01);
			assertEquals("Hole 3 position incorrect", 4.510, sortedHoles.get(2)
					.getBorePosition(), 0.01);
			assertEquals("Hole 4 position incorrect", 5.189, sortedHoles.get(3)
					.getBorePosition(), 0.01);
			assertEquals("Hole 5 position incorrect", 6.410, sortedHoles.get(4)
					.getBorePosition(), 0.01);
			assertEquals("Hole 6 position incorrect", 6.804, sortedHoles.get(5)
					.getBorePosition(), 0.01);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected Instrument getInstrumentFromXml()
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);

		return instrument;
	}

	protected void configureInstrument(Instrument instrument)
	{
		InstrumentConfigurator instrumentConfig = new SimpleFippleMouthpieceConfigurator();
		instrument.setConfiguration(instrumentConfig);

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
		PhysicalParameters parameters = new PhysicalParameters(22.22,
				TemperatureType.C);
		optimizer.setPhysicalParams(parameters);
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
	protected File getInputFile(String fileName, BindFactory bindFactory) throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
