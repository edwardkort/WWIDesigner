package com.wwidesigner.modelling;

import com.wwidesigner.modelling.InstrumentTuningReport;

/**
 * @author Burton Patkau
 * 
 */
public class PrintTuningReports
{
	/**
	 * For specified instruments, predict the tuning for each note,
	 * and compare to measured values.
	 */
	public static void main(String[] args)
	{
		InstrumentTuningReport reporter = new InstrumentTuningReport();
		reporter.printReport("com/wwidesigner/optimization/example/BP7.xml",
				"com/wwidesigner/optimization/example/BP7-tuning.xml");
		reporter.printReport("com/wwidesigner/optimization/example/BP8-partial.xml",
				"com/wwidesigner/optimization/example/BP8-partial-tuning.xml");
		reporter.printReport("com/wwidesigner/optimization/example/NoHoleNAF1.xml",
				"com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml");
		reporter.printReport("com/wwidesigner/optimization/example/1HoleNAF1.xml",
				"com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml");
		reporter.printReport("com/wwidesigner/optimization/example/LightG6HoleNAF.xml",
				"com/wwidesigner/optimization/example/LightG6HoleNAFTuning.xml", 24);
	}
}
