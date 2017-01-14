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
	protected boolean openEnd;
	
	public Termination()
	{
		this.openEnd = true;
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

	/**
	 * @return the openEnd
	 */
	public boolean isOpenEnd()
	{
		return openEnd;
	}

	/**
	 * @param openEnd - the openEnd property value to set
	 */
	public void setOpenEnd(Boolean openEnd)
	{
		if (openEnd == null)
		{
			this.openEnd = true;
		}
		else
		{
			this.openEnd = openEnd;
		}
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
		else if (flangeDiameter < 0.0)
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
