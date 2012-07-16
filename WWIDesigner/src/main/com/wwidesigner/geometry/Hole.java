package com.wwidesigner.geometry;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class Hole implements ComponentInterface, BorePointInterface
{
	protected String name;
	protected double height;
	protected double position;
	protected double diameter;
	protected boolean openHole;
	protected Double innerCurvatureRadius;
	protected Key key;
	
	protected double boreDiameter;

	public Hole()
	{

	}

	public Hole(double position, double diameter, double height)
	{
		this.position = position;
		this.diameter = diameter;
		this.height = height;
	}

	/**
	 * @return the radius
	 */
	public double getDiameter()
	{
		return diameter;
	}

	/**
	 * @param radius
	 *            the radius to set
	 */
	public void setDiameter(double diameter)
	{
		this.diameter = diameter;
	}

	/**
	 * @return the height
	 */
	public double getHeight()
	{
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(double height)
	{
		this.height = height;
	}

	/**
	 * @return the openHole
	 */
	public boolean isOpenHole()
	{
		return openHole;
	}

	/**
	 * @param openHole
	 *            the openHole to set
	 */
	public void setOpenHole(boolean openHole)
	{
		this.openHole = openHole;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the innerCurvatureRadius
	 */
	public Double getInnerCurvatureRadius()
	{
		return innerCurvatureRadius;
	}

	/**
	 * @param innerCurvatureRadius
	 *            the innerCurvatureRadius to set
	 */
	public void setInnerCurvatureRadius(Double innerCurvatureRadius)
	{
		this.innerCurvatureRadius = innerCurvatureRadius;
	}

	public double getBorePosition()
	{
		return position;
	}

	public void setBorePosition(double position)
	{
		this.position = position;
	}

	/**
	 * @return the key
	 */
	public Key getKey()
	{
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(Key key)
	{
		this.key = key;
	}

	public void convertDimensions(double multiplier)
	{
		height *= multiplier;
		position *= multiplier;
		diameter *= multiplier;
		boreDiameter *= multiplier;
		if (innerCurvatureRadius != null)
		{
			innerCurvatureRadius *= multiplier;
		}
		if (key != null)
		{
			key.convertDimensions(multiplier);
		}
	}

	public TransferMatrix calcTransferMatrix(double wave_number,
			PhysicalParameters mParameters)
	{
		double radius = getDiameter() / 2;
		double boreRadius = getBoreDiameter() / 2;
		Complex Zs = null;
		Complex Za = null;

		double Z0 = mParameters.calcZ0(radius);

		double delta = radius / boreRadius;

		double tm = (radius * delta / 8.)
				* (1. + 0.207 * delta * delta * delta);
		double te = height + tm;

		double ta = 0.;

		// Complex Gamma = Complex.I.multiply(wave_number);

		if (openHole) // open
		{
			double kb = wave_number * radius;
			double ka = wave_number * boreRadius;
			double xhi = 0.25 * kb * kb;

			ta = (-0.35 + 0.06 * Math.tanh(2.7 * height / radius)) * radius
					* delta * delta * delta * delta;

			Complex Zr = Complex.I.multiply(wave_number * 0.61 * radius).add(
					xhi);

			Complex Zo = (Zr.multiply(Math.cos(wave_number * te)).add(Complex.I
					.multiply(Math.sin(wave_number * te)))).divide(Complex.I
					.multiply(Zr).multiply(Math.sin(wave_number * te))
					.add(Math.cos(wave_number * te)));

			double ti = radius
					* (0.822 - 0.10 * delta - 1.57 * delta * delta + 2.14
							* delta * delta * delta - 1.6 * delta * delta
							* delta * delta + 0.50 * delta * delta * delta
							* delta * delta)
					* (1. + (1. - 4.56 * delta + 6.55 * delta * delta)
							* (0.17 * ka + 0.92 * ka * ka + 0.16 * ka * ka * ka - 0.29
									* ka * ka * ka * ka));

			Zs = Complex.I.multiply(wave_number * ti).add(Zo).multiply(Z0);

		}
		else
		{
			ta = (-0.12 - 0.17 * Math.tanh(2.4 * height / radius)) * radius
					* delta * delta * delta * delta;
			Zs = Complex.valueOf(0, -Z0 / Math.tan(wave_number * te));
		}

		Za = Complex.I.multiply(Z0 * wave_number * ta);
		Complex Za_Zs = Za.divide(Zs);

		TransferMatrix result = new TransferMatrix(Za_Zs.divide(2.).add(1.),
				Za.multiply(Za_Zs.divide(4.).add(1.)), Complex.ONE.divide(Zs),
				Za_Zs.divide(2.0).add(1.));

		return result;
	}

	@Override
	public void setBoreDiameter(double boreDiameter)
	{
		this.boreDiameter = boreDiameter;
	}

	@Override
	public double getBoreDiameter()
	{
		return boreDiameter;
	}
	public double getRatio()
	{
		return diameter / boreDiameter;
	}

	public void setRatio(double alpha)
	{
		diameter = alpha*boreDiameter;
	}
}
