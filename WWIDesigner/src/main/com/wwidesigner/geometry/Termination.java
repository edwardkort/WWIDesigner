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
	
	public Termination()
	{
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

	public void convertDimensions(double multiplier)
	{
		flangeDiameter *= multiplier;
	}

	public void checkValidity(InvalidFieldHandler handler,
			BorePoint terminalBorePoint)
	{
		if (Double.isNaN(flangeDiameter))
		{
			handler.logError("Termination flange diameter must be specified.");
		}
		else if (flangeDiameter < 0.0d)
		{
			handler.logError("Termination flange diameter must be positive.");
		}
		else if (flangeDiameter < this.boreDiameter)
		{
			handler.logError(
					"Termination flange diameter must not be less than bore diameter.");
		}
		else if ((terminalBorePoint != null) && ((terminalBorePoint.boreDiameter
				- flangeDiameter) > 0.00001d))
		{
			handler.logError(
					"Termination flange diameter must not be less than bore diameter.");
		}
	}
}
