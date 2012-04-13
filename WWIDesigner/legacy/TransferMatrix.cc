// FluteCalc
// Copyright Dan Gordon, 2006

#include "TransferMatrix.hh"
#include <iomanip>

TransferMatrix operator*(const TransferMatrix & lhs, const TransferMatrix & rhs)
{
	return TransferMatrix(
			lhs.mPP * rhs.mPP + lhs.mPU * rhs.mUP,
			lhs.mPP * rhs.mPU + lhs.mPU * rhs.mUU,
			lhs.mUP * rhs.mPP + lhs.mUU * rhs.mUP,
			lhs.mUP * rhs.mPU + lhs.mUU * rhs.mUU);
}

StateVector operator*(const TransferMatrix & lhs, const StateVector & rhs)
{
	return StateVector(
			lhs.mPP * rhs.mP + lhs.mPU * rhs.mU,
			lhs.mUP * rhs.mP + lhs.mUU * rhs.mU);
}

	
std::ostream & operator<<(std::ostream & os, const TransferMatrix & t)
{
	using namespace std;
	ios_base::fmtflags oldflags = os.flags();
	os << endl;
	os << setw(32) << t.mPP 
		<< setw(32) << t.mPU << endl;
	os << setw(32) << t.mUP 
		<< setw(32) << t.mUU << endl << resetiosflags(oldflags);
	return os;
}

std::ostream & operator<<(std::ostream & os, const StateVector & t)
{
	using namespace std;
	ios_base::fmtflags oldflags = os.flags();
	os 
		<< setw(32) << t.mP << endl
		<< setw(32) << t.mU << endl << resetiosflags(oldflags);
	return os;
}
