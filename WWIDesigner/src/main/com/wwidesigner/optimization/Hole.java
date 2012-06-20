package com.wwidesigner.optimization;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class Hole
{
	private double mRadius;
	private double mHeight;
    private double mPosition;

    public Hole(double position, double radius, double height)
    {
    	mPosition = position;
    	mRadius = radius;
    	mHeight = height;
    }
    
	public double getPosition()
	{
		return mPosition;
	}
	public void setPosition(double position)
	{
		mPosition = position;
	}

	public TransferMatrix transferMatrix(boolean state, double boreRadius, double wave_number, PhysicalParameters mParameters)
	{
		Complex Zs = null;
		Complex Za = null;

		double Z0 = mParameters.calcZ0(mRadius);
		
		double delta = mRadius / boreRadius;

		double tm = (mRadius * delta / 8.) * (1. + 0.207*delta*delta*delta);
		double te = mHeight + tm;

		double ta = 0.;
		
   	    //Complex Gamma = Complex.I.multiply(wave_number);

	    if (state == true) // open
	    {
            double kb = wave_number*mRadius;
            double ka = kb/delta;	   	 
       	    double xhi =  0.25*kb*kb;
            
       	    ta = (-0.35+0.06*Math.tanh(2.7*mHeight/mRadius)) * mRadius * delta*delta*delta*delta;	        
  
            Complex Zr = Complex.I.multiply(wave_number*0.61*mRadius).add( xhi );
            
   	        Complex Zo = (Zr.multiply(Math.cos(wave_number*te)).add( Complex.I.multiply(Math.sin(wave_number*te)))).divide(
   	        		Complex.I.multiply(Zr).multiply(Math.sin(wave_number*te)).add( Math.cos(wave_number*te)) );

            double ti = mRadius*(0.822-0.10*delta-1.57*delta*delta+2.14*delta*delta*delta-1.6*delta*delta*delta*delta+0.50*delta*delta*delta*delta*delta)*
	                    (1.+(1.-4.56*delta+6.55*delta*delta)*(0.17*ka+0.92*ka*ka+0.16*ka*ka*ka-0.29*ka*ka*ka*ka));

   	        Zs = Complex.I.multiply(wave_number*ti).add(Zo).multiply(Z0);
   	             
	    }
	    else
	    {   	
            ta = (-0.12-0.17*Math.tanh(2.4*mHeight/mRadius)) * mRadius * delta*delta*delta*delta;
            Zs =  Complex.valueOf(0, -Z0 / Math.tan(wave_number*te));
	    }

        Za = Complex.I.multiply( Z0*wave_number*ta );
        Complex Za_Zs = Za.divide(Zs);
        
        return new TransferMatrix(Za_Zs.divide(2.).add(1.), Za.multiply(Za_Zs.divide(4.).add(1.)), 
        		Complex.ONE.divide(Zs), Za_Zs.divide(2.0).add(1.));
	}        		
}
