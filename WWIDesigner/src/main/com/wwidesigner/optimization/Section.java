package com.wwidesigner.optimization;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

class Section
{
	private double mLength;
    private double mLeftRadius;
    private double mRightRadius;
    
	public Section(double length, double left_radius, double right_radius)
	{
		mLength = length;
		mLeftRadius = left_radius;
		mRightRadius = right_radius;
	}

	public double getLength()
	{
		return mLength;
	}
	public double getLeftRadius()
	{
		return mLeftRadius;
	}
	public double getRightRadius()
	{
		return mRightRadius;
	}

	public void setLength(double length)
	{
		mLength = length;
	}

	public TransferMatrix transferMatrix(double wave_number, PhysicalParameters params)
	{
		double Zc = params.calcZ0(mLeftRadius);
		
	    //double alpha = (1/mLeftRadius) * Math.sqrt(wave_number) * params.getAlphaConstant();
  	    Complex Gamma = Complex.I.multiply(wave_number); //.add( Complex.valueOf(1, 1).multiply(alpha) );

        Complex sinhL = Gamma.multiply(mLength).sinh();
        Complex coshL = Gamma.multiply(mLength).cosh();
		return new TransferMatrix(coshL, sinhL.multiply(Zc), sinhL.divide(Zc), coshL);
	}

	public double radiusAt(double position)
	{
		assert(position >= 0. && position <= mLength);

		return mLeftRadius + position/mLength * (mRightRadius-mLeftRadius);
	}

	public Section makeSubSection(double xi, double xf)
	{
		return new Section(xf-xi, radiusAt(xi), radiusAt(xf));
	}

	public static void main(String[] args)
	{
		double L = 0.3;
		double r = 0.005;
		
	    Section s = new Section(L, r, r);

	    PhysicalParameters params = new PhysicalParameters(25., TemperatureType.C);

	    System.out.println( params.toString() );
	    double c = params.getSpeedOfSound();
	    
	    double f = c/(2*L);	    
	    double k = 2*Math.PI*f/params.getSpeedOfSound();
	    
	    TransferMatrix M = s.transferMatrix(k, params);
	    StateVector sin = TransferMatrix.multiply(M, new StateVector(Complex.ZERO, Complex.ONE));
	    
	    Complex Rin = sin.Reflectance( params.calcZ0(r));
	    
	    System.out.println( "Phase of the impedance: " + Rin.negate().getArgument() + " at f = " + f);	    
	}
}