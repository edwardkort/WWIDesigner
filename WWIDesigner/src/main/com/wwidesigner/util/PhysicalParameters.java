/**
 * 
 */
package com.wwidesigner.util;

import static com.wwidesigner.util.Constants.*;


/**
 * @author kort
 * 
 */
public class PhysicalParameters
{

    private double mRho;
    private double mEta;
    private double mTemperature;
    private double mSpecificHeat;

    public PhysicalParameters()
    {
    	this(72.0, TemperatureType.F);
    }
    
    public PhysicalParameters( double temperature, TemperatureType tempType )
    {
        switch ( tempType )
        {
            case C:
                mTemperature = temperature + 273.15;
                mSpecificHeat = 332.0 * ( 1.0 + 0.00166 * temperature );
                break;
            case F:
                mTemperature = ( temperature + 40. ) * 5. / 9. - 40.;
                mSpecificHeat = 332.0 * ( 1.0 + 0.00166 * mTemperature );
                mTemperature += 273.15;
        }
        mRho = ( ( P_AIR / R_AIR ) + ( P_V / R_V ) ) / mTemperature;

        mEta = 3.648e-6 * ( 1 + 0.0135003 * mTemperature );

    }

    /**
     * Utility function. Calculate the wave impedance of a bore of nominal
     * radius r, given these parameters.
     */
    public double calcZ0( double radius )
    {
        return mRho * mSpecificHeat / ( Math.PI * radius * radius );
        // Wave impedance of a bore, nominal radius r.
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "Physical Parameters :\n" );
        buf.append( "Temperature = " + mTemperature + "\n" );
        buf.append( "Specific Heat = " + mSpecificHeat + "\n" );
        buf.append( "rho = " + mRho + "\n" );
        buf.append( "eta = " + mEta + "\n" );
        buf.append( "gamma = " + GAMMA + "\n" );
        buf.append( "kappa = " + KAPPA + "\n" );
        buf.append( "C_p = " + C_P + "\n" );
        buf.append( "nu = " + NU + "\n" );

        return buf.toString();
    }

    /**
     * @return the eta
     */
    public double getEta()
    {
        return mEta;
    }

    /**
     * @return the rho
     */
    public double getRho()
    {
        return mRho;
    }

    /**
     * @return the specificHeat
     */
    public double getSpecificHeat()
    {
        return mSpecificHeat;
    }

    /**
     * @return the temperature
     */
    public double getTemperature()
    {
        return mTemperature;
    }

}
