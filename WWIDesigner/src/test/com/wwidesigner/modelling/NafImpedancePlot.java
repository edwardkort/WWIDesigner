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
import com.wwidesigner.note.Fingering;
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
	protected static final int NoteIndex = 10;			// Index of fingering to plot.
	protected static final double FreqRange = 2.;		// Plot one octave above and below.
	protected static final int NumberOfPoints = 600;	// Number of points to plot.

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
			Fingering fingering = tuning.getFingering().get(NoteIndex);

			instrument.convertToMetres();
			instrument.setOpenHoles(fingering);

			double targetFreq;
			if ( fingering.getNote().getFrequency() != null )
			{
				targetFreq = fingering.getNote().getFrequency();
			}
			else if ( fingering.getNote().getFrequencyMax() != null )
			{
				targetFreq = fingering.getNote().getFrequencyMax();
			}
			else {
				targetFreq = 1000.0;
			}
			double freqStart = targetFreq / FreqRange;
			double freqEnd = targetFreq * FreqRange;
			if ( freqEnd > 4000.0 )
			{
				freqEnd = 4000.0;
			}
			PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();

			spectrum.calcImpedance(instrument, calculator, freqStart, freqEnd,
					NumberOfPoints, fingering);
			spectrum.plotImpedanceSpectrum();
			spectrum.plotPlayingRange();
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
