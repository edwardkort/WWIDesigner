/**
 * 
 */
package com.wwidesigner.geometry;

import com.wwidesigner.util.InvalidFieldException;

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
	public Exception checkValidity()
	{
		if (flangeDiameter <= boreDiameter)
		{
			return new InvalidFieldException("Instrument", "Termination flange diameter must be greater than bore diameter.");
		}
		return null;
	}
}
