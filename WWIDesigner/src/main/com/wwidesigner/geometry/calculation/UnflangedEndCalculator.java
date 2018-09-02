/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.geometry.calculation.Tube;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 *
 */
public class UnflangedEndCalculator extends TerminationCalculator
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
		Complex Zend = Tube.calcZload(params.calcFrequency(wave_number),
				0.5d*termination.getBoreDiameter(), params);
		return new StateVector( Zend, Complex.ONE );
	}

}
