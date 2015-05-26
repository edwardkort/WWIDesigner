/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.util.InvalidFieldHandler;

/**
 * @author kort
 * 
 */
public class Termination extends BorePoint implements TerminationInterface
{
	protected double flangeDiameter;

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

	public void convertDimensions(double multiplier)
	{
		flangeDiameter *= multiplier;
	}

	@Override
	public void checkValidity(InvalidFieldHandler handler)
	{
		if (flangeDiameter <= 0.0)
		{
			handler.logError("Termination flange diameter must be positive.");
		}
	}
}
