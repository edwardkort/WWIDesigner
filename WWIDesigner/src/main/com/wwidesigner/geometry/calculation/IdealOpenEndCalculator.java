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
public class IdealOpenEndCalculator extends TerminationCalculator
{

	/* (non-Javadoc)
	 * @see com.wwidesigner.geometry.TerminationCalculator#calcStateVector(double, com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public StateVector calcStateVector(Termination termination,
			boolean isOpen, double wave_number, PhysicalParameters params)
	{
		if (! isOpen)
		{
			return StateVector.ClosedEnd();
		}
		return StateVector.OpenEnd();
	}

}
