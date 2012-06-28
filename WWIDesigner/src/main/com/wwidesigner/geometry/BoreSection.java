package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

class BoreSection implements ComponentInterface
{
	private double mLength;
	private double mLeftRadius;
	private double mRightRadius;

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

	public TransferMatrix calcTransferMatrix(double wave_number,
			PhysicalParameters params)
	{
		double Zc = params.calcZ0(mLeftRadius);

		// double alpha = (1/mLeftRadius) * Math.sqrt(wave_number) *
		// params.getAlphaConstant();
		Complex Gamma = Complex.I.multiply(wave_number); // .add(
															// Complex.valueOf(1,
															// 1).multiply(alpha)
															// );

		Complex sinhL = Gamma.multiply(mLength).sinh();
		Complex coshL = Gamma.multiply(mLength).cosh();
		return new TransferMatrix(coshL, sinhL.multiply(Zc), sinhL.divide(Zc),
				coshL);
	}

}