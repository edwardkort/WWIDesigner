// FluteCalc
// Copyright Dan Gordon, 2006

#include<cmath>
#include "Embouchure.hh"
#include "BoreSect.hh"

void Embouchure::Validate()
{
	assert(mRB > 0.0);
	assert(mLChar > 0.0);
	assert(mLCav >= 0.0);
}

void Embouchure::CalcT(TransferMatrix & t, double freq) const
{
	double z0 = mParams.CalcZ0(mRB);
	double k_delta_l = CalcKDeltaL(freq);
	t.mPP = t.mUU = cos(k_delta_l);
	t.mPU = J * sin(k_delta_l) * z0; 
	t.mUP = J * sin(k_delta_l) / z0;
}

std::ostream & Embouchure::print_data(std::ostream & os) const
{
	os << "   Bore radius = " << mRB << std::endl;
	os << "   Embouchure characteristic length = " << mLChar << std::endl;
	os << "   Cavity length = " << mLCav << std::endl;
	
	return os;
}

double Embouchure::CalcKDeltaL(double freq) const
{
	double z0 = mParams.CalcZ0(mRB);
	double result = atan(1.0 / (z0 * (CalcJYE(freq) + CalcJYC(freq))));

	return result;
}

double Embouchure::CalcJYE(double freq) const
{
	double omega = 2.0 * PI * freq;
	
	double result = mLChar / (mParams.gamma * omega) ;

	return result;
}

double Embouchure::CalcJYC(double freq) const
{
	double omega = 2.0 * PI * freq;
	double v = 2.0 * PI * mRB * mRB * mLCav;

	double result = -(omega * v) / (mParams.gamma * mParams.c * mParams.c);

	return result;
}
