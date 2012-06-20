/**
 * 
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
    
    public Complex Impedance()
    {
    	return mP.divide(mU);
    }
    
    public Complex Reflectance(double Z0)
    {
    	return mP.subtract(mU.multiply(Z0)).divide( mP.add(mU.multiply(Z0)));
    }
}
