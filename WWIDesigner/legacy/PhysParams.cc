// FluteCalc
// Copyright Dan Gordon, 2006

#include "PhysParams.hh"

void PhysParams::set(double temp_c)
{
	T = temp_c + 273.15;

	c = 332.0 * (1.0 + 0.00166 * temp_c);

	// See Wikipedia:
	const double p_air = 101325.0;		// Dry air pressure, Pa.
	const double p_v = 0.0;				// Vapour pressure, Pa.
	const double r_air = 287.05;		// Gas constant air.
	const double r_v = 461.495;			// Gas constant water vapour.

	rho = ((p_air / r_air) + (p_v / r_v)) / T;

	eta = 3.648e-6 * (1 + 0.0135003 * T); 
							// The shear viscosity of air. See Nederveen.

	gamma = 1.4017; 		// The ratio of specific heats of air.
	kappa = 2.6118e-2;		// The thermal conductivity of air.
	C_p = 1.0063e3; 		// The specific heat of air at constant pressure.
	// nu = 0.8418;			// Prandtl number. Not used.
}

void PhysParams::print(std::ostream & os) const
{
	os << "Physical Parameters : " << std::endl;
	os << "Temp = " << T << std::endl;
	os << "c = " << c << std::endl;
	os << "rho = " << rho << std::endl;
	os << "eta = " << eta << std::endl;
	os << "gamma = " << gamma << std::endl;
	os << "kappa = " << kappa << std::endl;
	os << "C_p = " << C_p << std::endl;
//	os << "nu = " << nu << std::endl;
	os << std::endl;
}
