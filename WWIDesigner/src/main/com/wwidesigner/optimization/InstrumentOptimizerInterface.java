package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;

public interface InstrumentOptimizerInterface
{

	public abstract double[] getStateVector();

	public abstract void updateGeometry(double[] state_vector);
	
	public abstract Instrument getInstrument();

	public abstract InstrumentCalculator getInstrumentCalculator();
}