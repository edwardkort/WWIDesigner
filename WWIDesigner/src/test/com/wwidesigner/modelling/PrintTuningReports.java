package com.wwidesigner.modelling;

import com.wwidesigner.note.Tuning;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.Constants.TemperatureType;

/**
 * @author Burton Patkau
 * 
 */
public class PrintTuningReports
{
	public static void printReport(String instrumentFile, String tuningFile)
	{
		printReport( instrumentFile, tuningFile, 28.2 );
	}

	public static void printReport(String instrumentFile, String tuningFile, double temperature)
	{
		LinearXInstrumentTuner tuner = new LinearXInstrumentTuner();
		TuningComparisonTable table = new TuningComparisonTable("Tuning for " + instrumentFile);
		try
		{
			tuner.setInstrument(instrumentFile, true);
			tuner.setTuning(tuningFile, true);
			tuner.setParams( new PhysicalParameters(temperature, TemperatureType.C) );
			tuner.setCalculator( new WhistleCalculator() );
			Tuning predicted = tuner.getPredictedTuning();
			table.buildTable(tuner.getTuning(), predicted);
			table.printTuning( System.out );
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * For specified instruments, predict the tuning for each note,
	 * and compare to measured values.
	 */
	public static void main(String[] args)
	{
		printReport("com/wwidesigner/optimization/example/BP7.xml",
				"com/wwidesigner/optimization/example/BP7-tuning.xml");
		printReport("com/wwidesigner/optimization/example/BP8-partial.xml",
				"com/wwidesigner/optimization/example/BP8-partial-tuning.xml");
		printReport("com/wwidesigner/optimization/example/NoHoleNAF1.xml",
				"com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml");
		printReport("com/wwidesigner/optimization/example/1HoleNAF1.xml",
				"com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml");
		printReport("com/wwidesigner/optimization/example/LightG6HoleNAF.xml",
				"com/wwidesigner/optimization/example/LightG6HoleNAFTuning.xml", 24);
	}
}
