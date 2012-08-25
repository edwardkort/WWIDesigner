/**
 * 
 */
package com.wwidesigner.modelling;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 *         This class just serves as a debugger entry point to assess impedance
 *         calculations.
 */
public class SimpleImpedanceCalculationTest
{
	private static String instrumentFile_NAF = "com/wwidesigner/optimization/example/LightG6HoleNAF.xml";
	private static String tuningFile_NAF = "com/wwidesigner/optimization/example/LightG6HoleNAFTuning.xml";

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			Instrument instrument = (Instrument) GeometryBindFactory
					.getInstance().unmarshalXml(instrumentFile_NAF, true, true);
			PhysicalParameters params = new PhysicalParameters(74.,
					TemperatureType.F);
			InstrumentCalculator calculator = new GordonCalculator(instrument, params);
			Tuning tuning = (Tuning)NoteBindFactory.getInstance().unmarshalXml(tuningFile_NAF, true, true);
			
			for (Fingering fingering : tuning.getFingering()) {
				calculator.calcZ(fingering);
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}

}
