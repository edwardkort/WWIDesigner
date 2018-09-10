/**
 * Global constants used across the project.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		MM, CM, M, IN, FT;

		public double getMultiplierToMetres()
		{
			double multiplier;
			switch (this)
			{
				case MM:
					multiplier = 0.001d;
					break;
				case CM:
					multiplier = 0.01d;
					break;
				case IN:
					multiplier = 0.0254d;
					break;
				case FT:
					multiplier = 0.3048d;
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
					multiplier = 1000.d;
					break;
				case CM:
					multiplier = 100.d;
					break;
				case IN:
					multiplier = 39.370078740157484d;
					break;
				case FT:
					multiplier = 3.2808398950131233d;
					break;
				default:
					multiplier = 1.d;
			}

			return multiplier;
		}

		public int getDecimalPrecision()
		{
			int precision;
			switch (this)
			{
				case MM:
					precision = 2;
					break;
				case CM:
					precision = 3;
					break;
				case IN:
					precision = 3;
					break;
				case FT:
					precision = 4;
					break;
				default:
					precision = 5;
			}

			return precision;
		}
	}

	double P_AIR = 101325.0d; // Dry air pressure, Pa.
	double P_V = 0.0d; // Vapour pressure, Pa.
	double R_AIR = 287.05d; // Gas constant air.
	double R_V = 461.495d; // Gas constant water vapour.
	double GAMMA = 1.4017d; // The ratio of specific heats of air.
	double KAPPA = 2.6118e-2d; // The thermal conductivity of air.
	double C_P = 1.0063e3d; // The specific heat of air at constant pressure.
	double NU = 0.8418d; // Prandtl number.

	double CENT_FACTOR = 1.00057778951d;
	// Multiply freq by CENT_FACTOR^r to raise by r cents.
	int CENTS_IN_SEMITONE = 100;
	int CENTS_IN_OCTAVE = 1200;
	int A_SEMITONE = 9;
	double A440 = 440.0d;

	double LOG2 = Math.log(2.d);
	double BIG_DBL = 1e10d;

}
