/**
 * 
 */
package com.wwidesigner.impedance.geometry;

import org.apache.commons.math.complex.Complex;

/**
 * Class representing a load impedance at the terminal (foot) end of the
 * instrument.
 */
public interface TerminationInterface
{

	// Terminal load (impedance):
	public Complex calcZL(double freq);

}
