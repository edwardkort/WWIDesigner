package com.wwidesigner.modelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

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
 * For a specified instrument and tuning, print a report listing
 * the predicted tuning of the instrument, and the deviation from measured values.
 * @author Burton Patkau
 * 
 */
public class InstrumentTuningReport
{
	public void printReport(String instrumentFile, String tuningFile)
	{
		try
		{
			double temperature = 28.2;
			PhysicalParameters params = new PhysicalParameters(temperature, TemperatureType.C);
			Instrument instrument = getInstrumentFromXml(instrumentFile);
			InstrumentCalculator calculator = new WhistleCalculator(instrument,params);
			Tuning tuning = getTuningFromXml(tuningFile);
			PrintWriter pw = new PrintWriter( System.out );
			List<Fingering>  noteList = tuning.getFingering();

			instrument.convertToMetres();
			double totalError = 0.0;
			int nrPredictions = 0;

			pw.println(instrumentFile);
			pw.println("Note  Nominal   fmax   Pred fmax   cents");
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
					PlayingRange range = new PlayingRange(instrument,calculator, fingering);
					double predicted = range.findFmax(actual);
					double cents;
					pw.printf("%2d   %7.2f  %7.2f   %7.2f", i, fnom, actual, predicted);
					if ( predicted > 0.0 )
					{
						cents = Note.cents(actual, predicted);
						pw.printf( "  %7.2f", cents );
						totalError += cents;
						nrPredictions += 1;
					}
					pw.println();
					pw.flush();
				}
			}
			pw.println();
			if ( nrPredictions > 0 )
			{
				pw.printf("Average error: %7.2f cents", totalError/nrPredictions);
				pw.println();
			}
			pw.println();
			pw.flush();
			
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
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
	protected File getInputFile(String fileName, BindFactory bindFactory) throws FileNotFoundException
	{
		String inputPath = bindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
