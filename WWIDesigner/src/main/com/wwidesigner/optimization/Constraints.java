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
	protected HoleGroups holeGroups;

	public Constraints()
	{
		constraint = new ArrayList<Constraint>();
		this.dimensionType = LengthType.M;
	}

	public Constraints(LengthType aDimensionType)
	{
		constraint = new ArrayList<Constraint>();
		this.dimensionType = aDimensionType;
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

	/**
	 * Adds the Constraint values into this merged Constraints
	 * 
	 * @param newConstraints
	 */
	public void addConstraints(Constraints newConstraints)
	{
		for (Constraint thisConstraint : newConstraints.getConstraint())
		{
			addConstraint(thisConstraint);
		}
		addNonConstraints(newConstraints);
	}

	/**
	 * Adds the non-Constraint values into this merged Constraints. Currently,
	 * only HoleGroups.
	 * 
	 * @param newConstraints
	 */
	protected void addNonConstraints(Constraints newConstraints)
	{
		HoleGroups newHoleGroups = newConstraints.getHoleGroups();
		if (newHoleGroups != null)
		{
			// Since Constraints has only one HoleGroups, replace the existing
			// one.
			this.holeGroups = newHoleGroups;
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

	public void setNumberOfHoles(int aNumberOfHoles)
	{
		this.numberOfHoles = aNumberOfHoles;
	}

	public String getObjectiveDisplayName()
	{
		return objectiveDisplayName;
	}

	public void setObjectiveDisplayName(String aObjectiveDisplayName)
	{
		this.objectiveDisplayName = aObjectiveDisplayName;
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
		if (lowerBounds == null)
		{
			extractBounds(true);
		}

		return lowerBounds;
	}

	private void extractBounds(boolean isLowerBounds)
	{
		double[] bounds = new double[getTotalNumberOfConstraints()];
		Set<String> categories = getCategories();
		int idx = 0;
		for (String category : categories)
		{
			List<Constraint> constraints = getConstraints(category);
			for (Constraint thisConstraint : constraints)
			{
				Double value;
				if (isLowerBounds)
				{
					value = thisConstraint.getLowerBound();
				}
				else
				{
					value = thisConstraint.getUpperBound();
				}
				if (value == null)
				{
					value = 0.;
				}
				bounds[idx++] = value;
			}
		}

		if (isLowerBounds)
		{
			lowerBounds = bounds;
		}
		else
		{
			upperBounds = bounds;
		}
	}

	public void setLowerBounds(double[] aLowerBounds)
	{
		validateBounds(aLowerBounds);
		this.lowerBounds = aLowerBounds;
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
			for (Constraint thisConstraint : constraints)
			{
				Double value = null;
				if (bounds != null)
				{
					value = bounds[idx++];
				}
				if (isLowerBounds)
				{
					thisConstraint.setLowerBound(value);
				}
				else
				{
					thisConstraint.setUpperBound(value);
				}
			}
		}
	}

	public void setDimensionType(LengthType aDimensionType)
	{
		this.dimensionType = aDimensionType;
	}

	public LengthType getDimensionType()
	{
		return dimensionType;
	}

	public String getConstraintsName()
	{
		return constraintsName;
	}

	public void setConstraintsName(String aConstraintsName)
	{
		this.constraintsName = aConstraintsName;
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
		if (upperBounds == null)
		{
			extractBounds(false);
		}

		return upperBounds;
	}

	public void setUpperBounds(double[] aUpperBounds)
	{
		validateBounds(aUpperBounds);
		this.upperBounds = aUpperBounds;
		updateBounds(false);
	}

	/**
	 * Used to set the transient parent value of each contained Constraint when
	 * the Constraints were constructed from an XML file.
	 */
	public void setConstraintParent()
	{
		// List<Constraint> constraintList = getConstraint();
		for (Constraint thisConstraint : constraint)
		{
			thisConstraint.setParent(this);
		}
	}

	public HoleGroups getHoleGroups()
	{
		return holeGroups;
	}

	public void setHoleGroups(HoleGroups aHoleGroups)
	{
		this.holeGroups = aHoleGroups;
	}

	public void setHoleGroups(int[][] groups)
	{
		this.holeGroups = new HoleGroups(groups);
	}

	public int[][] getHoleGroupsArray()
	{
		int[][] holeGroupsArray = null;
		if (holeGroups != null)
		{
			holeGroupsArray = holeGroups.getHoleGroupsArray();
		}

		return holeGroupsArray;
	}

}
