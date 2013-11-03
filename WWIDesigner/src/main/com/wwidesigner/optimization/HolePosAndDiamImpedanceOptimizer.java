/**
 * 
 */
package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * @author kort
 * 
 */
@Deprecated
public class HolePosAndDiamImpedanceOptimizer extends
		HolePositionAndDiameterOptimizer
{
	public HolePosAndDiamImpedanceOptimizer(Instrument inst,
			InstrumentCalculator calculator, TuningInterface tuning)
	{
		super(inst, calculator, tuning);
	}

	@Override
	public void setOptimizationFunction()
	{
		optimizationFunction = new BasicImpedanceOptimizationFunction(this,
				tuning);

	}
}
