/**
 * Class to manage acoustic state vectors with two elements: pressure and volume flow.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.math;

import org.apache.commons.math3.complex.Complex;

/**
 * @author kort
 * 
 */
public class StateVector
{

    protected Complex mP;
    protected Complex mU;

    /**
     * Simple 2x1 complex vector representing the state of the air column at a
     * point in the bore. P refers to the pressure and U to the volume flow.
     */
    public StateVector()
    {
        mP = Complex.ZERO;
        mU = Complex.ZERO;
    }

    public StateVector( Complex p, Complex u )
    {
        mP = TransferMatrix.copyComplex( p );
        mU = TransferMatrix.copyComplex( u );
    }

    public StateVector( StateVector from )
    {
        mP = TransferMatrix.copyComplex( from.mP );
        mU = TransferMatrix.copyComplex( from.mU );
    }

    /**
     * Construct a new state vector that satisfies Z.equals(this.getImpedance()).
     */
    public StateVector( Complex Z )
    {
    	if (Z.getReal() == Double.POSITIVE_INFINITY)
    	{
    		mP = new Complex(1.0, 0.0);
    		mU = new Complex(0.0, 0.0);
    		return;
    	}
    	if (Z.getReal() == Double.NEGATIVE_INFINITY)
    	{
    		mP = new Complex(-1.0, 0.0);
    		mU = new Complex(0.0, 0.0);
    		return;
    	}
    	// For greater robustness, divide both P and U by (1+Z),
    	// so that both are between 0 and 1, but ratio still works out to Z.
    	// From Paul Dickens, 2007.
    	Complex Zplus1 = Z.add(1.0);
        mP = Z.divide(Zplus1);
        mU = Complex.ONE.divide(Zplus1);
    }

    /**
     * @return a state vector representing an ideal open end.
     */
    public static StateVector OpenEnd()
    {
    	// At an open end, pressure is zero.
    	return new StateVector(Complex.ZERO, Complex.ONE);
    }

    /**
     * @return a state vector representing an ideal closed end.
     */
    public static StateVector ClosedEnd()
    {
    	// At a closed end, acoustic flow is zero.
    	return new StateVector(Complex.ONE, Complex.ZERO);
    }

	/**
	 * @return the impedance (Z) that a component with this state vector is
	 *         presenting
	 */
    public Complex getImpedance()
    {
    	return mP.divide(mU);
    }

	/**
	 * @return the admittance (Y) that a component with this state vector is
	 *         presenting
	 */
    public Complex getAdmittance()
    {
    	return mU.divide(mP);
    }

	/**
	 * @return the reflectance (coefficient of reflection of pressure)
	 *         that a component with this state vector is presenting
	 */
    public Complex getReflectance(double Z0)
    {
    	return mP.subtract(mU.multiply(Z0))
    			.divide(mP.add(mU.multiply(Z0)));
    }

    /**
     * Add another state vector in series with this.
     * @param other
     * @return sv that satisfies sv.getImpedance() = this.getImpedance() + other.getImpedance().
     */
    public StateVector series(StateVector other)
    {
    	Complex newP = this.mP.multiply(other.mU).add(other.mP.multiply(this.mU));
    	Complex newU = this.mU.multiply(other.mU);
    	return new StateVector(newP, newU);
    }

    /**
     * Add another state vector in parallel with this.
     * @param other
     * @return sv that satisfies 1/sv.getImpedance() = 1/this.getImpedance() + 1/other.getImpedance().
     */
    public StateVector parallel(StateVector other)
    {
    	Complex newU = this.mP.multiply(other.mU).add(other.mP.multiply(this.mU));
    	Complex newP = this.mP.multiply(other.mP);
    	return new StateVector(newP, newU);
    }
}
