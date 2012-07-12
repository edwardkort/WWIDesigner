package com.wwidesigner.optimization;

import com.wwidesigner.geometry.Instrument;

public interface InstrumentOptimizerInterface
{

	public abstract double[] getStateVector();

	public abstract void updateGeometry(double[] state_vector);
	
	public abstract Instrument getInstrument();

}