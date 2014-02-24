/**
 * Test instrument impedance calculation on whistle BP7.
 */
package com.wwidesigner.modelling;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.note.Note;
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

	protected String inputInstrumentXML = "com/wwidesigner/optimization/example/BP7.xml";
	protected String inputTuningXML = "com/wwidesigner/optimization/example/BP7-tuning.xml";

	public static void main(String[] args)
	{
		InstrumentImpedanceTest myTest = new InstrumentImpedanceTest();
		myTest.testInstrumentImpedance();
	}

	/**
	 * For the standard instrument, test the calculation of
	 * impedance against measured fmax values,
	 * and compare predicted fmax to measured values.
	 */
	@Test
	public final void testInstrumentImpedance()
	{
		try
		{
			double temperature = 27.0;
			PhysicalParameters params = new PhysicalParameters(temperature, TemperatureType.C,
					98.4, 100, 0.04);
			Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument,params);
			Tuning tuning = getTuningFromXml(inputTuningXML);
			PrintWriter pw = new PrintWriter( System.out );
			List<Fingering>  noteList = tuning.getFingering();

			// High C3 here is higher than measured value, to eliminate outlier.
			Double fmax[]
					= {  589.,   663.,   740.,   791.,   892.,   998.,  1086.,  1143.,
						1207.,  1334.,  1493.,  1595.,  1803.,  2007.,  2045.,  2250.,
						2457.,   905.};

			instrument.convertToMetres();
			double Z0 = params.calcZ0(instrument.getMouthpiece()
									  .getBoreDiameter() / 2.0);

			// Test that impedance is near zero at measured fmax values.

			pw.println("Note  fmax       Z.real       Z.imag      imag/real");
			for (int i = 0; i < fmax.length; ++i)
			{
				pw.printf("%2d  %7.2f", i, fmax[i]);
				Fingering fingering = noteList.get(i);
				Complex Z = calculator.calcZ(fmax[i],fingering);
				Z = Z.divide(Z0);
				double normalized = Z.getImaginary()/Z.getReal();
				pw.printf( " %12.4f %12.4f %12.5f", Z.getReal(), Z.getImaginary(), normalized );
				pw.println();
				pw.flush();
				assertEquals("Imag(Z) is non-zero at known resonance.", 0.0,
						Z.getImaginary(), 0.10);
			}
			pw.println();
			pw.flush();

			// Test that zeros of the calculated impedance are close to the measured values of fmax.

			pw.println("Note  Nominal   fmax   Pred fmax   cents       Z.real       Z.imag      imag/real");
			double totalError = 0.0;
			int nrPredictions = 0;

			for ( int i = 0; i < noteList.size(); ++ i )
			{
				Fingering fingering = noteList.get(i);
				double fnom = 0.0;
				double actual = 0.0;
				double cents;
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
					double predicted = range.findXZero(actual);
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

					assertTrue("No prediction for note " + i, predicted > 0.0 );
					cents = Note.cents(actual, predicted);
					assertEquals("Predicted fmax does not agree with actual at note " + i, 0.0,
								cents, 26.0 );
					totalError += cents;
					nrPredictions += 1;
				}
			}
			pw.println();
			pw.flush();

			// Test that the average prediction error is close to zero.
			
			assertEquals("Average prediction error is not small.", 0.0, totalError/nrPredictions, 3.50 );
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

	protected Tuning getTuningFromXml(String tuningXML) throws Exception
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
	protected File getInputFile(String fileName, BindFactory bindFactory)
			throws FileNotFoundException
	{
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
