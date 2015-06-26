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
public class NafImpedancePlot
{
	// List of note indexes in the list of fingerings to plot.
	protected static final int[] NoteIndexList = {9,10,11,12};
	protected static final double FreqRange = 1.5;		// Plot a fifth above and below.

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		NafImpedancePlot plot = new NafImpedancePlot();
		try
		{
			String inputInstrumentXML = "com/wwidesigner/optimization/example/LightG6HoleNAF.xml";
			String inputTuningXML = "com/wwidesigner/optimization/example/LightG6HoleNAFTuning.xml";

			PhysicalParameters params = new PhysicalParameters(23.3,
					TemperatureType.C);
			Instrument instrument = plot.getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument,params);
			Tuning tuning = plot.getTuningFromXml(inputTuningXML);

			for ( int noteIndex : NoteIndexList )
			{
				PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();
				spectrum.plot(calculator, tuning.getFingering().get(noteIndex), FreqRange, 600, true);
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
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
