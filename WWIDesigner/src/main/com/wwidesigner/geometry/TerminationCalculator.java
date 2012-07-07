/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class TerminationCalculator
{
	protected Termination termination;

	public abstract StateVector calcStateVector(double wave_number,
			PhysicalParameters params);

	public TerminationCalculator(Termination termination)
	{
		this.termination = termination;
	}
}
