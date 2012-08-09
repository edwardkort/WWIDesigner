/**
 * 
 */
package com.wwidesigner.geometry;

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

}
