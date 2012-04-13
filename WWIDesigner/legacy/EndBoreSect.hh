// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef END_CONIC_BORE_SECT_DOT_HH
#define END_CONIC_BORE_SECT_DOT_HH

#include "BoreSect.hh"
#include "Termination.hh"

/**
 * Models the flanged termination that occurs at the right hand (foot) end
 * of a flute.
 */
class EndBoreSect : private BoreSect, public Termination
{
	public:
		
		// ------------------------------------------------------------------
		// Life cycle:
		//

		EndBoreSect(const BoreSect & bs, double rFlange) : 
			BoreSect(bs),
			mRFlange(rFlange)
		{
			// Empty.
		}

		virtual void Validate()
		{
			BoreSect::Validate();
		}

		// ------------------------------------------------------------------
		// Accessor functions:
		//

		/**
		 * The large radius of the flange.
		 */
		double GetRFlange()
		{
			return mRFlange;
		}
		/**
		 * The large radius of the flange.
		 */
		void SetRFlange(double rFlange)
		{
			mRFlange = rFlange;
		}

		// ------------------------------------------------------------------
		// Base (re)implementation:
		//
		
		virtual complex CalcZL(double freq) const;

	protected:

		// ------------------------------------------------------------------
		// Protected member functions:
		//
		
		virtual std::ostream & print_name(std::ostream & os) const
		{
			return os << "Terminal bore section:" << std::endl;
		}
		
		virtual std::ostream & print_data(std::ostream & os) const;

		// ------------------------------------------------------------------
		// Protected member data:
		//
		
		double mRFlange;
		// The radius of the outer edge of the flange.
};

#endif // END_CONIC_BORE_SECT_DOT_HH
