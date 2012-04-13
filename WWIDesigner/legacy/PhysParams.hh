// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef PHYS_PARAMS_DOT_HH
#define PHYS_PARAMS_DOT_HH

#include <iostream>
#include "Common.hh"

/**
 * Physical Constants : SI units used
 */
class PhysParams
{
	public:
		void set(double temp_c);
		void print(std::ostream & os) const;

		/**
		 * Utility function. Calculate the wave impedance of a bore of 
		 * nominal radius r, given these parameters.
		 */
		double CalcZ0(double r) const
		{
			return rho * c / (PI * r * r);  
			// Wave impedance of a bore, nominal radius r.
		}

		double T;			// Air temp in C.
		double c;			// The speed of sound in air. m/s
		double rho; 		// The density of air. kg/m^3

		double eta; 		// The shear viscosity of air.
		double gamma; 		// The ratio of specific heats of air.
		double kappa;		// The thermal conductivity of air.
		double C_p; 		// The specific heat of air at constant pressure.
//		double nu;			// Prandtl number.
};

#endif // PHYS_PARAMS_DOT_HH
