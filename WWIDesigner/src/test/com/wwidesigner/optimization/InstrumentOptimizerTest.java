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
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.SimpleTestCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class InstrumentOptimizerTest
{
	protected String inputInstrumentXML = "com/wwidesigner/optimization/example/cylinderWith6Holes.xml";
	protected String inputTuningXML = "com/wwidesigner/optimization/example/6HoleTuning.xml";

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
		Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
		InstrumentCalculator calculator = new SimpleTestCalculator(instrument);
		configureInstrument(instrument);

		Tuning tuning = getTuningFromXml(inputTuningXML);

		InstrumentOptimizer optimizer = new HolePositionOptimizer(
				instrument, calculator, tuning);
		setPhysicalParameters(optimizer);
		setOptimizationBounds(optimizer);
		optimizer.optimizeInstrument();

		// Convert back to the input unit-of-measure values
		instrument.convertToLengthType();

		// The optimizer modifies the input Instrument instance
		return instrument;
	}

	@Test
	public final void testInstrumentOptimization()
	{
		try
		{
			Instrument optimizedInstrument = doInstrumentOptimization();
			
			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect", 377.10, lastPoint.getBorePosition(), 0.01);
			
			// Test hole positions
			List<Hole> holes = optimizedInstrument.getHole();
			PositionInterface[] sortedHoles = Instrument.sortList(holes);
			assertEquals("Hole 1 position incorrect", 188.32, sortedHoles[0].getBorePosition(), 0.01);
			assertEquals("Hole 2 position incorrect", 212.60, sortedHoles[1].getBorePosition(), 0.01);
			assertEquals("Hole 3 position incorrect", 238.91, sortedHoles[2].getBorePosition(), 0.01);
			assertEquals("Hole 4 position incorrect", 272.14, sortedHoles[3].getBorePosition(), 0.01);
			assertEquals("Hole 5 position incorrect", 283.72, sortedHoles[4].getBorePosition(), 0.01);
			assertEquals("Hole 6 position incorrect", 317.46, sortedHoles[5].getBorePosition(), 0.01);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected Instrument getInstrumentFromXml(String instrumentXML)
			throws Exception
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

	protected Tuning getTuningFromXml(String tuningXML) throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(inputTuningXML, noteBindFactory);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return tuning;
	}

	protected void setPhysicalParameters(InstrumentOptimizer optimizer)
	{
		PhysicalParameters parameters = new PhysicalParameters(25.,
				TemperatureType.C);
		optimizer.setPhysicalParams(parameters);
	}

	protected void setOptimizationBounds(InstrumentOptimizer optimizer)
	{
		double[] lB = new double[7];
		double[] uB = new double[7];

		lB[0] = 0.25;
		uB[0] = 0.40;
		lB[1] = 0.01;
		uB[1] = 0.035;
		lB[2] = 0.01;
		uB[2] = 0.035;
		lB[3] = 0.01;
		uB[3] = 0.035;
		lB[4] = 0.01;
		uB[4] = 0.035;
		lB[5] = 0.01;
		uB[5] = 0.05;
		lB[6] = 0.01;
		uB[6] = 0.08;

		optimizer.setLowerBnd(lB);
		optimizer.setUpperBnd(uB);
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
