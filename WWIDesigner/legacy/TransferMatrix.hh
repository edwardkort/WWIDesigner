// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef TRANSFER_MATRIX_DOT_HH
#define TRANSFER_MATRIX_DOT_HH

#include "Common.hh"
#include <ostream>

/**
 * Simple 2x2 complex matrix. Here e.g. PU represents the component of the
 * output pressure that depends on the input volume flow, etc.
 */
class TransferMatrix
{

	friend std::ostream & operator<<(std::ostream & os, 
			const TransferMatrix & t);

	public:

		TransferMatrix():
			mPP(ZERO),
			mPU(ZERO),
			mUP(ZERO),
			mUU(ZERO)
		{
			// Empty.
		}

		TransferMatrix(complex pp, complex pu, complex up, complex uu) :
			mPP(pp),
			mPU(pu),
			mUP(up),
			mUU(uu)
		{
			// Empty.
		}

		TransferMatrix(const TransferMatrix & from) :
			mPP(from.mPP),
			mPU(from.mPU),
			mUP(from.mUP),
			mUU(from.mUU)
		{
			// Empty.
		}
		void operator=(const TransferMatrix & rhs)
		{
			mPP = rhs.mPP;
			mPU = rhs.mPU;
			mUP = rhs.mUP;
			mUU = rhs.mUU;
		}

		complex mPP;	
		complex mPU;	
		complex mUP;	
		complex mUU;	
};

/**
 * Simple 2x1 complex vector representing the state of the air column at a 
 * point in the bore. P refers to the pressure and U to the volume flow.
 */
class StateVector
{

	friend std::ostream & operator<<(std::ostream & os, 
			const StateVector & t);

	public:

		StateVector():
			mP(ZERO),
			mU(ZERO)
		{
			// Empty.
		}

		StateVector(complex p, complex u) :
			mP(p),
			mU(u)
		{
			// Empty.
		}

		StateVector(const StateVector & from) :
			mP(from.mP),
			mU(from.mU)
		{
			// Empty.
		}
		void operator=(const StateVector & rhs)
		{
			mP = rhs.mP;
			mU = rhs.mU;
		}

		complex mP;	
		complex mU;	
};

TransferMatrix operator*(
		const TransferMatrix & lhs, 
		const TransferMatrix & rhs);

StateVector operator*(
		const TransferMatrix & lhs,
		const StateVector & rhs);

#endif // TRANSFER_MATRIX_DOT_HH
