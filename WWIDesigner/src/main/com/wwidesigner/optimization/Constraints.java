package com.wwidesigner.optimization;

import java.util.ArrayList;
import java.util.List;

import com.wwidesigner.util.Constants.LengthType;

public class Constraints
{
	private List<Constraint> constraints;
	private LengthType dimensionType;

	public Constraints(LengthType dimensionType)
	{
		constraints = new ArrayList<Constraint>();
		this.dimensionType = dimensionType;
	}

	public void addConstraint(Constraint newConstraint)
	{
		constraints.add(newConstraint);
	}

	public Constraint getConstraint(int index)
	{
		return constraints.get(index);
	}

	public void addConstraints(Constraints newConstraints)
	{
		constraints.addAll(newConstraints.constraints);
	}

	public List<Constraint> getConstraints()
	{
		return constraints;
	}

	public int getNumberOfConstraints()
	{
		return constraints.size();
	}

}
