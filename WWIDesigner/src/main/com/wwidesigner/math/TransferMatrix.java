package com.wwidesigner.math;

import org.apache.commons.math3.complex.Complex;

/**
 * @author kort
 * 
 */
public class TransferMatrix
{

    protected Complex mPP;
    protected Complex mPU;
    protected Complex mUP;
    protected Complex mUU;

    /**
     * Simple 2x2 complex matrix. Here e.g. PU represents the component of the
     * output pressure that depends on the input volume flow, etc.
     */
    public TransferMatrix()
    {
        mPP = Complex.ZERO;
        mPU = Complex.ZERO;
        mUP = Complex.ZERO;
        mUU = Complex.ZERO;
    }

    public TransferMatrix( Complex pp, Complex pu, Complex up, Complex uu )
    {
        mPP = copyComplex( pp );
        mPU = copyComplex( pu );
        mUP = copyComplex( up );
        mUU = copyComplex( uu );
    }

    public TransferMatrix( TransferMatrix from )
    {
        mPP = copyComplex( from.mPP );
        mPU = copyComplex( from.mPU );
        mUP = copyComplex( from.mUP );
        mUU = copyComplex( from.mUU );
    }

    public static TransferMatrix multiply( TransferMatrix lhs, TransferMatrix rhs )
    {
        return new TransferMatrix( lhs.mPP.multiply( rhs.mPP ).add( lhs.mPU.multiply( rhs.mUP ) ),
                                   lhs.mPP.multiply( rhs.mPU ).add( lhs.mPU.multiply( rhs.mUU ) ),
                                   lhs.mUP.multiply( rhs.mPP ).add( lhs.mUU.multiply( rhs.mUP ) ),
                                   lhs.mUP.multiply( rhs.mPU ).add( lhs.mUU.multiply( rhs.mUU ) ) );
    }

    public static StateVector multiply( TransferMatrix lhs, StateVector rhs )
    {
        return new StateVector( lhs.mPP.multiply( rhs.mP ).add( lhs.mPU.multiply( rhs.mU ) ),
                                lhs.mUP.multiply( rhs.mP ).add( lhs.mUU.multiply( rhs.mU ) ) );
    }

    public static Complex copyComplex( Complex in )
    {
        return new Complex( in.getReal(), in.getImaginary() );
    }

    
    /**
     * @return the pP
     */
    public Complex getPP()
    {
        return mPP;
    }

    
    /**
     * @return the pU
     */
    public Complex getPU()
    {
        return mPU;
    }

    
    /**
     * @return the uP
     */
    public Complex getUP()
    {
        return mUP;
    }

    
    /**
     * @return the uU
     */
    public Complex getUU()
    {
        return mUU;
    }

    
    /**
     * @param pp the pP to set
     */
    public void setPP( Complex pp )
    {
        mPP = pp;
    }

    
    /**
     * @param pu the pU to set
     */
    public void setPU( Complex pu )
    {
        mPU = pu;
    }

    
    /**
     * @param up the uP to set
     */
    public void setUP( Complex up )
    {
        mUP = up;
    }

    
    /**
     * @param uu the uU to set
     */
    public void setUU( Complex uu )
    {
        mUU = uu;
    }
}
