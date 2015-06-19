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
			double wave_number, PhysicalParameters params)
	{
		Complex P = calcZ(termination, wave_number, params).multiply(
				params.calcZ0(termination.getBoreDiameter() / 2.));

		return new StateVector(P, Complex.ONE);
	}

	private Complex calcZ(Termination termination,
			double wave_number, PhysicalParameters params)
	{
		double a = termination.getBoreDiameter() / 2;
		double b = termination.getFlangeDiameter() / 2;

		double a_b = a / b;

		double ka = wave_number * a;

		double delta_inf = 0.8216;
		double delta_0 = 0.6133;

		double delta_circ = delta_inf + a_b * (delta_0 - delta_inf) + 0.057
				* a_b * (1 - FastMath.pow(a_b, 5));
		double R0 = (1 + 0.2 * ka - 0.084 * ka * ka)
				/ (1 + 0.2 * ka + (0.5 - 0.084) * ka * ka);

		Complex R = Complex.I.multiply(-2 * delta_circ * ka).exp()
				.multiply(-R0);

		return R.add(1).divide(R.negate().add(1));
	}
}
