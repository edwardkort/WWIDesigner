package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.util.Constants.LengthType;

public class Constraints
{
	private Map<String, List<Constraint>> constraintsMap;
	private LengthType dimensionType;
	private int numberOfHoles;
	private String objectiveDisplayName;
	private String objectFunctionName;
	private double[] lowerBounds;
	private double[] upperBounds;

	public Constraints(LengthType dimensionType)
	{
		constraintsMap = new LinkedHashMap<String, List<Constraint>>(6);
		this.dimensionType = dimensionType;
	}

	public Set<String> getCategories()
	{
		return constraintsMap.keySet();
	}

	public void addConstraint(Constraint newConstraint)
	{
		if (Constraint.isValid(newConstraint))
		{
			String category = newConstraint.getCategory();
			newConstraint.setParent(this);
			List<Constraint> catConstraints = getConstraints(category);
			catConstraints.add(newConstraint);
		}
	}

	public Constraint getConstraint(String category, int index)
	{
		return constraintsMap.get(category).get(index);
	}

	public void addConstraints(Constraints newConstraints)
	{
		Set<String> categories = newConstraints.getCategories();
		for (String category : categories)
		{
			List<Constraint> catConstraints = newConstraints
					.getConstraints(category);
			for (Constraint constraint : catConstraints)
			{
				constraint.setParent(this);
				addConstraint(constraint);
			}
		}
	}

	public List<Constraint> getConstraints(String category)
	{
		if (category == null || category.trim().length() == 0)
		{
			return null;
		}

		List<Constraint> catConstraints = constraintsMap.get(category);
		if (catConstraints == null)
		{
			catConstraints = new ArrayList<Constraint>();
			constraintsMap.put(category, catConstraints);
		}

		return catConstraints;
	}

	public void clearConstraints(String category)
	{
		constraintsMap.remove(category);
	}

	public int getNumberOfConstraints(String category)
	{
		return constraintsMap.get(category).size();
	}

	public int getTotalNumberOfConstraints()
	{
		int num = 0;
		Set<String> categories = getCategories();
		for (String category : categories)
		{
			num += getNumberOfConstraints(category);
		}

		return num;
	}

	public int getNumberOfHoles()
	{
		return numberOfHoles;
	}

	public void setNumberOfHoles(int numberOfHoles)
	{
		this.numberOfHoles = numberOfHoles;
	}

	public String getObjectiveDisplayName()
	{
		return objectiveDisplayName;
	}

	public void setObjectiveDisplayName(String objectiveDisplayName)
	{
		this.objectiveDisplayName = objectiveDisplayName;
	}

	public String getObjectFunctionName()
	{
		return objectFunctionName;
	}

	public void setObjectFunctionName(String objectFunctionName)
	{
		this.objectFunctionName = objectFunctionName;
	}

	public double[] getLowerBounds()
	{
		return lowerBounds;
	}

	public void setLowerBounds(double[] lowerBounds)
	{
		validateBounds(lowerBounds);
		this.lowerBounds = lowerBounds;
		updateBounds(true);
	}

	private void updateBounds(boolean isLowerBounds)
	{
		Set<String> categories = getCategories();
		int idx = 0;
		double[] bounds = isLowerBounds ? lowerBounds : upperBounds;
		for (String category : categories)
		{
			List<Constraint> constraints = getConstraints(category);
			for (Constraint constraint : constraints)
			{
				Double value = null;
				if (bounds != null)
				{
					value = bounds[idx++];
				}
				if (isLowerBounds)
				{
					constraint.setLowerBound(value);
				}
				else
				{
					constraint.setUpperBound(value);
				}
			}
		}
	}

	public LengthType getDimensionType()
	{
		return dimensionType;
	}

	private void validateBounds(double[] bounds)
	{
		int inputSize = bounds != null ? bounds.length : 0;
		int expectedSize = getTotalNumberOfConstraints();
		if (bounds != null && inputSize != expectedSize)
		{
			throw new DimensionMismatchException(inputSize, expectedSize);
		}
	}

	public double[] getUpperBounds()
	{
		return upperBounds;
	}

	public void setUpperBounds(double[] upperBounds)
	{
		validateBounds(upperBounds);
		this.upperBounds = upperBounds;
		updateBounds(false);
	}

}
