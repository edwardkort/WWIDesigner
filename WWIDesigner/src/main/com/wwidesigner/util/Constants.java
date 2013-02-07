/**
 * 
 */
package com.wwidesigner.util;

/**
 * @author kort
 * 
 */
public interface Constants
{

	enum TemperatureType implements Constants
	{
		C, F;
	}

	enum LengthType implements Constants
	{
		CM, MM, FT, IN, M;

		public double getMultiplierToMetres()
		{
			double multiplier;
			switch (this)
			{
				case MM:
					multiplier = 0.001;
					break;
				case CM:
					multiplier = 0.01;
					break;
				case IN:
					multiplier = 0.0254;
					break;
				default:
					multiplier = 1.;
			}

			return multiplier;
		}

		public double getMultiplierFromMetres()
		{
			double multiplier;
			switch (this)
			{
				case MM:
					multiplier = 1000.;
					break;
				case CM:
					multiplier = 100.;
					break;
				case IN:
					multiplier = 39.3701;
					break;
				default:
					multiplier = 1.;
			}

			return multiplier;
		}
	}

	double P_AIR = 101325.0; // Dry air pressure, Pa.
	double P_V = 0.0; // Vapour pressure, Pa.
	double R_AIR = 287.05; // Gas constant air.
	double R_V = 461.495; // Gas constant water vapour.
	double GAMMA = 1.4017; // The ratio of specific heats of air.
	double KAPPA = 2.6118e-2; // The thermal conductivity of air.
	double C_P = 1.0063e3; // The specific heat of air at constant pressure.
	double NU = 0.8418; // Prandtl number.

	double CENT_FACTOR = 1.00057778951;
	// Multiply freq by CENT_FACTOR^r to raise by r cents.
	int CENTS_IN_SEMITONE = 100;
	int CENTS_IN_OCTAVE = 1200;
	int A_SEMITONE = 9;
	double A440 = 440.0;

	double LOG2 = Math.log(2);
	double BIG_DBL = 1e10;

}
