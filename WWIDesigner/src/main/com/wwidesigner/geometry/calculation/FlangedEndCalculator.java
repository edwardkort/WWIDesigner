/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.geometry.TerminationCalculator;
import com.wwidesigner.geometry.calculation.Tube;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 *
 */
public class FlangedEndCalculator extends TerminationCalculator
{

	public FlangedEndCalculator(Termination termination)
	{
		super(termination);
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.geometry.TerminationCalculator#calcStateVector(double, com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public StateVector calcStateVector(double wave_number,
			PhysicalParameters params)
	{
		Complex Zend = Tube.calcZflanged(params.calcFrequency(wave_number),
				0.5*this.termination.getBoreDiameter(), params);
		return new StateVector( Zend, Complex.ONE );
	}

}
