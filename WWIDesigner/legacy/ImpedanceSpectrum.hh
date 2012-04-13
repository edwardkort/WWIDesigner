// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef IMPEDANCE_SPECTRUM_DOT_HH
#define IMPEDANCE_SPECTRUM_DOT_HH

#include<map>
#include<vector>
#include "Flute.hh"

/** 
 * Representation of a complex spectrum, along with information about its 
 * extreme points.
 */
class ImpedanceSpectrum
{
	public:

		/** 
		 * Frequency vs impedance.
		 */
		const std::map<double, complex> & Spectrum() const
		{
			return mSpectrum;
		}
		/** 
		 * Frequency vs impedance.
		 */
		std::map<double, complex> & Spectrum() 
		{
			return mSpectrum;
		}

		/** 
		 * Stores the minima of the norm of the spectrum.
		 */
		const std::vector<double> & Minima() const
		{
			return mMinima;
		}
		/** 
		 * Stores the minima of the norm of the spectrum.
		 */
		std::vector<double> & Minima()
		{
			return mMinima;
		}

		/** 
		 * Stores the maxima of the norm of the spectrum.
		 */
		const std::vector<double> & Maxima() const
		{
			return mMaxima;
		}
		/** 
		 * Stores the maxima of the norm of the spectrum.
		 */
		std::vector<double> & Maxima()
		{
			return mMaxima;
		}

		/** 
		 * Add or replace a point in the spectrum.
		 */
		void SetDataPoint(const double f, const complex & z)
		{
			mSpectrum[f] = z;
		}

	private:

		std::map<double, complex> mSpectrum;
		std::vector<double> mMinima;
		std::vector<double> mMaxima;
};

void CalcImpedance(ImpedanceSpectrum & impSpect, Flute & flute, 
		double freq_start, double freq_end, int nfreq);

#endif
