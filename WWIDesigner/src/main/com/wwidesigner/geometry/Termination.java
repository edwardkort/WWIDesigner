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
	protected TerminationCalculator calculator;

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
		StateVector result = calculator.calcStateVector(wave_number, params);
		
		return result;
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
	 * @param calculator the calculator to set
	 */
	public void setCalculator(TerminationCalculator calculator)
	{
		this.calculator = calculator;
	}

}
