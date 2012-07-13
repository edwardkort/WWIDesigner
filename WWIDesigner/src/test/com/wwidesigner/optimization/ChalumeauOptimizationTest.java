/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.InstrumentConfigurator;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.calculation.SimpleTestConfigurator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class ChalumeauOptimizationTest
{
	protected String inputInstrumentXML = "com/wwidesigner/optimization/example/chalumeau_alto.xml";
	protected String inputTuningXML = "com/wwidesigner/optimization/example/chalumeau_alto_tuning.xml";

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
		configureInstrument(instrument);

		Tuning tuning = getTuningFromXml(inputTuningXML);

		HolePositionAndDiameterOptimizer optimizer =
				new HolePositionAndDiameterOptimizer(instrument, tuning);

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
			
			System.out.print("last point = " + lastPoint.getBorePosition());
			//assertEquals("Bore length incorrect", 379.54, lastPoint.getBorePosition(), 0.01);
			
			// Test hole positions
			//List<com.wwidesigner.geometry.Hole> holes = optimizedInstrument.getHole();
			//PositionInterface[] sortedHoles = Instrument.sortList(holes);
			//assertEquals("Hole 1 position incorrect", 188.98, sortedHoles[0].getBorePosition(), 0.01);
			//assertEquals("Hole 2 position incorrect", 213.44, sortedHoles[1].getBorePosition(), 0.01);
			//assertEquals("Hole 3 position incorrect", 239.96, sortedHoles[2].getBorePosition(), 0.01);
			//assertEquals("Hole 4 position incorrect", 273.49, sortedHoles[3].getBorePosition(), 0.01);
			//assertEquals("Hole 5 position incorrect", 285.17, sortedHoles[4].getBorePosition(), 0.01);
			//assertEquals("Hole 6 position incorrect", 319.24, sortedHoles[5].getBorePosition(), 0.01);
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

		return instrument;
	}

	protected void configureInstrument(Instrument instrument)
	{
		InstrumentConfigurator instrumentConfig = new SimpleTestConfigurator();
		instrument.setConfiguration(instrumentConfig);

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
		double[] lB = new double[21]; // lower bound
		double[] uB = new double[21]; // upper bound

		lB[0] = 0.15;
		uB[0] = 0.30;
		lB[1] = 0.000;
		uB[1] = 0.020;
		lB[2] = 0.000;
		uB[2] = 0.050;
		lB[3] = 0.000;
		uB[3] = 0.050;
		lB[4] = 0.005;
		uB[4] = 0.050;
		lB[5] = 0.005;
		uB[5] = 0.050;
		lB[6] = 0.005;
		uB[6] = 0.100;
		lB[7] = 0.005;
		uB[7] = 0.050;
		lB[8] = 0.005;
		uB[8] = 0.050;
		lB[9] = 0.005;
		uB[9] = 0.100;
		lB[10] = 0.005;
		uB[10] = 0.100;

		lB[11] = 0.2;
		uB[11] = 0.5;
		lB[12] = 0.2;
		uB[12] = 0.5;
		lB[13] = 0.2;
		uB[13] = 0.5;
		lB[14] = 0.2;
		uB[14] = 0.5;
		lB[15] = 0.2;
		uB[15] = 0.5;
		lB[16] = 0.2;
		uB[16] = 0.5;
		lB[17] = 0.2;
		uB[17] = 0.5;
		lB[18] = 0.2;
		uB[18] = 0.5;
		lB[19] = 0.2;
		uB[19] = 0.5;		          
		lB[20] = 0.2;
		uB[20] = 0.5;
		          
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
	 */
	protected File getInputFile(String fileName, BindFactory bindFactory)
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
