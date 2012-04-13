// FluteCalc
// Copyright Dan Gordon, 2006

#include "EndBoreSect.hh"
#include <iostream>
		
complex EndBoreSect::CalcZL(double freq) const
{
	// Start with z_l for a cylindrical tube or radius mRBR.
 
	// For now, just use a crude piecewise constant resistance plus 
	// frequency dependent reactance from Scavone's thesis.
	// See Fletcher and Rossing.
	double kr = 2.0 * PI * freq * mRBR / mParams.c;
	double z0 = mParams.CalcZ0(mRBR);
	double flange_factor = mRBR / mRFlange;
	double length_corr_factor = 
		0.821 - 0.135 * flange_factor - 0.073 * pow(flange_factor, 4);
	double rea = z0 * length_corr_factor * kr;
	// Reactance (im part of Z_L).
	double res = z0 * (kr < 2.0 ? 0.25 * kr * kr : 1.0);
	// Resistance (re part of Z_L).
	complex result = res + J * rea;	

	// The result is now scaled by s_p / s_s, where s_p is the cross sectional
	// surface area at the end of the cone, s_s is the spherical wave surface
	// area at the end of the cone; see Scavone, PhD Thesis.

	// For present, assume a scaling factor of 1 which should be sufficient
	// for the kinds of conic sections found in realistic flutes.

	//double scale = 1.0;

	//result *= scale;	

	return result;
}

std::ostream & EndBoreSect::print_data(std::ostream & os) const
{
	os << "   Bore left radius = " << mRBL << std::endl;
	os << "   Bore right radius = " << mRBR << std::endl;
	os << "   Flange radius = " << mLB << std::endl;
	os << "   Bore length = " << mLB << std::endl;
	return os;
}
