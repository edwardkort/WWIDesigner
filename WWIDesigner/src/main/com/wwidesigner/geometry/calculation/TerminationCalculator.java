/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import com.wwidesigner.geometry.Termination;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public abstract class TerminationCalculator
{
	public TerminationCalculator()
	{
	}

	/**
	 * Return a state vector describing the specified termination,
	 * assuming the bore end is open.
	 * 
	 * @param termination
	 * @param wave_number
	 * @param params
	 * @return [P, U] state vector.
	 */
	public StateVector calcStateVector(Termination termination,
			double wave_number, PhysicalParameters params)
	{
		return calcStateVector(termination, true, wave_number, params);
	}

	/**
	 * Return a state vector describing the specified termination.
	 * 
	 * @param termination
	 * @param isOpen - true if the bore end is open, false if it is closed.
	 * @param wave_number
	 * @param params
	 * @return [P, U] state vector.
	 */
	public abstract StateVector calcStateVector(Termination termination,
			boolean isOpen, double wave_number, PhysicalParameters params);
}
