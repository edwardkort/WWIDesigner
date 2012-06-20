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
	private double mSpeedOfSound;

	private double mMu;
	private double mGamma;
	private double mNu;
	private double mAlphaConstant;
	
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
                break;
            case F:
                mTemperature = ( temperature + 40. ) * 5. / 9. - 40. + 273.15;
                break;
        }
        mSpeedOfSound = 332.0 * ( 1.0 + 0.00166*(mTemperature-273.15) );
        
        //mRho = ( ( P_AIR / R_AIR ) + ( P_V / R_V ) ) / mTemperature;

        mEta = 3.648e-6 * ( 1 + 0.0135003 * mTemperature );
        
        double deltaT = (mTemperature-273.15)-26.85;
        
        mRho = 1.1769   * (1 - 0.00335*deltaT);
        mMu = 1.8460E-5 * (1 + 0.00250*deltaT);
  	    mGamma = 1.4017 * (1 - 0.00002*deltaT);
   	    mNu = 0.8410    * (1 - 0.00020*deltaT);
   	    //p.c = 3.4723E+2  * (1 + 0.00166*deltaT);

        
        mAlphaConstant = Math.sqrt(mMu/(2*mRho*mSpeedOfSound)) * (1 + (mGamma-1)/mNu);

    }

    /**
     * Utility function. Calculate the wave impedance of a bore of nominal
     * radius r, given these parameters.
     */
    public double calcZ0( double radius )
    {
        return mRho * mSpeedOfSound / ( Math.PI * radius * radius );
        // Wave impedance of a bore, nominal radius r.
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "Physical Parameters :\n" );
        buf.append( "Temperature = " + mTemperature + "\n" );
        buf.append( "Specific Heat = " + mSpecificHeat + "\n" );
        buf.append( "rho = " + mRho + "\n" );
        buf.append( "c = " + mSpeedOfSound + "\n" );
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

	public double getSpeedOfSound()
	{
		return mSpeedOfSound;
	}

	public double getAlphaConstant()
	{
		return mAlphaConstant;
	}
}
