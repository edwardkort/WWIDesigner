// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef COMPONENT_DOT_HH
#define COMPONENT_DOT_HH

#include "Common.hh"
#include "PhysParams.hh"
#include "TransferMatrix.hh"
#include <iostream>

/**
 * An abstract base class for a four port acoustical component. For any given
 * frequency, it is possible to calculate a transfer matrix. The transfer 
 * matrix, when it multiplies a vector containing the acoustic pressure and 
 * volume flow at the input end, gives the corresponding vector at the 
 * output end. 
 */
class Component
{
	public:

		// ------------------------------------------------------------------
		// Friends:
		//

		/**
		 *  Stream output operator. 
		 */
		friend std::ostream & operator<<(std::ostream & os, 
		                                 const Component & t)
		{
			return t.print(os);
		}

		// ------------------------------------------------------------------
		// Life cycle:
		//

		Component(const PhysParams & params) : mIsValid(false), mParams(params)
		{
			// Empty.	
		}

		virtual ~Component()
		{
			// Empty.
		}

		/**
		 *  Should be called before any work is done. 
		 */
		virtual void Validate()
		{
			mIsValid = true;
		}

		// ------------------------------------------------------------------
		// Accessor functions:
		//

		/**
		 *  Calculate the transfer matrix at frequency \a freq. 
		 */
		virtual void CalcT(TransferMatrix & t, double freq) const = 0;

	protected:

		// ------------------------------------------------------------------
		// Protected member data:
		//

		bool mIsValid;

		PhysParams mParams;

	private:

		// ------------------------------------------------------------------
		// Private member functions:
		//

		virtual std::ostream & print(std::ostream & os) const
		{
			print_name(os);
			print_data(os);
			return os;
		}
		
		virtual std::ostream & print_name(std::ostream & os) const
		{
			return os << "Component:" << std::endl;
		}

		virtual std::ostream & print_data(std::ostream & os) const
		{
			return os;
		}
};

#endif // COMPONENT_DOT_HH
