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
import com.wwidesigner.optimization.InstrumentOptimizer;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class BP7ImpedancePlot
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		BP7ImpedancePlot plot = new BP7ImpedancePlot();
		try
		{
			String inputInstrumentXML = "com/wwidesigner/geometry/bind/example/BP7.xml";
			String inputTuningXML = "com/wwidesigner/note/bind/example/BP7-tuning.xml";

			PhysicalParameters params = new PhysicalParameters(28.2,
					TemperatureType.C);
			Instrument instrument = plot.getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new WhistleCalculator(instrument);
			Tuning tuning = plot.getTuningFromXml(inputTuningXML);
			Fingering fingering = tuning.getFingering().get(0);

			instrument.convertToMetres();
			instrument.setOpenHoles(fingering);

			double freqRange = 2.;
			int numberOfFrequencies = 2400;
			double targetFreq = fingering.getNote().getFrequency();
			double freqStart = targetFreq / freqRange;
			double freqEnd = targetFreq * freqRange;
			PlayingRangeSpectrum spectrum = new PlayingRangeSpectrum();

			spectrum.calcImpedance(instrument, calculator, freqStart, freqEnd,
					numberOfFrequencies, fingering, params);
			spectrum.plotImpedanceSpectrum();
			spectrum.plotPlayingRange();
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e);
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

	protected void setPhysicalParameters(InstrumentOptimizer optimizer)
	{
		PhysicalParameters parameters = new PhysicalParameters(22.22,
				TemperatureType.C);
		optimizer.setPhysicalParams(parameters);
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
