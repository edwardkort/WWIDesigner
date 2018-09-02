package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

public class ThickFlangedOpenEndCalculator extends TerminationCalculator
{
	@Override
	public StateVector calcStateVector(Termination termination,
			boolean isOpen, double wave_number, PhysicalParameters params)
	{
		if (! isOpen)
		{
			return StateVector.ClosedEnd();
		}
		Complex P = calcZ(termination, wave_number, params).multiply(
				params.calcZ0(termination.getBoreDiameter() / 2.d));

		return new StateVector(P, Complex.ONE);
	}

	private Complex calcZ(Termination termination,
			double wave_number, PhysicalParameters params)
	{
		double a = termination.getBoreDiameter() / 2d;
		double b = termination.getFlangeDiameter() / 2d;

		double a_b = a / b;

		double ka = wave_number * a;

		double delta_inf = 0.8216d;
		double delta_0 = 0.6133d;

		double delta_circ = delta_inf + a_b * (delta_0 - delta_inf) + 0.057d
				* a_b * (1d - FastMath.pow(a_b, 5d));
		double R0 = (1d + 0.2d * ka - 0.084d * ka * ka)
				/ (1d + 0.2d * ka + (0.5d - 0.084d) * ka * ka);

		Complex R = Complex.I.multiply(-2 * delta_circ * ka).exp()
				.multiply(-R0);

		return R.add(1d).divide(R.negate().add(1d));
	}
}
