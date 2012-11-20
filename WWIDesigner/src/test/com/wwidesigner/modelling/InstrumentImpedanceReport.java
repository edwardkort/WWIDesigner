package com.wwidesigner.modelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
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

	protected static String inputInstrumentXML = "com/wwidesigner/optimization/example/BP7.xml";
	protected static String inputTuningXML = "com/wwidesigner/optimization/example/BP7-tuning.xml";

	/**
	 * For the standard instrument, calculate the impedance for
	 * selected notes at the known fmax, where Imag(Z) == 0.
	 * Predict fmax, and compare to measured values.
	 */
	public static void main(String[] args)
	{
		try
		{
			double temperature = 28.2;
			PhysicalParameters params = new PhysicalParameters(temperature, TemperatureType.C);
			Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument,params);
			Tuning tuning = getTuningFromXml(inputTuningXML);
			PrintWriter pw = new PrintWriter( System.out );
			List<Fingering>  noteList = tuning.getFingering();

			Double fmax[]
				  = { 588.,   665.,   741.,   791.,   899.,  1005.,  1087.,  1147.,
			         1202.,  1333.,  1485.,  1586.,  1787.,  1997.,  2048.,  2245.,
			         2437.,   908.};

			instrument.convertToMetres();
			double Z0 = params.calcZ0(instrument.getMouthpiece().getBoreDiameter()/2.0);

			pw.println("Note  fmax       Z.real       Z.imag      imag/real");
			for ( int i = 0; i < fmax.length; ++ i )
			{
				pw.printf("%2d  %7.2f", i, fmax[i]);
				Fingering fingering = noteList.get(i);
				Complex Z = calculator.calcZ(fmax[i],fingering);
				Z = Z.divide(Z0);
				double normalized = Z.getImaginary()/Z.getReal();
				pw.printf( " %12.4f %12.4f %12.5f", Z.getReal(), Z.getImaginary(), normalized );
				pw.println();
			}
			pw.println();
			pw.flush();
			
			pw.println("Note  Nominal   fmax   Pred fmax   cents       Z.real       Z.imag      imag/real");
			for ( int i = 0; i < noteList.size(); ++ i )
			{
				Fingering fingering = tuning.getFingering().get(i);
				double fnom = 0.0;
				double actual = 0.0;
				if ( fingering.getNote().getFrequencyMax() != null )
				{
					actual = fingering.getNote().getFrequencyMax();
				}
				if ( fingering.getNote().getFrequency() != null )
				{
					fnom = fingering.getNote().getFrequency();
					if ( actual == 0.0 )
					{
						actual = fnom;
					}
				}
				if ( actual != 0.0 )
				{
					PlayingRange range = new PlayingRange(calculator, fingering);
					double predicted = range.findFmax(actual);
					pw.printf("%2d   %7.2f  %7.2f   %7.2f", i, fnom, actual, predicted);
					if ( predicted > 0.0 )
					{
						pw.printf( "  %7.2f", Note.cents(actual, predicted) );
						Complex Z = calculator.calcZ(predicted,fingering);
						Z = Z.divide(Z0);
						double normalized = Z.getImaginary()/Z.getReal();
						pw.printf( " %12.4f %12.4f %12.5f", Z.getReal(), Z.getImaginary(), normalized );
					}
					pw.println();
					pw.flush();
				}
			}
			pw.println();
			
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected static Instrument getInstrumentFromXml(String instrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(instrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
	}

	protected static Tuning getTuningFromXml(String tuningXML) throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(tuningXML, noteBindFactory);
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
