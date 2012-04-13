// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef SILVER_FLUTE_HOLE_DOT_HH
#define SILVER_FLUTE_HOLE_DOT_HH

#include "Hole.hh"

/**
 * Adds the presence of a pad above the hole. Also deals with the different 
 * geometry of the hole itself.
 */
class SilverFluteHole : public Hole
{
	public:

		SilverFluteHole(const PhysParams & params,
				double rB,
				double rHExt,
				double lH,
				bool isClosed,
				double rC,
				double padHeight,
				double rPad);

		/**
		 * Effective accoustic length of the hole when it is open.
		 */
		virtual double CalcHLE(double freq) const;

	protected:

		// Calculate and cache the geometric length and radius.
		// Overrides base class method.
		virtual void CalcAndCacheRLG();

		double mPadHeight;
		double mRPad;
};

#endif
