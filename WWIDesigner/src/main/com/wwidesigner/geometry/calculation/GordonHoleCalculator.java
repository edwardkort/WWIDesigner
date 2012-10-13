/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class GordonHoleCalculator extends HoleCalculator
{
	private double mRB;
	private double mRH;
	private double mLH;
	private double mRC;
	private double mOHLB;
	private double mCHLB;
	private PhysicalParameters mParams;

	protected void updateGeometry(Hole hole)
	{
		mRB = hole.getBoreDiameter() / 2.;
		mRH = 0.912 * hole.getDiameter() / 2.; // Multiplier set by eye to fit LightG6HoleNaf tuning.
		mLH = hole.getHeight();
		mRC = hole.getInnerCurvatureRadius() == null ? 0.0005 : hole
				.getInnerCurvatureRadius();
		double rh_on_rb = mRH / mRB;
		double rh_on_rb_2 = rh_on_rb * rh_on_rb;
		double rh_on_rb_4 = rh_on_rb_2 * rh_on_rb_2;

		double term1 = 0.47 * mRH * rh_on_rb_4;
		double term2 = 0.62 * rh_on_rb_2 + 0.64 * rh_on_rb;
		double term3 = Math.tanh(1.84 * mLH / mRH);

		// From eq. (8) in Keefe (1990):
		mOHLB = term1 / (term2 + term3);
		// From eq. (9) in Keefe (1990):
		mCHLB = term1 / (term2 + (1.0 / term3));
	}

	// Effective acoustic length of the hole when it is open.
	protected double calcHLE(double freq)
	{
		// See Keefe 1990

		double k = 2.0 * Math.PI * freq / mParams.getSpeedOfSound(); // Wavenumber.
		double k_inv = 1.0 / k;

		double tan_k_l = Math.tan(k * mLH);

		double rh_on_rb = mRH / mRB;

		double result = (k_inv * tan_k_l + mRH
				* (1.40 - 0.58 * rh_on_rb * rh_on_rb))
				/ (1.0 - 0.61 * k * mRH * tan_k_l);
		// From eq. (5) in Keefe (1990):

		return result;
	}

	// Specific resistance along the bore, when the hole is open.
	protected double calcXi(double freq)
	{
		double omega = 2.0 * Math.PI * freq;
		double k = omega / mParams.getSpeedOfSound(); // Wavenumber.

		double d_v = Math.sqrt(2.0 * mParams.getEta()
				/ (mParams.getRho() * omega));
		// Viscous boundary layer thickness.

		double alpha = (Math.sqrt(2 * mParams.getEta() * omega
				/ mParams.getRho()) + (mParams.getGamma() - 1)
				* Math.sqrt(2. * mParams.getKappa() * omega
						/ (mParams.getRho() * mParams.getC_p())))
				/ (2. * mRH * mParams.getSpeedOfSound());

		double result = 0.25 * (k * mRH) * (k * mRH) + alpha * mLH + 0.25 * k
				* d_v * Math.log(2. * mRH / mRC);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(Hole hole, double waveNumber,
			PhysicalParameters parameters)
	{
		mParams = parameters;
		updateGeometry(hole);
		TransferMatrix matrix = new TransferMatrix();
		double omega = waveNumber * mParams.getSpeedOfSound();
		double freq = omega / (2. * Math.PI);
		double z0 = mParams.getRho() * mParams.getSpeedOfSound()
				/ (Math.PI * mRB * mRB); // Wave
		// impedance of
		// the main
		// bore.
		double rb_on_rh = mRB / mRH;
		double rb_on_rh_2 = rb_on_rh * rb_on_rh;

		matrix.setPP(Complex.ONE);
		matrix.setUU(Complex.ONE);
		if (hole.isOpenHole())
		{
			// Sign as per Gordon's implementation
			matrix.setPU(Complex.I.multiply(z0 * rb_on_rh_2 * waveNumber
					* mOHLB));
			matrix.setUP(new Complex(0., -1.)
					.multiply(waveNumber * calcHLE(freq)).add(calcXi(freq))
					.multiply(z0 * rb_on_rh_2).reciprocal());

			// Change sign to match Antoine's configuration
			// matrix.setPU(new Complex(0., -1.).multiply(z0 * rb_on_rh_2 *
			// waveNumber
			// * mOHLB));
			// matrix.setUP(new Complex(0., 1.)
			// .multiply(waveNumber * calcHLE(freq)).add(calcXi(freq))
			// .multiply(z0 * rb_on_rh_2).reciprocal());
		}
		else
		{
			// Sign as per Gordon's implementation
			matrix.setPU(Complex.I.multiply(z0 * rb_on_rh_2 * waveNumber
					* mCHLB));
			matrix.setUP(new Complex(0., -1.).multiply(Math.tan(waveNumber
					* mLH)
					/ (z0 * rb_on_rh_2)));

			// Change sign to match Antoine's configuration
			// matrix.setPU(new Complex(0., -1.).multiply(z0 * rb_on_rh_2
			// * waveNumber * mCHLB));
			// matrix.setUP(new Complex(0., 1.).multiply(Math.tan(waveNumber
			// * mLH)
			// / (z0 * rb_on_rh_2)));
		}

		return matrix;
	}
}
