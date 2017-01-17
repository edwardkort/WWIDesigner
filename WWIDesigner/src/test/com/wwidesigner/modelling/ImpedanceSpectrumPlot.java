/**
 * 
 */
package com.wwidesigner.modelling;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
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
			String inputInstrumentXML = "com/wwidesigner/optimization/example/E4-instrument_actual.xml";
			String inputTuningXML = "com/wwidesigner/optimization/example/E4-tuning_actual.xml";

			PhysicalParameters params = new PhysicalParameters(22.22,
					TemperatureType.C);
			Instrument instrument = plot
					.getInstrumentFromXml(inputInstrumentXML);
			InstrumentCalculator calculator = new NAFCalculator(instrument,
					params);

			Tuning tuning = plot.getTuningFromXml(inputTuningXML);
			// Fingering fingering = tuning.getFingering().get(0);
			Fingering fingering = tuning.getFingering().get(7);

			instrument.convertToMetres();

			double freqRange = 1.1;
			int numberOfFrequencies = 10000;
			double targetFreq = fingering.getNote().getFrequency();
			double freqStart = targetFreq / freqRange;
			double freqEnd = targetFreq * freqRange;
			ImpedanceSpectrum impSpectrum = new ImpedanceSpectrum();

			impSpectrum.calcImpedance(instrument, calculator, freqStart,
					freqEnd, numberOfFrequencies, fingering, params);
			impSpectrum.plotImpedanceSpectrum();

			Complex fluteImpedance = calculator.calcZ(targetFreq, fingering);
			String outStr = "Flute impedance: " + fluteImpedance.getReal()
					+ ", " + fluteImpedance.getImaginary() + "at " + targetFreq
					+ " Hz";
			System.out.println(outStr);

			ReflectanceSpectrum reflSpectrum = new ReflectanceSpectrum();
			reflSpectrum.calcReflectance(instrument, calculator, 200., 4000.,
					numberOfFrequencies, fingering, params);
			List<Double> magMinima = reflSpectrum.getMagnitudeMinima();
			double predFreq = reflSpectrum
					.getClosestMinimumFrequency(targetFreq);
			outStr = "Flute impedance magnitude minima for "
					+ fingering.getNote().getName() + " "
					+ fingering.toString() + " : " + magMinima.get(0) + " "
					+ magMinima.get(1) + " " + magMinima.get(2) + " "
					+ magMinima.get(3) + " at target " + targetFreq
					+ " Hz, and predicted " + predFreq + " Hz";
			System.out.println(outStr);
			reflSpectrum.plotReflectanceSpectrum(ReflectanceSpectrum.PLOT_REFL_MAGNITUDE_ONLY);
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
