/**
 * Test isCompatible function in instrument calculators.
 */
package com.wwidesigner.modelling;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece.SingleReed;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.modelling.SimpleReedCalculator;
import com.wwidesigner.util.BindFactory;

/**
 * @author Burton Patkau
 * 
 */
public class CompatibilityTest
{
	// Standard instrument, and its measured tuning.

	protected String fippleInstrumentXML = "com/wwidesigner/optimization/example/BP7.xml";
	protected String reedInstrumentXML = "com/wwidesigner/optimization/example/chalumeau_alto.xml";

	public static void main(String[] args)
	{
		CompatibilityTest myTest = new CompatibilityTest();
		myTest.testCompatibility();
	}

	/**
	 * Test compatibility of example instruments with available calculators.
	 */
	@Test
	public final void testCompatibility()
	{
		try
		{
			Instrument fippleInstrument = getInstrumentFromXml(fippleInstrumentXML);
			Instrument reedInstrument = getInstrumentFromXml(reedInstrumentXML);
			reedInstrument.getMouthpiece().setFipple(null);
			reedInstrument.getMouthpiece().setSingleReed(new SingleReed());
			InstrumentCalculator calculator = new WhistleCalculator();
			assertTrue(calculator.getClass().getSimpleName() + " fails to accept " + fippleInstrumentXML,
					calculator.isCompatible(fippleInstrument));
			assertFalse(calculator.getClass().getSimpleName() + " fails to reject " + reedInstrumentXML,
					calculator.isCompatible(reedInstrument));
			calculator = new NAFCalculator();
			assertTrue(calculator.getClass().getSimpleName() + " fails to accept " + fippleInstrumentXML,
					calculator.isCompatible(fippleInstrument));
			assertFalse(calculator.getClass().getSimpleName() + " fails to reject " + reedInstrumentXML,
					calculator.isCompatible(reedInstrument));
			calculator = new FluteCalculator();
			assertFalse(calculator.getClass().getSimpleName() + " fails to reject " + fippleInstrumentXML,
					calculator.isCompatible(fippleInstrument));
			assertFalse(calculator.getClass().getSimpleName() + " fails to reject " + reedInstrumentXML,
					calculator.isCompatible(reedInstrument));
			calculator = new SimpleReedCalculator();
			assertFalse(calculator.getClass().getSimpleName() + " fails to reject " + fippleInstrumentXML,
					calculator.isCompatible(fippleInstrument));
			assertTrue(calculator.getClass().getSimpleName() + " fails to accept " + reedInstrumentXML,
					calculator.isCompatible(reedInstrument));
		}
		catch (Exception e)
		{
			fail("Exception: " + e.getMessage());
		}
	}

	protected Instrument getInstrumentFromXml(String instrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(instrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
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
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
