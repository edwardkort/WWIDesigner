// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef TERMINATION_DOT_HH
#define TERMINATION_DOT_HH

/**
 * Class representing a load impedance at the terminal (foot) end of the
 * instrument.
 */
class Termination
{
	public:

		// Terminal load (impedance):
		virtual complex CalcZL(double freq) const
		{
			return 0.0;
		}
};

#endif
