package com.wwidesigner.optimization;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

class IdealOpenEnd implements TerminationInterface
{
	public StateVector stateVector(double wave_number, PhysicalParameters params)
	{
		return new StateVector(Complex.ZERO, Complex.ONE);
	}
}