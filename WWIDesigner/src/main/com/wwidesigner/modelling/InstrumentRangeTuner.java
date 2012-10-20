/**
 * 
 */
package com.wwidesigner.modelling;

/**
 * @author kort
 * 
 */
public class InstrumentRangeTuner extends InstrumentTuner
{
	public void showTuning(String title, boolean exitOnTableClose)
	{
		InstrumentTuningRangeTable table = new InstrumentTuningRangeTable(title);

		calculator.setInstrument(instrument);
		calculator.setPhysicalParameters(params);

		table.buildTable(calculator, instrument, tuning);
		table.showTuning(exitOnTableClose);
	}

}
