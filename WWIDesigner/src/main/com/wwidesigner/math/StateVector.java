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
    
    public Complex Impedance()
    {
    	return mP.divide(mU);
    }
    
    public Complex Reflectance(double Z0)
    {
    	return mP.subtract(mU.multiply(Z0)).divide( mP.add(mU.multiply(Z0)));
    }
}
