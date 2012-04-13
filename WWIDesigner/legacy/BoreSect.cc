// FluteCalc
// Copyright Dan Gordon, 2006

#include <cmath>
#include "BoreSect.hh"
#include "Common.hh"

void BoreSect::Validate()
{
	mIsConv = (mRBL > mRBR);
	mRBSmall = (mIsConv ? mRBR : mRBL);
	mRBLarge = (mIsConv ? mRBL : mRBR);
	mX0Inv = (mRBLarge - mRBSmall) / (mLB * mRBSmall);
}

void BoreSect::CalcT(TransferMatrix & t, double freq) const
{
	// See Scavone, PhD Thesis.

	double omega = 2.0 * PI * freq;
	double k = omega / mParams.c;

	double z0 = mParams.CalcZ0(mRBSmall);
		// Wave impedance of cylindrical bore, using radius at small end.

	double l_c_inv = mX0Inv / (1.0 + mLB * mX0Inv);

	double l_c_on_x0 = 1.0 + mLB * mX0Inv;
	double x0_on_l_c = 1.0 / l_c_on_x0;

	complex gamma;
		// Complex propagation wavenumber.
	complex z_c;
		// Characteristic impedance.

	bool use_losses = true;
	if (use_losses)
	{
		double r_ave = 0.5*(mRBL + mRBR);
			// Use ave. radius for calculation of viscous and thermal
			// losses.

		double r_v_m1 = 1.0/(sqrt(omega * mParams.rho / mParams.eta) * r_ave);
		double r_v_m2 = r_v_m1 * r_v_m1;
		double r_v_m3 = r_v_m2 * r_v_m1;

		double omega_on_v_p = k * (1.0 + 1.045 * r_v_m1) ;
			// omega times inverse of phase velocity of the complex
			// propagation wavenumber.

		double alpha = k * (1.045 * r_v_m1 + 1.080 * r_v_m2 +
				0.750 * r_v_m3);
			// Attenuation coefficient of the complex
			// propagation wavenumber.

		gamma = alpha + J * omega_on_v_p;

		z_c = (1.0 + 0.369 * r_v_m1)
			- J * (0.369 * r_v_m1 + 1.149 * r_v_m2 + 0.303 * r_v_m3);
		z_c *= z0;
	}
	else
	{
		// For lossless case the following hold:
		gamma = J * k;
		z_c = z0;
	}

	complex gamma_lb = gamma * mLB;
	complex cosh_gamma_lb = cosh(gamma_lb);
	complex sinh_gamma_lb = sinh(gamma_lb);

	complex gamma_x0_inv = mX0Inv / gamma ;

	complex a = l_c_on_x0 * cosh_gamma_lb - gamma_x0_inv * sinh_gamma_lb;
	complex b = x0_on_l_c * z_c * sinh(gamma_lb);
	complex c = (1.0 / z_c) *
		((l_c_on_x0 - gamma_x0_inv * gamma_x0_inv) * sinh_gamma_lb +
		 mX0Inv * gamma_x0_inv * mLB * cosh_gamma_lb);
	complex d = x0_on_l_c * cosh_gamma_lb + l_c_inv * sinh_gamma_lb / gamma ;

	if (mIsConv)
	{
		t.mPP = d;
		t.mPU = b;
		t.mUP = c;
		t.mUU = a;
	}
	else
	{
		t.mPP = a;
		t.mPU = b;
		t.mUP = c;
		t.mUU = d;
	}
}

std::ostream & BoreSect::print_data(std::ostream & os) const
{
	os << "   Bore left radius = " << mRBL << std::endl;
	os << "   Bore right radius = " << mRBR << std::endl;
	os << "   Bore length = " << mLB << std::endl;
	return os;
}
