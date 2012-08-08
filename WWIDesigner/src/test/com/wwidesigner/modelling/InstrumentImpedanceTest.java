/**
 * Test instrument impedance calculation on whistle BP7.
 */
package com.wwidesigner.modelling;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author Burton Patkau
 * 
 */
public class InstrumentImpedanceTest
{
	// Standard instrument, and its measured tuning.

	protected String inputInstrumentXML = "com/wwidesigner/geometry/bind/example/BP7.xml";
	protected String inputTuningXML = "com/wwidesigner/note/bind/example/BP7-tuning.xml";

	/**
	 * For the standard instrument, calculate the impedance for selected notes
	 * at the known fmax, where Imag(Z) == 0.
	 */
	@Test
	public final void testInstrumentImpedance()
	{
		try
		{
			Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument);
			Tuning tuning = getTuningFromXml(inputTuningXML);
			PhysicalParameters params = new PhysicalParameters(28.2,
					TemperatureType.C);

			Double fmax[] = { 589.49699364, 665.95846589, 740.62596732,
					790.25253027, 895.41223635, 1000.04547471, 1080.97410484,
					1139.23859984, 1201.28218389, 1336.22103577, 1487.47285037,
					1588.12692212, 1787.297483, 1992.58680484, 2045.42056261,
					2233.64276274, 2433.04456904, 912.91065873 };

			instrument.convertToMetres();
			double Z0 = params.calcZ0(instrument.getMouthpiece()
					.getBoreDiameter() / 2.0);

			for (int i = 0; i < fmax.length; ++i)
			{
				Fingering fingering = tuning.getFingering().get(i);
				Complex Z = calculator.calcZ(fmax[i],
						fingering, params);
				Z = Z.divide(Z0);
				assertEquals("Imag(Z) is non-zero at known resonance.", 0.0,
						Z.getImaginary(), 0.035);
			}
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
}
