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
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.SimpleReedCalculator;
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
		InstrumentCalculator calculator = new SimpleReedCalculator(instrument);
		instrument.convertToMetres();

		Tuning tuning = getTuningFromXml(inputTuningXML);

		HolePositionAndDiameterOptimizer optimizer =
				new HolePositionAndDiameterOptimizer(instrument, calculator, tuning);

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
			SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(borePoints);
			BorePoint lastPoint = sortedPoints.getLast();
			
			// Test hole positions
			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(holes);
			
			System.out.println("Hole position and diameter:");
			for (int i = 0; i < sortedHoles.size(); ++i)
			{
				System.out.print( sortedHoles.get(i).getBorePosition() );
				System.out.print("  ");
				System.out.println( sortedHoles.get(i).getDiameter() );
			}

			//System.out.print("last point = " + lastPoint.getBorePosition());
			assertEquals("Bore length incorrect", 337.5, lastPoint.getBorePosition(), 0.1);
			
			assertEquals("Hole 1 position incorrect", 111.2, sortedHoles.get(0).getBorePosition(), 0.1);
			assertEquals("Hole 2 position incorrect", 111.9, sortedHoles.get(1).getBorePosition(), 0.1);
			assertEquals("Hole 3 position incorrect", 127.0, sortedHoles.get(2).getBorePosition(), 0.1);
			assertEquals("Hole 4 position incorrect", 139.8, sortedHoles.get(3).getBorePosition(), 0.1);
			assertEquals("Hole 5 position incorrect", 159.0, sortedHoles.get(4).getBorePosition(), 0.1);
			assertEquals("Hole 6 position incorrect", 184.7, sortedHoles.get(5).getBorePosition(), 0.1);
			assertEquals("Hole 7 position incorrect", 205.2, sortedHoles.get(6).getBorePosition(), 0.1);
			assertEquals("Hole 8 position incorrect", 225.2, sortedHoles.get(7).getBorePosition(), 0.1);
			assertEquals("Hole 9 position incorrect", 245.3, sortedHoles.get(8).getBorePosition(), 0.1);
			assertEquals("Hole 10 position incorrect", 273.9, sortedHoles.get(9).getBorePosition(), 0.1);
			
			assertEquals("Hole 1 diameter incorrect", 6.0, sortedHoles.get(0).getDiameter(), 0.1);
			assertEquals("Hole 2 diameter incorrect", 5.4, sortedHoles.get(1).getDiameter(), 0.1);
			assertEquals("Hole 3 diameter incorrect", 4.8, sortedHoles.get(2).getDiameter(), 0.1);
			assertEquals("Hole 4 diameter incorrect", 5.9, sortedHoles.get(3).getDiameter(), 0.1);
			assertEquals("Hole 5 diameter incorrect", 6.4, sortedHoles.get(4).getDiameter(), 0.1);
			assertEquals("Hole 6 diameter incorrect", 7.1, sortedHoles.get(5).getDiameter(), 0.1);
			assertEquals("Hole 7 diameter incorrect", 6.4, sortedHoles.get(6).getDiameter(), 0.1);
			assertEquals("Hole 8 diameter incorrect", 6.3, sortedHoles.get(7).getDiameter(), 0.1);
			assertEquals("Hole 9 diameter incorrect", 6.1, sortedHoles.get(8).getDiameter(), 0.1);
			assertEquals("Hole 10 diameter incorrect", 6.2, sortedHoles.get(9).getDiameter(), 0.1);

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

		lB[0] = 0.32;
		uB[0] = 0.38;
		lB[1] = 0.000;
		uB[1] = 0.001;
		lB[2] = 0.000;
		uB[2] = 0.050;
		lB[3] = 0.000;
		uB[3] = 0.050;
		lB[4] = 0.015;
		uB[4] = 0.050;
		lB[5] = 0.015;
		uB[5] = 0.050;
		lB[6] = 0.015;
		uB[6] = 0.050;
		lB[7] = 0.02;
		uB[7] = 0.050;
		lB[8] = 0.02;
		uB[8] = 0.050;
		lB[9] = 0.02;
		uB[9] = 0.050;
		lB[10] = 0.02;
		uB[10] = 0.100;

		lB[11] = 0.2;
		uB[11] = 0.4;
		lB[12] = 0.2;
		uB[12] = 0.4;
		lB[13] = 0.2;
		uB[13] = 0.4;
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
		lB[20] = 0.35;
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
	 * @throws FileNotFoundException 
	 */
	protected File getInputFile(String fileName, BindFactory bindFactory) throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
