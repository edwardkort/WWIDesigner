package com.wwidesigner.optimization;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.note.TuningInterface;

/**
 * Abstract class for classes that merge multiple ObjectiveFunctions into a
 * single objective function. The number of geometry dimensions is the sum of
 * the dimensions of the individual classes. The geometry dimensions of the
 * individual ObjectiveFunctions must be independent.
 * 
 * @author Burton Patkau
 * 
 */
public abstract class MergedObjectiveFunction extends BaseObjectiveFunction
{
	BaseObjectiveFunction[] components;

	/**
	 * Constructor for merged class.
	 * 
	 * @param calculator
	 * @param tuning
	 * @param evaluator
	 */
	public MergedObjectiveFunction(InstrumentCalculator calculator,
			TuningInterface tuning, EvaluatorInterface evaluator)
	{
		super(calculator, tuning, evaluator);
	}

	/**
	 * Function to calculate the dimensions of the merged class, and pull any
	 * specific bounds from the components. Derived classes must call this
	 * function at the end of their constructor, after initializing
	 * this.components.
	 */
	protected void sumDimensions()
	{
		nrDimensions = 0;
		for (BaseObjectiveFunction component : components)
		{
			nrDimensions += component.getNrDimensions();
			constraints.addConstraints(component.constraints);
		}

		lowerBounds = new double[nrDimensions];
		upperBounds = new double[nrDimensions];
		int i = 0; // Index into bounds.
		for (BaseObjectiveFunction component : components)
		{
			double[] subLower = component.getLowerBounds();
			double[] subUpper = component.getUpperBounds();
			for (int j = 0; j < component.getNrDimensions(); j++)
			{
				if (subLower != null)
				{
					lowerBounds[i] = subLower[j];
				}
				if (subUpper != null)
				{
					upperBounds[i] = subUpper[j];
				}
				++i;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.BaseObjectiveFunction#getGeometryPoint()
	 */
	@Override
	public double[] getGeometryPoint()
	{
		double[] point = new double[nrDimensions];
		int i = 0; // Index into point.
		for (BaseObjectiveFunction component : components)
		{
			double[] subPoint = component.getGeometryPoint();
			for (double x : subPoint)
			{
				point[i++] = x;
			}
		}

		return point;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.BaseObjectiveFunction#setGeometryPoint(double
	 * [])
	 */
	@Override
	public void setGeometryPoint(double[] point)
	{
		if (point.length != nrDimensions)
		{
			throw new DimensionMismatchException(point.length, nrDimensions);
		}
		int i = 0; // Index into point.
		for (BaseObjectiveFunction component : components)
		{
			double[] subPoint = new double[component.getNrDimensions()];
			for (int j = 0; j < component.getNrDimensions(); j++)
			{
				subPoint[j] = point[i++];
			}
			component.setGeometryPoint(subPoint);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.BaseObjectiveFunction#setLowerBounds(double
	 * [])
	 */
	@Override
	public void setLowerBounds(double[] lowerBounds)
	{
		super.setLowerBounds(lowerBounds);
		// Copy the lower bounds to the component ObjectiveFunctions.
		int i = 0; // Index into point.
		for (BaseObjectiveFunction component : components)
		{
			double[] subPoint = new double[component.getNrDimensions()];
			for (int j = 0; j < component.getNrDimensions(); j++)
			{
				subPoint[j] = lowerBounds[i++];
			}
			component.setLowerBounds(subPoint);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.optimization.BaseObjectiveFunction#setUpperBounds(double
	 * [])
	 */
	@Override
	public void setUpperBounds(double[] upperBounds)
	{
		super.setUpperBounds(upperBounds);
		// Copy the upper bounds to the component ObjectiveFunctions.
		int i = 0; // Index into point.
		for (BaseObjectiveFunction component : components)
		{
			double[] subPoint = new double[component.getNrDimensions()];
			for (int j = 0; j < component.getNrDimensions(); j++)
			{
				subPoint[j] = upperBounds[i++];
			}
			component.setUpperBounds(subPoint);
		}
	}

}
