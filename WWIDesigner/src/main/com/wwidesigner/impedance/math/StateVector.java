/**
 * 
 */
package com.wwidesigner.impedance.math;

import org.apache.commons.math.complex.Complex;

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
}
