/**
 * 
 */
package com.wwidesigner.modelling;

import java.io.File;
import java.io.FileNotFoundException;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.WhistleCalculator;
import com.wwidesigner.modelling.PlayingRangeSpectrum;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class BP8NotesPlot
{
	// List of note indexes in the list of fingerings to plot.
	protected static final int[] NoteIndexList = {8,10,12,14,16};
	protected static final double FreqRange = 1.4;		// Plot one octave above and below.
	protected static final int NumberOfPoints = 600;	// Number of points to plot.

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		BP8NotesPlot plot = new BP8NotesPlot();
		try
		{
			String inputInstrumentXML = "com/wwidesigner/optimization/example/BP8.xml";
			String inputTuningXML = "com/wwidesigner/optimization/example/BP8-tuning.xml";

			PhysicalParameters params = new PhysicalParameters(28.2,
					TemperatureType.C);
			Instrument instrument = plot.getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument,params);
			Tuning tuning = plot.getTuningFromXml(inputTuningXML);
			for ( int noteIndex : NoteIndexList )
			{
				PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();
				spectrum.plot(calculator, tuning.getFingering().get(noteIndex),
						FreqRange,NumberOfPoints);
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e);
			e.printStackTrace();
		}
	}

	protected Instrument getInstrumentFromXml(String inputInstrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
	}

	protected Tuning getTuningFromXml(String inputTuningXML) throws Exception
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
