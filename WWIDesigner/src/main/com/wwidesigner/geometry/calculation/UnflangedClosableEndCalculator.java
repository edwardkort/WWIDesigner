package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

public class UnflangedClosableEndCalculator extends UnflangedEndCalculator
{
	@Override
	public StateVector calcStateVector(Termination termination,
			double wave_number, PhysicalParameters params)
	{
		if (termination.isOpenEnd())
		{
			return super.calcStateVector(termination, wave_number, params);
		}
		return new StateVector(Complex.ONE, Complex.ZERO);
	}

}
