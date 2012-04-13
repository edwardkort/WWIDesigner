// FluteCalc
// Copyright Dan Gordon, 2006

#include "SilverFluteHole.hh"

SilverFluteHole::SilverFluteHole(const PhysParams & params,
		double rB,
		double rHExt,
		double lH,
		bool isClosed,
		double rC,
		double padHeight,
		double rPad) :
	Hole(params, rB, rHExt, lH, isClosed, rC),
	mPadHeight(padHeight),
	mRPad(rPad)
{
	// Empty.
}

double SilverFluteHole::CalcHLE(double freq) const
{
	// See Keefe 1990

	double k = 2.0 * PI * freq / mParams.c;	// Wavenumber.
	double k_inv = 1.0 / k;
	double tan_k_l = tan(k * mLHG);
	double rh_on_rb = mRHG / mRB;
	double rpad_on_rh = mRPad/mRHG;
	double rh_on_padh = mRHG / mPadHeight;

	double temp1 = 0.61 * pow(rpad_on_rh, 0.18) * pow(rh_on_padh,0.39);

	double result = 
		(k_inv * tan_k_l + 
		 mRHG * (temp1 + (0.25 * PI) * (1 - 0.74 * rh_on_rb * rh_on_rb))) /
		(1.0 - temp1 * k * mRHG * tan_k_l);
		// From eq. (5) in Keefe (1990):

	return result;
}

void SilverFluteHole::CalcAndCacheRLG()
{
	mRHG = mRHExt;

	// See Keefe 1990
	double rh_on_rb = mRHG / mRB;
	mLHG = mLH + 0.125 * mRHG * rh_on_rb * (1.0 + 0.172 * rh_on_rb * rh_on_rb);
}
