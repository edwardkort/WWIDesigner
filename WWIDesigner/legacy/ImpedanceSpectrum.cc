// FluteCalc
// Copyright Dan Gordon, 2006

#include "ImpedanceSpectrum.hh"

void CalcImpedance(ImpedanceSpectrum & impSpect, Flute & flute, 
		double freq_start, double freq_end, int nfreq)
{
	complex prev_z = ZERO;
	double abs_prev_prev_z = 0;
	double prev_freq = 0;
	double freq_step = (freq_end - freq_start) / (nfreq - 1);
	for (int i = 0; i < nfreq; ++i)
	{
		double freq = freq_start + i * freq_step;
		complex z_ac = flute.CalcZ(freq);
		double abs_z_ac = abs(z_ac);

		impSpect.SetDataPoint(freq, z_ac);

		double abs_prev_z = abs(prev_z);

		if ((i >= 2) && 
				(abs_prev_z < abs_z_ac) && 
				(abs_prev_z < abs_prev_prev_z))
		{
			// We have found an impedance minimum.
			impSpect.Minima().push_back(prev_freq);
		}

		if ((i >= 2) && 
				(abs_prev_z > abs_z_ac) && 
				(abs_prev_z > abs_prev_prev_z))
		{
			// We have found an impedance maximum.
			impSpect.Maxima().push_back(prev_freq);
		}

		abs_prev_prev_z = abs_prev_z;
		prev_z = z_ac;
		prev_freq = freq;
	}
}
