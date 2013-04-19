package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.exception.DimensionMismatchException;

import com.wwidesigner.util.Constants.LengthType;

public class Constraints
{
	private transient LengthType dimensionType;
	protected int numberOfHoles;
	protected String objectiveDisplayName;
	protected String objectiveFunctionName;
	protected String constraintsName;
	protected List<Constraint> constraint;
	private transient double[] lowerBounds;
	private transient double[] upperBounds;

	public Constraints()
	{
		new Constraints(LengthType.M);
	}

	public Constraints(LengthType dimensionType)
	{
		constraint = new ArrayList<Constraint>();
		this.dimensionType = dimensionType;
	}

	public Set<String> getCategories()
	{
		Set<String> categories = new LinkedHashSet<String>();
		for (Constraint value : constraint)
		{
			categories.add(value.getCategory());
		}

		return categories;
	}

	public void addConstraint(Constraint newConstraint)
	{
		if (Constraint.isValid(newConstraint))
		{
			constraint.add(newConstraint);
			newConstraint.setParent(this);
		}
	}

	public Constraint getConstraint(String category, int index)
	{
		int idx = 0;
		for (Constraint thisConstraint : constraint)
		{
			if (category.equals(thisConstraint.getCategory()))
			{
				if (idx == index)
				{
					return thisConstraint;
				}
				idx++;
			}
		}

		return null;
	}

	public void addConstraints(Constraints newConstraints)
	{
		for (Constraint thisConstraint : newConstraints.getConstraint())
		{
			addConstraint(thisConstraint);
		}
	}

	public List<Constraint> getConstraints(String category)
	{
		if (category == null || category.trim().length() == 0)
		{
			return null;
		}

		List<Constraint> catConstraints = new ArrayList<Constraint>();
		for (Constraint thisConstraint : constraint)
		{
			if (category.equals(thisConstraint.getCategory()))
			{
				catConstraints.add(thisConstraint);
			}
		}

		return catConstraints;
	}

	public void clearConstraints(String category)
	{
		for (int i = constraint.size() - 1; i >= 0; i--)
		{
			Constraint thisConstraint = constraint.get(i);
			if (thisConstraint.getCategory().equals(category))
			{
				constraint.remove(i);
			}
		}
	}

	public int getNumberOfConstraints(String category)
	{
		return getConstraints(category).size();
	}

	public int getTotalNumberOfConstraints()
	{
		return constraint.size();
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

	public String getObjectiveFunctionName()
	{
		return objectiveFunctionName;
	}

	public void setObjectiveFunctionName(String objectFunctionName)
	{
		this.objectiveFunctionName = objectFunctionName;
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

	public String getConstraintsName()
	{
		return constraintsName;
	}

	public void setConstraintsName(String constraintsName)
	{
		this.constraintsName = constraintsName;
	}

	public void setConstraint(List<Constraint> constraintList)
	{
		constraint = new ArrayList<Constraint>();
		if (constraintList != null)
		{
			for (Constraint thisConstraint : constraintList)
			{
				addConstraint(thisConstraint);
			}
		}
	}

	public List<Constraint> getConstraint()
	{
		return constraint;
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
