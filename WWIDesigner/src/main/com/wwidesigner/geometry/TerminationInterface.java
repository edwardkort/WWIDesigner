/**
 * 
 */
package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

/**
 * Class representing a load impedance at the terminal (foot) end of the
 * instrument.
 */
public interface TerminationInterface
{

	// Terminal load (impedance):
	public Complex calcZL(double freq);

}
