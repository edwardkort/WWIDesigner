/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class Termination extends BorePoint implements TerminationInterface
{
	protected double flangeDiameter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.TerminationInterface#calcStateVector(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public StateVector calcStateVector(double wave_number,
			PhysicalParameters params)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the flangeDiameter
	 */
	public double getFlangeDiameter()
	{
		return flangeDiameter;
	}

	/**
	 * @param flangeDiameter
	 *            the flangeDiameter to set
	 */
	public void setFlangeDiameter(double flangeDiameter)
	{
		this.flangeDiameter = flangeDiameter;
	}

}
