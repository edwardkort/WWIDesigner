// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef BORE_SECT_DOT_HH
#define BORE_SECT_DOT_HH

#include "Component.hh"

/**
 * Section of bore represented by a conic section. Takes into account viscous 
 * and thermal effects. 
 */
class BoreSect : public Component
{
	public:

		// ------------------------------------------------------------------
		// Life cycle:
		//

		BoreSect(const PhysParams & params, 
				double lB,
				double rBL, 
				double rBR) :
			Component(params),
			mLB(lB),
			mRBL(rBL),
			mRBR(rBR),
			mIsConv(false),
			mRBSmall(0.0),
			mRBLarge(0.0),
			mX0Inv(0.0)
		{
			// Empty.
		}

		virtual ~BoreSect()
		{
			// Empty.
		}

		virtual void Validate();

		// ------------------------------------------------------------------
		// Accessor functions:
		//

		/**
		 * Length of bore section. 
		 */
		double GetLB() const
		{
			return mLB;
		}
		/**
		 * Length of bore section. 
		 */
		void SetLB(double l)
		{
			mLB = l;
		} 

		/**
		 * Bore radius left (head) end. 
		 */
		double GetRBL() const
		{
			return mRBL;
		}
		/**
		 * Bore radius left (head) end. 
		 */
		void SetRBL(double r)
		{
			mRBL = r;
		} 

		/**
		 * Bore radius right (foot) end. 
		 */
		double GetRBR() const
		{
			return mRBR;
		}
		/**
		 * Bore radius right (foot) end. 
		 */
		void SetRBR(double r)
		{
			mRBR = r;
		}

		// ------------------------------------------------------------------
		// Base (re)implementation:
		//

		virtual void CalcT(TransferMatrix & t, double freq) const;

	protected:

		// ------------------------------------------------------------------
		// Protected member functions:
		//

		virtual std::ostream & print_name(std::ostream & os) const
		{
			return os << "Bore section:" << std::endl;
		}

		virtual std::ostream & print_data(std::ostream & os) const;

		// ------------------------------------------------------------------
		// Protected member data:
		//

		double mLB;				// Bore length.
		double mRBL;			// Bore radius at left (head) end.
		double mRBR;			// Bore radius at right (foot) end.

		// The following are calculated at validation:

		bool mIsConv;
		// True if the section is converging towards the right (foot) end.
		double mRBSmall;		// Bore radius at small end.
		double mRBLarge;		// Bore radius at large end.
		double mX0Inv;
		// Inverse of the 'missing length' - the length from the hypothetical 
		// apex of the cone to the small end of the conic section.
};

#endif // BORE_SECT_DOT_HH
