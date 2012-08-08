/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author waveNumberort
 * 
 */
public class GordonBoreSectionCalculator extends BoreSectionCalculator
{
	private double mRBL;
	private double mRBR;
	private double mLB;
	private double mRBSmall;
	private double mRBLarge;
	private double mX0Inv;
	private boolean mIsConv;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.BoreSectionCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(BoreSection section,
			double waveNumber, PhysicalParameters params)
	{
		setVariables(section);
		// See Scavone, PhD Thesis.

		double omega = waveNumber * params.getSpeedOfSound();

		double z0 = params.calcZ0(mRBSmall);
		// Wave impedance of cylindrical bore, using radius at small end.

		double l_c_inv = mX0Inv / (1.0 + mLB * mX0Inv);

		double l_c_on_x0 = 1.0 + mLB * mX0Inv;
		double x0_on_l_c = 1.0 / l_c_on_x0;

		Complex gamma;
		// Complex propagation wavenumber.
		Complex z_c;
		// Characteristic impedance.

		boolean use_losses = true;
		if (use_losses)
		{
			double r_ave = 0.5 * (mRBL + mRBR);
			// Use ave. radius for calculation of viscous and thermal
			// losses.

			double r_v_m1 = 1.0 / (Math.sqrt(omega * params.getRho()
					/ params.getEta()) * r_ave);
			double r_v_m2 = r_v_m1 * r_v_m1;
			double r_v_m3 = r_v_m2 * r_v_m1;

			double omega_on_v_p = waveNumber * (1.0 + 1.045 * r_v_m1);
			// omega times inverse of phase velocity of the complex
			// propagation wavenumber.

			double alpha = waveNumber
					* (1.045 * r_v_m1 + 1.080 * r_v_m2 + 0.750 * r_v_m3);
			// Attenuation coefficient of the complex
			// propagation wavenumber.

			gamma = new Complex(0., -1.).multiply(omega_on_v_p).add(alpha);

			z_c = Complex.I.multiply(
					0.369 * r_v_m1 + 1.149 * r_v_m2 + 0.303 * r_v_m3).add(
					1.0 + 0.369 * r_v_m1);
			z_c = z_c.multiply(z0);
		}
		else
		{
			// For lossless case the following hold:
			gamma = new Complex(0., -1.).multiply(waveNumber);
			z_c = new Complex(z0, 0.);
		}

		Complex gamma_lb = gamma.multiply(mLB);
		Complex cosh_gamma_lb = gamma_lb.cosh();
		Complex sinh_gamma_lb = gamma_lb.sinh();

		Complex gamma_x0_inv = gamma.reciprocal().multiply(mX0Inv);

		Complex a = cosh_gamma_lb.multiply(l_c_on_x0).subtract(
				sinh_gamma_lb.multiply(gamma_x0_inv));
		Complex b = gamma_lb.sinh().multiply(z_c).multiply(x0_on_l_c);
		Complex c = z_c.reciprocal().multiply(
				gamma_x0_inv
						.multiply(gamma_x0_inv)
						.subtract(l_c_on_x0)
						.multiply(-1.)
						.multiply(sinh_gamma_lb)
						.add(gamma_x0_inv.multiply(mX0Inv).multiply(mLB)
								.multiply(cosh_gamma_lb)));
		Complex d = cosh_gamma_lb.multiply(x0_on_l_c).add(
				sinh_gamma_lb.multiply(l_c_inv).divide(gamma));

		TransferMatrix matrix = new TransferMatrix();
		if (mIsConv)
		{
			matrix.setPP(d);
			matrix.setPU(b);
			matrix.setUP(c);
			matrix.setUU(a);
		}
		else
		{
			matrix.setPP(a);
			matrix.setPU(b);
			matrix.setUP(c);
			matrix.setUU(d);
		}

		return matrix;
	}

	protected void setVariables(BoreSection section)
	{
		mRBL = section.getLeftRadius();
		mRBR = section.getRightRadius();
		mLB = section.getLength();
		mIsConv = (mRBL > mRBR);
		mRBSmall = (mIsConv ? mRBR : mRBL);
		mRBLarge = (mIsConv ? mRBL : mRBR);
		mX0Inv = (mRBLarge - mRBSmall) / (mLB * mRBSmall);
	}
}
