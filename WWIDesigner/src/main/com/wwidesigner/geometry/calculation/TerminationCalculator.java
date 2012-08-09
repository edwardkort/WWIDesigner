/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class TerminationCalculator
{
	public TerminationCalculator()
	{
	}

	public abstract StateVector calcStateVector(Termination termination,
			double wave_number, PhysicalParameters params);
}
