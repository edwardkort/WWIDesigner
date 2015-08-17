/**
 * Class to represent an acoustic transfer matrix in a transmission matrix model.
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
public class TransferMatrix
{

    protected Complex mPP;
    protected Complex mPU;
    protected Complex mUP;
    protected Complex mUU;

    /**
     * Simple 2x2 complex matrix. Here e.g. PU represents the component of the
     * output pressure that depends on the input volume flow, etc.
     * The default matrix is the identity matrix.
     */
    public TransferMatrix()
    {
        mPP = Complex.ONE;
        mPU = Complex.ZERO;
        mUP = Complex.ZERO;
        mUU = Complex.ONE;
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

    public TransferMatrix multiply( TransferMatrix rhs )
    {
        return new TransferMatrix( this.mPP.multiply( rhs.mPP ).add( this.mPU.multiply( rhs.mUP ) ),
                                   this.mPP.multiply( rhs.mPU ).add( this.mPU.multiply( rhs.mUU ) ),
                                   this.mUP.multiply( rhs.mPP ).add( this.mUU.multiply( rhs.mUP ) ),
                                   this.mUP.multiply( rhs.mPU ).add( this.mUU.multiply( rhs.mUU ) ) );
    }

    public static StateVector multiply( TransferMatrix lhs, StateVector rhs )
    {
        return new StateVector( lhs.mPP.multiply( rhs.mP ).add( lhs.mPU.multiply( rhs.mU ) ),
                                lhs.mUP.multiply( rhs.mP ).add( lhs.mUU.multiply( rhs.mU ) ) );
    }

    public StateVector multiply( StateVector rhs )
    {
        return new StateVector( this.mPP.multiply( rhs.mP ).add( this.mPU.multiply( rhs.mU ) ),
                                this.mUP.multiply( rhs.mP ).add( this.mUU.multiply( rhs.mU ) ) );
    }
    
    public Complex determinant()
    {
    	Complex det = mPP.multiply(mUU).subtract(mPU.multiply(mUP));
    	return det;
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

	public static TransferMatrix makeIdentity()
	{
        return new TransferMatrix(Complex.ONE, Complex.ZERO, Complex.ZERO, Complex.ONE);
	}
}
