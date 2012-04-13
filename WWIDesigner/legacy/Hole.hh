// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef HOLE_DOT_HH
#define HOLE_DOT_HH

#include "Component.hh"

/**
 * Component representing a finger/tone hole.
 */
class Hole : public Component
{
	public:

		// ------------------------------------------------------------------
		// Life cycle:
		//
		
		Hole(const PhysParams & params,
				double rB,
				double rHExt,
				double lH,
				bool isClosed,
				double rC) : 
			Component(params),
			mRB(rB),
			mRHExt(rHExt), 
			mLH(lH), 
			mIsClosed(isClosed),
			mRC(rC), 
			mLHG(0.0), 
			mRHG(0.0), 
			mOHLB(0.0), 
			mCHLB(0.0)
		{
			// Empty.
		}

		virtual ~Hole()
		{
			// Empty.
		}

		virtual void Validate();

		// ------------------------------------------------------------------
		// Accessor functions:
		//

		/**
		 * The bore radius at the position of the hole.
		 */
		double GetRB() const
		{
			return mRB;
		}
		/**
		 * The bore radius at the position of the hole.
		 */
		void SetRB(double rB)
		{
			mRB = rB;
		}

		/**
		 * The radius of the hole. 
		 */
		double GetRHExt() const
		{
			return mRHExt;
		}
		/**
		 * The radius of the hole. 
		 */
		void SetRHExt(double rHExt)
		{
			mRHExt = rHExt;
		}

		/**
		 * The actual depth of the hole. 
		 */
		double GetLH() const
		{
			return mLH;
		}
		/**
		 * The actual depth of the hole. 
		 */
		void SetLH(double l)
		{
			mLH = l;
		}

		/**
		 * Is the hole closed?
		 */
		bool GetIsClosed() const
		{
			return mIsClosed;
		}
		/**
		 * Is the hole closed?
		 */
		void SetIsClosed(bool isClosed)
		{
			mIsClosed = isClosed;
		}

		/**
		 * The effective radius of curvature of the transition between the 
		 * hole wall and the bore or exterior of the flute.
		 */
		double GetRC() const
		{
			return mRC;
		}
		/**
		 * The effective radius of curvature of the transition between the 
		 * hole wall and the bore or exterior of the flute.
		 */
		void SetRC(double rC)
		{
			mRC = rC;
		}

		/**
		 * Effective accoustic length along the bore, when the hole is open.
		 */
		double GetOHLB() const
		{
			return mOHLB;
		}

		/**
		 * Effective accoustic length along the bore, when the hole is closed.
		 */
		double GetCHLB() const
		{
			return mCHLB;
		}

		// ------------------------------------------------------------------
		// Other member functions: 
		//
		
		/**
		 * Effective accoustic length of the hole, when it is open.
		 */
		virtual double CalcHLE(double freq) const;

		/**
		 * Specific resistance along the bore, when the hole is open.
		 */
		double CalcXi(double freq) const;

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
			return os << "Hole:" << std::endl;
		}

		virtual std::ostream & print_data(std::ostream & os) const;

		// Calculate and cache the geometric length and radius.
		virtual void CalcAndCacheRLG();

	protected:

		// ------------------------------------------------------------------
		// Protected member data:
		//

		double mRB;			// The radius of the bore at the position of the 
							// hole.
		double mRHExt;		// The external radius of the hole;
		double mLH;			// The physical length of the hole. 
		bool mIsClosed;		// Is the hole open or closed?
		double mRC;			// Effective radius of curvature of the internal 
							// and external ends of the tonehole wall, see 
							// Keefe (1990). Ithink this refers to the 
							// transition between the tonehole wall and the
							// bore or exterior of the flute.

		// The following values are calculated at validation and cached.
		double mLHG;		// The geometric length of the hole, which
							// takes into accout the effects of the hole
							// deviating from a perfect cylinder.
		double mRHG;		// The geometric radius of the hole, which 
							// takes into accout the effects of the hole
							// deviating from a perfect cylinder.
		double mOHLB;		// Effective acoustic length along the axis of the 
							// bore, when the hole is open.
		double mCHLB;		// Effective acoustic length along the axis of the 
							// bore, when the hole is closed.
};

#endif // HOLE_DOT_HH
