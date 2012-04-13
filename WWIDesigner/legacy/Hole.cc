// FluteCalc
// Copyright Dan Gordon, 2006

#include<cmath>
#include "Hole.hh"
void Hole::Validate()
{
	Component::Validate();

	assert(mRB > 0.0);
	assert(mRHExt > 0.0);
	assert(mLH > 0.0);

	CalcAndCacheRLG();

	// Calculate and cache the series effective lengths of the hole:

	// See Keefe 1990

	double rh_on_rb = mRHG / mRB;
	double rh_on_rb_2 = rh_on_rb * rh_on_rb;
	double rh_on_rb_4 = rh_on_rb_2 * rh_on_rb_2;

	double term1 = 0.47 * mRHG * rh_on_rb_4;
	double term2 = 0.62 * rh_on_rb_2 + 0.64 * rh_on_rb;
	double term3 = tanh(1.84 * mLHG / mRHG);

	// From eq. (8) in Keefe (1990):
	mOHLB =  term1 / (term2 + term3); 
	// From eq. (9) in Keefe (1990):
	mCHLB =  term1 / (term2 + (1.0 / term3)); 
}

// Effective accoustic length of the hole when it is open.
double Hole::CalcHLE(double freq) const
{
	// See Keefe 1990

	double k = 2.0 * PI * freq / mParams.c;	// Wavenumber.
	double k_inv = 1.0 / k;

	double tan_k_l = tan(k * mLHG);

	double rh_on_rb = mRHG / mRB;
	
	double result = 
		(k_inv * tan_k_l + mRHG * (1.40 - 0.58 * rh_on_rb * rh_on_rb)) / 
		(1.0 - 0.61 * k * mRHG * tan_k_l);
		// From eq. (5) in Keefe (1990):
	assert(result > 0.0);

	return result;
}

// Specific resistance along the bore, when the hole is open.
double Hole::CalcXi(double freq) const
{
	double omega = 2.0 * PI * freq;	
	double k = omega / mParams.c;	// Wavenumber.

	double d_v = sqrt (2.0 * mParams.eta / (mParams.rho * omega));	
		// Viscous boundary layer thickness.

	double alpha = 
		(sqrt(2 * mParams.eta * omega / mParams.rho) + 
		 (mParams.gamma - 1) * 
		 sqrt(2 * mParams.kappa * omega / (mParams.rho * mParams.C_p))) / 
		(2 * mRHG * mParams.c);

	double result = 0.25 * (k * mRHG) * (k * mRHG) + alpha * mLHG +
		0.25 * k * d_v * log(2 * mRHG / mRC);

	return result;
}
		
void Hole::CalcT(TransferMatrix & t, double freq) const
{
	double omega = 2.0 * PI * freq;	
	double k = omega / mParams.c;	// Wavenumber.
	double z0 = mParams.rho * mParams.c / (PI * mRB * mRB);	// Wave impedance of the main bore.
	double rb_on_rh = mRB / mRHG;
	double rb_on_rh_2 = rb_on_rh * rb_on_rh;

	t.mPP = t.mUU = ONE;
	if (mIsClosed)
	{
		t.mPU = -J * z0 * rb_on_rh_2 * k * mCHLB;
		t.mUP =  J * tan(k * mLHG) / (z0 * rb_on_rh_2);
	}
	else
	{
		t.mPU = -J * z0 * rb_on_rh_2 * k * mOHLB;
		t.mUP = 1.0 / 
			(z0 * rb_on_rh_2 * (J * k * CalcHLE(freq) + CalcXi(freq)));
	}
}
		
std::ostream & Hole::print_data(std::ostream & os) const
{
	os << "   Bore radius = " << mRB << std::endl;
	os << "   Hole radius = " << mRHExt << std::endl;
	os << "   Hole geometric radius = " << mRHG << std::endl;
	os << "   Hole length = " << mLH << std::endl;
	os << "   Hole geometric length = " << mLHG << std::endl;
	os << "   Closed = " << mIsClosed << std::endl;
	os << "   Edge radius of curvature = " << mRC << std::endl;
	/*
	os << "      Length corrections: " << std::endl;
	for (double freq = 50.0; freq <= 2000.0; freq += 50.0)
	{
		os << "         f = " << freq << ", eff length = " 
			<< (CalcHLE(freq)) << std::endl;
	}
	*/

	return os;
}
		
void Hole::CalcAndCacheRLG()
{
	mRHG = mRHExt;
	mLHG = mLH;
}
