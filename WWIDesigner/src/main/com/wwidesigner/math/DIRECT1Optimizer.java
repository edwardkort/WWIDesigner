/**
 * Variant of the DIRECT optimization algorithm.
 * 
 * Copyright (C) 2016, Burton Patkau, Edward Kort, Antoine Lefebvre.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.wwidesigner.math;

import java.util.Arrays;

/**
 * Variant of the DIRECT optimization algorithms.
 * For other than hypercubes, divides only one long side from
 * each hyperrectangle.  This strategy is suggested in:
 * 
 *     D.R. Jones. The DIRECT Global Optimization Algorithm.
 *     The Encyclopedia of Optimization. Kluwer Academic, 1999.
 *
 * Instead of choosing the one long side at random as suggested there,
 * we track the improvement in function value observed on the latest
 * divisions in each dimension, and choose the long side showing the
 * greatest improvement.
 */
public class DIRECT1Optimizer extends DIRECTOptimizer
{
	public DIRECT1Optimizer()
	{
		this(DEFAULT_X_THRESHOLD);
	}

	public DIRECT1Optimizer(double convergenceThreshold)
	{
		super(convergenceThreshold);
	}

	/**
	 * For a specified rectangle, choose which sides to use for dividing the rectangle.
	 */
	@Override
	protected EligibleSides selectEligibleSides(RectangleValue rectangle)
	{
		EligibleSides eligibleSides = new EligibleSides(rectangle.getWidth().length);
		boolean isHypercube = true;		// if all long sides have non-zero width.
		int nrEligibleSides = rectangle.getLongCount();
		int eligibleSide = rectangle.getLongIdx();
		int i;
		double highestPotential;
		
		// For hypercubes, divide on all sides.
		for (i = 0; i < rectangle.getWidth().length; ++i)
		{
			eligibleSides.setEligible(i, rectangle.isLongSide(i));
			if (! rectangle.isLongSide(i) && boundDifference[i] > 0.0)
			{
				isHypercube = false;
			}
		}
		if (! isHypercube)
		{
			// Divide on only the long side with the most potential.
			highestPotential = -Double.MAX_VALUE;
			double[] potential = rectangle.getPotential();
			if (potential == null)
			{
				potential = new double[rectangle.getWidth().length];
				Arrays.fill(potential, 0.0);
			}
			nrEligibleSides = 1;
			for (i = 0; i < rectangle.getWidth().length; ++i)
			{
				if (rectangle.isLongSide(i) 
						&& potential[i] > highestPotential)
				{
					highestPotential = potential[i];
					eligibleSide = i;
				}
			}
		}
		eligibleSides.setNrEligibleSides(nrEligibleSides);
		eligibleSides.setEligibleSide(eligibleSide);
		return eligibleSides;
	}

	@Override
	protected void calculatePotential(RectangleValue rectangle, double[] basePotential,
			int dimension, double thisF, double neighbourF, double baseline)
	{
		double[] potential = rectangle.getPotential();
		double newPotential = (neighbourF - thisF);
		if (potential == null)
		{
			if (basePotential == null)
			{
				potential = new double[rectangle.getWidth().length];
				Arrays.fill(potential, 0.0);
			}
			else
			{
				potential = Arrays.copyOf(basePotential, basePotential.length);
			}
			rectangle.setPotential(potential);
		}
		if (newPotential >= rectangle.getPotential()[dimension])
		{
			// When potential increases, use new figure immediately.
			rectangle.setPotential(dimension, newPotential);
		}
		else
		{
			// When potential decreases, only decrease half way.
			rectangle.setPotential(dimension,
					0.5 * (newPotential + rectangle.getPotential()[dimension]));
		}
	}

}
