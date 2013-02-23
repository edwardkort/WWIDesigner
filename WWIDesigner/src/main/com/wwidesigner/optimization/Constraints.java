package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wwidesigner.util.Constants.LengthType;

public class Constraints
{
	private Map<String, List<Constraint>> constraints;
	private LengthType dimensionType;

	public Constraints(LengthType dimensionType)
	{
		constraints = new HashMap<String, List<Constraint>>();
		this.dimensionType = dimensionType;
	}

	public Set<String> getCategories()
	{
		return constraints.keySet();
	}

	public void addConstraint(Constraint newConstraint)
	{
		if (Constraint.isValid(newConstraint))
		{
			String category = newConstraint.getCategory();
			List<Constraint> catConstraints = getConstraints(category);
			catConstraints.add(newConstraint);
		}
	}

	public Constraint getConstraint(String category, int index)
	{
		return constraints.get(category).get(index);
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

		List<Constraint> catConstraints = constraints.get(category);
		if (catConstraints == null)
		{
			catConstraints = new ArrayList<Constraint>();
			constraints.put(category, catConstraints);
		}

		return catConstraints;
	}

	public void clearConstraints(String category)
	{
		constraints.remove(category);
	}

	public int getNumberOfConstraints(String category)
	{
		return constraints.get(category).size();
	}

}
