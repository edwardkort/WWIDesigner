package com.wwidesigner.modelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author Burton Patkau
 * 
 */
public class InstrumentImpedanceReport
{
	// Standard instrument, and its measured tuning.

	protected static String inputInstrumentXML = "com/wwidesigner/geometry/bind/example/BP7.xml";
	protected static String inputTuningXML = "com/wwidesigner/note/bind/example/BP7-tuning.xml";

	/**
	 * For the standard instrument, calculate the impedance for
	 * selected notes at the known fmax, where Imag(Z) == 0.
	 */
	public static void main(String[] args)
	{
		try
		{
			Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument);
			Tuning tuning = getTuningFromXml(inputTuningXML);
			double temperature = 28.2;
			PhysicalParameters params = new PhysicalParameters(temperature, TemperatureType.C);
			PrintWriter pw = new PrintWriter( System.out );

			Double fmax[]
				  = { 589.49699364,   665.95846589,   740.62596732,   790.25253027,
			          895.41223635,  1000.04547471,  1080.97410484,  1139.23859984,
			         1201.28218389,  1336.22103577,  1487.47285037,  1588.12692212,
			         1787.297483  ,  1992.58680484,  2045.42056261,  2233.64276274,
			         2433.04456904,   912.91065873};

			instrument.convertToMetres();
			double Z0 = params.calcZ0(instrument.getMouthpiece().getBoreDiameter()/2.0);

			pw.println("Note  fmax       Z.real       Z.imag      imag/real");
			pw.flush();
			for ( int i = 0; i < fmax.length; ++ i )
			{
				pw.printf("%2d  %7.2f", i, fmax[i]);
				Fingering fingering = tuning.getFingering().get(i);
				Complex Z = calculator.calcZ(fmax[i],fingering,params);
				Z = Z.divide(Z0);
				double normalized = Z.getImaginary()/Z.getReal();
				pw.printf( " %12.4f %12.4f %12.5f", Z.getReal(), Z.getImaginary(), normalized );
				pw.println();
				pw.flush();
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}

	protected static Instrument getInstrumentFromXml(String instrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
	}

	protected static Tuning getTuningFromXml(String tuningXML) throws Exception
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
	protected static File getInputFile(String fileName, BindFactory bindFactory) throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
