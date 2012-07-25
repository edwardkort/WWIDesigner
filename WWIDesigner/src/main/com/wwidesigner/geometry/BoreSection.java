package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class BoreSection implements ComponentInterface
{
	private double mLength;
	private double mLeftRadius;
	private double mRightRadius;
	private double rightBorePosition;
	
	public BoreSection()
	{

	}

	public BoreSection(double length, double left_radius, double right_radius)
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

	/**
	 * @param mLeftRadius
	 *            the mLeftRadius to set
	 */
	public void setLeftRadius(double leftRadius)
	{
		mLeftRadius = leftRadius;
	}

	/**
	 * @param mRightRadius
	 *            the mRightRadius to set
	 */
	public void setRightRadius(double rightRadius)
	{
		mRightRadius = rightRadius;
	}

	public TransferMatrix calcTransferMatrix(double wave_number, PhysicalParameters params)
	{
		double ZcLeft = params.calcZ0(mLeftRadius);

		if (mLeftRadius == mRightRadius) // the case of a cylindrical segment
		{
			double alpha = (1 / mLeftRadius) * Math.sqrt(wave_number)
					* params.getAlphaConstant();

			// Gamma = alpha + i*omega/v
			//    alpha =~ omega/c * epsilon
			//    omega/v =~ omega/c ( 1 + epsilon ) = omega/c + alpha
			// Gamma = i * omega/c + (1+i) * alpha
			Complex Gamma = Complex.I.multiply(wave_number).add(
					Complex.valueOf(1, 1).multiply(alpha));

			Complex sinhL = Gamma.multiply(mLength).sinh();
			Complex coshL = Gamma.multiply(mLength).cosh();

			TransferMatrix tm = new TransferMatrix(coshL, sinhL.multiply(ZcLeft),
					sinhL.divide(ZcLeft), coshL);
			assert tm.determinant() == Complex.valueOf(1.0,0.0);
			return tm;
		}

		// the case of a cone
		double ZcRight = params.calcZ0(mRightRadius);

		double one_over_x_in = (mRightRadius - mLeftRadius)
				/ (mLeftRadius * mLength);
		double one_over_x_out = (mRightRadius - mLeftRadius)
				/ (mRightRadius * mLength);

		// inverse of the equivalent radius at which we calculate the losses
		double one_over_Req = Math.log(mRightRadius / mLeftRadius)
				/ (mRightRadius - mLeftRadius);

		Complex k_lossy = Complex.valueOf(1, -1).multiply(
				one_over_Req * Math.sqrt(wave_number) * params.getAlphaConstant()).add(wave_number);

		Complex k_lossy_L = k_lossy.multiply(mLength);

		Complex A = k_lossy_L.cos().multiply(mRightRadius / mLeftRadius)
				.subtract( k_lossy_L.sin().multiply(one_over_x_in).divide(k_lossy) );
		Complex B = k_lossy_L.sin().multiply(Complex.I)
				.multiply((mRightRadius / mLeftRadius) * ZcRight);
		Complex C = Complex.valueOf(mRightRadius / mLeftRadius)
				.multiply(Complex.I.multiply(k_lossy_L.sin()).multiply(
						k_lossy.pow(-2).multiply(one_over_x_in*one_over_x_out).add(1) ).add(
								k_lossy_L.cos().multiply(one_over_x_in - one_over_x_out).divide(
										Complex.I.multiply(k_lossy))))
				.divide(ZcLeft);
		Complex D = k_lossy_L.cos().multiply(mLeftRadius / mRightRadius)
				.add(k_lossy_L.sin().multiply(one_over_x_out).divide(k_lossy));

		TransferMatrix tm = new TransferMatrix(A, B, C, D); 
		assert tm.determinant() == Complex.valueOf(1.0,0.0);
		return tm;
	}

	/**
	 * @return the rightBorePosition
	 */
	public double getRightBorePosition()
	{
		return rightBorePosition;
	}

	/**
	 * @param rightBorePosition
	 *            the rightBorePosition to set
	 */
	public void setRightBorePosition(double rightBorePosition)
	{
		this.rightBorePosition = rightBorePosition;
	}

}