package com.wwidesigner.modelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
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
			double totalMaxError = 0.0;		// Net error in predicting fmax, in cents.
			double varianceMax = 0.0;		// Sum of squared error in predicting fmax.
			int nrMaxPredictions = 0;		// Number of predictions of fmax.
			double totalMinError = 0.0;		// Net error in predicting fmin, in cents.
			double varianceMin = 0.0;		// Sum of squared error in predicting fmin.
			int nrMinPredictions = 0;		// Number of predictions of fmin.

			pw.println(instrumentFile);
			pw.println("Note  Nominal   fmin   Pred fmin   cents    fmax   Pred fmax   cents");
			for ( int i = 0; i < noteList.size(); ++ i )
			{
				Fingering fingering = tuning.getFingering().get(i);
				Double fnom = fingering.getNote().getFrequency();
				Double actualMax = fingering.getNote().getFrequencyMax();
				Double actualMin = fingering.getNote().getFrequencyMin();
				double target = 0.0;
				if ( fnom != null )
				{
					target = fnom;
				}
				else if ( actualMax != null )
				{
					target = actualMax;
				}
				else if ( actualMin != null )
				{
					target = actualMin;
				}
				if ( target != 0.0 )
				{
					PlayingRange range = new PlayingRange(instrument,calculator, fingering);
					double fmax, fmin;
					try {
						fmax = range.findFmax(target);
					}
					catch (NoPlayingRange e)
					{
						fmax = 0.0;
					}
					try {
						fmin = range.findFmin(fmax);
					}
					catch (NoPlayingRange e)
					{
						fmin = 0.0;
					}
					double cents;
					pw.printf("%2d   %7.2f  %7.2f   %7.2f", i, fnom, actualMin, fmin);
					if ( actualMin != null && fmin > 0.0 )
					{
						cents = Note.cents(actualMin, fmin);
						pw.printf( "  %7.2f", cents );
						totalMinError += cents;
						varianceMin   += cents*cents;
						nrMinPredictions += 1;
					}
					else
					{
						pw.print("         ");
					}
					pw.printf("  %7.2f   %7.2f", actualMax, fmax);
					if ( actualMax != null && fmax > 0.0 )
					{
						cents = Note.cents(actualMax, fmax);
						pw.printf( "  %7.2f", cents );
						totalMaxError += cents;
						varianceMax   += cents*cents;
						nrMaxPredictions += 1;
					}
					else
					{
						pw.print("         ");
					}
					pw.println();
					pw.flush();
				}
			}
			pw.println();
			if ( nrMaxPredictions > 0 )
			{
				pw.printf("Error in fmax: net %7.2f cents, deviation %7.2f cents", 
						totalMaxError/nrMaxPredictions, Math.sqrt(varianceMax/nrMaxPredictions));
				pw.println();
			}
			if ( nrMinPredictions > 0 )
			{
				pw.printf("Error in fmin: net %7.2f cents, deviation %7.2f cents", 
						totalMinError/nrMinPredictions, Math.sqrt(varianceMin/nrMinPredictions));
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
