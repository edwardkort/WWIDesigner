/**
 * 
 */
package com.wwidesigner.math;

import java.io.File;
import java.io.FileNotFoundException;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.InstrumentConfigurator;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.calculation.GordonConfigurator;
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
public class ImpedanceSpectrumPlot
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		ImpedanceSpectrumPlot plot = new ImpedanceSpectrumPlot();
		try
		{
			String inputInstrumentXML = "com/wwidesigner/optimization/example/NoHoleNAF1.xml";
			String inputTuningXML = "com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml";

			PhysicalParameters params = new PhysicalParameters(22.22,
					TemperatureType.C);
			Instrument instrument = plot
					.getInstrumentFromXml(inputInstrumentXML);
			Tuning tuning = plot.getTuningFromXml(inputTuningXML);
			Fingering fingering = tuning.getFingering().get(0);

			plot.configureInstrument(instrument);
			instrument.updateComponents();
			instrument.setOpenHoles(fingering);

			double freqRange = 2.;
			int numberOfFrequencies = 2400;
			double targetFreq = fingering.getNote().getFrequency();
			double freqStart = targetFreq / freqRange;
			double freqEnd = targetFreq * freqRange;
			ImpedanceSpectrum spectrum = new ImpedanceSpectrum();

			spectrum.calcImpedance(instrument, freqStart, freqEnd,
					numberOfFrequencies, fingering, params);
			spectrum.plotSpectrum();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}

	protected Instrument getInstrumentFromXml(String inputInstrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);

		return instrument;
	}

	protected void configureInstrument(Instrument instrument) throws Exception
	{
		// InstrumentConfigurator instrumentConfig = new
		// SimpleFippleMouthpieceConfigurator();
		InstrumentConfigurator instrumentConfig = new GordonConfigurator();
		instrument.setConfiguration(instrumentConfig);

		// This unit-of-measure converter is called in setConfiguration(), but
		// is shown here to make it explicit. The method is efficient: it does
		// not redo the work.
		instrument.convertToMetres();
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
