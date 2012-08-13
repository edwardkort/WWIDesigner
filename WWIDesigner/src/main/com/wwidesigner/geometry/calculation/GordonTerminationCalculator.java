/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class GordonTerminationCalculator extends TerminationCalculator
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.calculation.TerminationCalculator#calcStateVector
	 * (com.wwidesigner.geometry.Termination, double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public StateVector calcStateVector(Termination termination,
			double waveNumber, PhysicalParameters params)
	{
		double boreRadius = termination.getBoreDiameter() / 2.;
		double flangeRadius = termination.getFlangeDiameter() / 2.;

		// Start with z_l for a cylindrical tube or radius mRBR.

		// For now, just use a crude piecewise constant resistance plus
		// frequency dependent reactance from Scavone's thesis.
		// See Fletcher and Rossing.
		double kr = waveNumber * boreRadius;
		double z0 = params.calcZ0(boreRadius);
		double flange_factor = boreRadius / flangeRadius;
		double length_corr_factor = 0.821 - 0.135 * flange_factor - 0.073
				* Math.pow(flange_factor, 4);
		double rea = z0 * length_corr_factor * kr;
		// Reactance (im part of Z_L).
		double res = z0 * (kr < 2.0 ? 0.25 * kr * kr : 1.0);
		// Resistance (re part of Z_L).
		Complex result = new Complex(res, -rea);

		return new StateVector(result, Complex.ONE);
	}

}
