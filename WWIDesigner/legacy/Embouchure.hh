// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef EMBOUCHURE_DOT_HH
#define EMBOUCHURE_DOT_HH

#include "Component.hh"

/**
 * Component modelling the embouchure hole plus the stopper cavity. 
 */
class Embouchure : public Component
{
	public:

		// ------------------------------------------------------------------
		// Life cycle:
		//
	
		Embouchure(const PhysParams & params, 
				double RB, 
				double LChar, 
				double LCav):
			Component(params),
			mRB(RB),
			mLChar(LChar),
			mLCav(LCav)
		{
			// Empty.
		}


		virtual ~Embouchure()
		{
			// Empty.
		}
		
		virtual void Embouchure::Validate();

		// ------------------------------------------------------------------
		// Accessor functions:
		//

		/**
		 * The local radius of the bore. 
		 */
		double GetRB() const
		{
			return mRB;
		}
		/**
		 * The local radius of the bore. 
		 */
		void SetRB(double rB)
		{
			mRB = rB;
		}

		/**
		 * LChar, a characteristic length for the embouchure, equal to 
		 * the surface area of the hole divided by the effective length 
		 * of the hole. 
		 */
		double GetLChar() const
		{
			return mLChar;
		}
		/**
		 * LChar, a characteristic length for the embouchure, equal to 
		 * the surface area of the hole divided by the effective length 
		 * of the hole. 
		 */
		void SetLChar(double lChar)
		{
			mLChar = lChar;
		}
		
		/**
		 * The length of the stopper cavity. 
		 */
		double GetLCav() const
		{
			return mLCav;
		}
		/**
		 * The length of the stopper cavity. 
		 */
		void SetLCav(double lCav)
		{
			mLCav = lCav;
		}

		// ------------------------------------------------------------------
		// Base (re)implementation:
		//
	
		virtual void CalcT(TransferMatrix & t, double freq) const;
	
	private:

		// ------------------------------------------------------------------
		// Private member functions:
		//
		
		virtual std::ostream & print_name(std::ostream & os) const
		{
			return os << "Embouchure:" << std::endl;
		}

		virtual std::ostream & print_data(std::ostream & os) const;

		double CalcKDeltaL(double freq) const;

		double CalcJYE(double freq) const;
		
		double CalcJYC(double freq) const;

		// ------------------------------------------------------------------
		// Private member data:
		//

		double mRB;				// The bore radius.
		double mLChar;			// LChar, the characteristic length for the 
								// embouchure, equal to the surface area of 
								// the hole divided by the effective length of 
								// the hole. 
		double mLCav;			// Length of the stopper cavity.
};

#endif // EMBOUCHURE_DOT_HH
