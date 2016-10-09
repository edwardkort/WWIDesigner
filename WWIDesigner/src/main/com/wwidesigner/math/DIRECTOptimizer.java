/**
 * Re-implementation of the DIRECT and DIRECT-L algorithms
 * described in:
 *
 *		D. R. Jones, C. D. Perttunen, and B. E. Stuckmann,
 *		"Lipschitzian optimization without the lipschitz constant,"
 *		J. Optimization Theory and Applications, vol. 79, p. 157 (1993).
 *
 *		J. M. Gablonsky and C. T. Kelley, "A locally-biased form
 *		of the DIRECT algorithm," J. Global Optimization 21 (1),
 *		p. 27-37 (2001).
 *
 * Original implementation in C by Steven G. Johnson
 * Translated to Java and adapted by Burton Patkau
 *
 * Author's Note, Steven G. Johnson
 * --------------------------------
 * I re-implemented the algorithms for a couple of reasons.  First,
 * because I was interested in the algorithms and wanted to play with
 * them by trying some variations (originally, because I wanted to
 * experiment with a hybrid approach combining DIRECT with local search
 * algorithms, see hybrid.c).  Second, I wanted to remove some arbitrary
 * restrictions in the original Fortran code, e.g. a fixed upper bound on
 * the number of function evaluations.  Third, because it was fun to
 * code.  As far as I can tell, my version converges in about the same
 * number of iterations as Gablonsky's code (with occasional slight
 * differences due to minor differences in how I break ties, etc.).
 *
 * Original work: Copyright (c) 2007-2014 Massachusetts Institute of Technology
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * --------------------------------
 * 
 * Translator's Note: Burton Patkau
 * 
 * This implementation uses the following convergence criterion:
 * 
 *   The resolution of all the x (independent) variables of the solution
 *   found is within a specified fraction (convergenceThreshold) of the
 *   distance between the bounds for their respective dimensions, and no 
 *   other solution (hyperrectangle) examined in the current iteration shows
 *   promise of providing a better solution.  When dividing a hyperrectangle,
 *   a new point "shows promise" if a line through the original centre
 *   and the new point leads to a lower value than the current best when
 *   extrapolated to either edge of the hyperrectangle being divided.
 *
 * With DIRECT, the current best solution can change dramatically from
 * iteration to iteration.  Thus, typical convergence criteria that look
 * at improvement from one iteration to the next are at considerable
 * risk of false positives.
 * 
 * The conventional definition of potentially optimal hyperrectangles
 * (POH) in DIRECT requires some K > 0 s.t.:
 * 
 *     f(cj) - K*dj <= f(ci) - K*di, for all i = 1..m
 *     f(cj) - K*dj <= fmin - eps * |fmin|
 * 
 * This implementation computes the convex hull to fulfill the first
 * condition, but instead of the second condition, it enforces K > 0
 * (K != 0) when pruning the convex hull.  Instead of the lower half
 * of the convex hull, we select only the lower right quarter of the
 * convex hull.  This effectively fulfills "eps arbitrarily small
 * but non-zero" without having to use eps.  The POH hull will always
 * include a hyperrectangle containing fmin; if there is more than one
 * such hyperrectangle, it will include the one(s) with the largest
 * diameter.
 * 
 * Java implementation: Copyright (C) 2016, Burton Patkau, Edward Kort, Antoine Lefebvre.
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
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.util.FastMath;

/**
 * Implementation of the DIRECT and DIRECT-L optimization algorithms
 * described in:
 *
 *		D. R. Jones, C. D. Perttunen, and B. E. Stuckmann,
 *		"Lipschitzian optimization without the lipschitz constant,"
 *		J. Optimization Theory and Applications, vol. 79, p. 157 (1993).
 *
 *		J. M. Gablonsky and C. T. Kelley, "A locally-biased form
 *		of the DIRECT algorithm," J. Global Optimization 21 (1),
 *		p. 27-37 (2001).
 */
public class DIRECTOptimizer extends MultivariateOptimizer
{
	public static final double DEFAULT_X_THRESHOLD = 1.0e-4;

	private static final double THIRD = 0.3333333333333333333333d;
	private static final double EQUAL_SIDE_TOL = 5e-2;		// tolerance to equate side sizes
	private static final double DIAMETER_GRANULARITY = 1.0e-13;
	protected static final boolean DISPLAY_PROGRESS = true;	// Debugging output.

	/** which measure of hyper-rectangle diameter to use:
	 *  0 = Jones, centre-to-vertex distance
	 *  1 = Gablonsky, half longest side
	 */
	protected int optDiam_longSide;

	/** which way to divide rects:
	 *  0: orig. Jones, divide all longest sides
	 *  1: divide all sides for hypercubes, otherwise divide first long side
	 *  2: for hypercubes, divide sides with above-average potential,
	 *     otherwise divide long side with most potential, based on
	 *     improvement in function value for last division in each dimension.
	 */
	protected int optDivide_oneSide;

	/** which rectangles are considered "potentially optimal"
	 *  0: Jones, all points on convex hull, even equal points
	 *  1: Gablonsky, pick one of each equal point
	 */
	protected int optHull_onePoint;
 
	/**
	 * Desired accuracy in x values required for convergence,
	 * relative to distance between bounds.
	 */
	protected double convergenceThreshold;

	/**
	 * Workspace of function values when dividing a rectangle.
	 * fv[2*i] is value at point below centre in dimension i,
	 * fv[2*i+1] is value at point above centre in dimension i.
	 */
	protected double[] fv;
	
	/**
	 * Array of dimension indexes, used for indirect sort of fv.
	 */
	protected Integer[] isort;
	
	/**
	 * Best point found so far.
	 */
	protected PointValuePair currentBest;
	
	/**
	 * Worst value found so far, used for infeasible points.
	 */
	protected double fMax;
	
	/** Differences between the upper and lower bounds. */
	private double[] boundDifference;

	public DIRECTOptimizer()
	{
		this(DEFAULT_X_THRESHOLD);
	}

	public DIRECTOptimizer(double convergenceThreshold)
	{
		super(null);		// No standard convergence checker.
		this.convergenceThreshold = convergenceThreshold;
		optDiam_longSide = 0;		// Jones diameter measure.
		optDivide_oneSide  = 2;		// Jones long side division.
		optHull_onePoint  = 0;		// Jones hull selection: allow duplicate points.
	}

	/* Basic data structure:
	*
	* A hyper-rectangle has a Rectangle Key, with the value (f) of the
	* function at the center, the "size" measure (d) of the rectangle,
	* an "age" measure for tie-breaking purposes, and a RectangleValue with
	* the coordinates of the center (c) in absolute terms, and the widths
	* of the sides (w) relative to boundDifference.
	*
	* We store the hyper-rectangles in a red-black tree, sorted by (d,f)
	* in lexicographic order, to allow us to perform quick convex-hull
	* calculations (in the future, we might make this data structure
	* more sophisticated based on the dynamic convex-hull literature).
	*/
	protected int nextSerial;			// Serial number for next new rect
	protected class RectangleKey implements Comparable<RectangleKey>
	{
		protected double diameter;	// "Size" of the rectangle.
		protected double fValue;	// Value of the function at the centre.
		protected int serial;		// Serial nr of rectangle, for tie-breaking purposes.

		/**
		 * Create the key for a real rectangle, of specified diameter and
		 * function value.
		 * @param diameter - "diameter" measure of the rectangle
		 * @param fValue - function value at centre of rectangle
		 */
		public RectangleKey(double diameter, double fValue)
		{
			this.diameter = diameter;
			this.fValue = fValue;
			this.serial = ++nextSerial;
		}

		/**
		 * Create an index reference, used for searching the rectangle tree for
		 * rectangles before or after a specified diameter.
		 * @param diameter
		 */
		public RectangleKey(double diameter)
		{
			this.diameter = diameter;
			this.fValue = -Double.MAX_VALUE;
			this.serial = 0;
		}

		public double getDiameter()
		{
			return diameter;
		}

		public double getfValue()
		{
			return fValue;
		}

		@Override
		public int compareTo(RectangleKey arg0)
		{
			if (this.diameter > arg0.diameter)
			{
				return 1;
			}
			if (this.diameter < arg0.diameter)
			{
				return -1;
			}
			if (this.fValue > arg0.fValue)
			{
				return 1;
			}
			if (this.fValue < arg0.fValue)
			{
				return -1;
			}
			if (this.serial > arg0.serial)
			{
				return 1;
			}
			if (this.serial < arg0.serial)
			{
				return -1;
			}
			if (this.equals(arg0))
			{
				return 0;
			}
			// Should not occur.
			return (int) this.hashCode() - arg0.hashCode();
		}
	}
	
	protected class RectangleValue
	{
		/**
		 * Coordinates of centre point of the rectangle, in absolute terms.
		 */
		protected  double[] centre;
		
		/**
		 * Width of rectangle, relative to boundDifference.
		 */
		protected  double[] width;
		
		/**
		 * Indication of potential improvement available on each dimension.
		 */
		protected  double[] potential;
		
		/**
		 * Length of longest side, count of long sides, and index of first long side.
		 */
		protected  double maxWidth;
		protected  int longCount;
		protected  int longIdx;

		/**
		 * @param centre - Coordinates of centre point of the rectangle, in absolute terms.
		 * @param width - Width of rectangle, relative to boundDifference.
		 */
		public RectangleValue(double[] centre, double[] width)
		{
			this.centre = centre;
			this.width = width;
			this.potential = new double[width.length];
			Arrays.fill(this.potential, 0.0);
			updateLongSides();
		}
		
		/**
		 * @param centre - Coordinates of centre point of the rectangle, in absolute terms.
		 * @param width - Width of rectangle, relative to boundDifference.
		 */
		public RectangleValue(double[] centre, double[] width, double[] potential)
		{
			this.centre = centre;
			this.width = width;
			this.potential = potential;
			updateLongSides();
		}
		
		public void updateLongSides()
		{
			int i;
			maxWidth = width[0];
			longIdx = 0;
			for (i = 1; i < width.length; ++i)
			{
				if (width[i] > maxWidth)
				{
					maxWidth = width[i];
					longIdx = i;
				}
			}
			longCount = 0;
			for (i = 0; i < width.length; ++i)
			{
				if (width[i] >= maxWidth * (1.0 - EQUAL_SIDE_TOL))
				{
					++longCount;
				}
			}
		}

		public double[] getCentre()
		{
			return centre;
		}

		public double[] getWidth()
		{
			return width;
		}

		public double[] getPotential()
		{
			return potential;
		}

		public int getLongCount()
		{
			return longCount;
		}

		public int getLongIdx()
		{
			return longIdx;
		}

		public boolean isLongSide(int i)
		{
			if (i == longIdx)
			{
				return true;
			}
			return width[i] >= maxWidth * (1.0 - EQUAL_SIDE_TOL);
		}

		public boolean isSmall()
		{
			int i;
			for (i = 0; i < width.length; ++i)
			{
				if (width[i] > convergenceThreshold)
				{
					return false;
				}
			}
			return true;
		}
	}
	
	protected class Rectangle implements
				Map.Entry<RectangleKey, RectangleValue>
	{
		protected RectangleKey key;
		protected RectangleValue value;

		public Rectangle()
		{
			this.key = null;
			this.value = null;
		}

		public Rectangle(Map.Entry<RectangleKey, RectangleValue> entry)
		{
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		@Override
		public RectangleKey getKey()
		{
			return key;
		}

		@Override
		public RectangleValue getValue()
		{
			return value;
		}

		@Override
		public RectangleValue setValue(RectangleValue value)
		{
			this.value = value;
			return value;
		}
	}

	protected TreeMap<RectangleKey, RectangleValue> rtree;
	protected Rectangle[] hull;	// array to store convex hull

	/** {@inheritDoc} */
	@Override
	protected PointValuePair doOptimize()
	{
		currentBest = new PointValuePair(getStartPoint(), Double.MAX_VALUE, true);

		// Validity checks.
		setup();
		
		double convergenceDiameter = thresholdDiameter(convergenceThreshold, boundDifference.length );

		do
		{
			incrementIterationCount();
		}
		while (dividePotentiallyOptimal(convergenceDiameter));

		if (getGoalType() == GoalType.MAXIMIZE)
		{
			return new PointValuePair(currentBest.getPoint(),
					-currentBest.getValue());
		}

		return currentBest;
	}
	
	public PointValuePair getCurrentBest()
	{
		if (getGoalType() == GoalType.MAXIMIZE)
		{
			return new PointValuePair(currentBest.getPoint(),
					-currentBest.getValue());
		}
		return currentBest;
	}

	// To maximize a function, minimize negative value of the function.
	/** {@inheritDoc} */
	@Override
	public double computeObjectiveValue(double[] params)
	{
		double fval;
		try
		{
			fval = super.computeObjectiveValue(params);
			if (getGoalType() == GoalType.MAXIMIZE)
			{
				fval = -fval;
			}
			if (fval < currentBest.getValue())
			{
				currentBest = new PointValuePair(params, fval, true);
			}
			if (fval > fMax)
			{
				fMax = fval;
			}
		}
		catch (NoSuchElementException e)
		{
			fval = fMax;
		}
		return fval;
	}

	/**
	 *  Evaluate the "diameter" (d) of a rectangle of widths w[n] 
	 *
	 *  We round the result to single precision, which should be plenty for
	 *  the use we put the diameter to (rect sorting), to allow our
	 *  performance hack in getPotentiallyOptimal to work (in the Jones and Gablonsky
	 *  DIRECT algorithms, all of the rects fall into a few diameter
	 *  values, and we don't want rounding error to spoil this). 
	 */
	protected double rectangleDiameter(double[] w)
	{
		int i;
		if (optDiam_longSide == 0)
		{
			/* Jones measure */
			/* distance from center to a vertex */
			double sum = 0.0;
			for (i = 0; i < w.length; ++i)
			{
				if (boundDifference[i] > 0)
				{
					sum += w[i] * w[i];
				}
			}
			return ((float) (FastMath.sqrt(sum) * 0.5));
		}
		else
		{
			/* Gablonsky measure */
			/* half-width of longest side */
			double wmax = 0.0;
			for (i = 0; i < w.length; ++i)
			{
				if (w[i] > wmax)
				{
					wmax = w[i];
				}
			}
			return ((float) (wmax * 0.5));
		}
	}
	
	/**
	 * Return the threshold diameter required for convergence, for a given
	 * relative convergence threshold.
	 */
	protected double thresholdDiameter(double convergenceThreshold, int dimension)
	{
		if (optDiam_longSide == 1)
		{
			// Diameter is half of longest side.
			return 0.5 * convergenceThreshold;
		}
		// Round the threshold down to the next smaller power of 1/3.
		// Rectangle is small when *all* sides are this size.
		double iterations = FastMath.ceil(FastMath.log(THIRD,convergenceThreshold));
		double threshold = FastMath.pow(THIRD, iterations);
		return 0.5 * FastMath.sqrt(dimension) * threshold;
	}

	/**
	 * Performs validity checks, and creates initial rectangle.
	 */
	protected void setup()
	{
		double[] init = getStartPoint();
		final int dimension = init.length;

		// Check problem dimension.
		if (dimension < 1)
		{
			throw new NumberIsTooSmallException(dimension, 1, true);
		}

		// Initialize bound differences.
		boundDifference = new double[dimension];
		double[] centre = new double[dimension];
		double[] width  = new double[dimension];

		for (int i = 0; i < dimension; i++)
		{
			boundDifference[i] = getUpperBound()[i] - getLowerBound()[i];
			centre[i] = 0.5 * (getUpperBound()[i] + getLowerBound()[i]);
			if (boundDifference[i] > 0)
			{
				width[i] = 1.0;		// Full scale
			}
			else
			{
				width[i] = 0.0;		// No variation
			}
		}
		
		nextSerial = 0;
		rtree = new TreeMap<RectangleKey, RectangleValue>();
		fv = new double[2 * dimension];
		isort = new Integer[dimension];
		int hullSize = (int) FastMath.sqrt(getMaxEvaluations());
		if (hullSize < 150)
		{
			hullSize = 150;
		}
		hull = new Rectangle[hullSize];

		fMax = 1.0;
		RectangleValue firstRect = new RectangleValue(centre, width);
		RectangleKey   firstKey = new RectangleKey(
				rectangleDiameter(width), computeObjectiveValue(centre));
		// We assume that the first point is feasible, and does not throw
		// an exception, otherwise the function value will be fMax.
		fMax = firstKey.getfValue();

		rtree.put(firstKey, firstRect);
		divideRectangle(firstKey, firstRect);
	}

	/**
	 * Class to hold the description of which sides of a rectangle
	 * should be divided.  The choice of sides is made in selectEligibleSides(),
	 * which supplies the outcome using this class.
	 */
	protected class EligibleSides
	{
		protected int nrEligibleSides;
		protected int eligibleSide;
		protected boolean[] isEligibleSide;

		public EligibleSides(int nrDimensions)
		{
			nrEligibleSides = 0;
			eligibleSide = 0;
			isEligibleSide = new boolean[nrDimensions];
			Arrays.fill(isEligibleSide, false);
		}

		public void setNrEligibleSides(int nrEligibleSides)
		{
			this.nrEligibleSides = nrEligibleSides;
			if (nrEligibleSides == 1)
			{
				Arrays.fill(isEligibleSide, false);
				isEligibleSide[eligibleSide] = true;
			}
		}

		public int getNrEligibleSides()
		{
			return nrEligibleSides;
		}

		public void setEligibleSide(int eligibleSide)
		{
			this.eligibleSide = eligibleSide;
			if (nrEligibleSides == 1)
			{
				Arrays.fill(isEligibleSide, false);
				isEligibleSide[eligibleSide] = true;
			}
		}

		public int getEligibleSide()
		{
			return eligibleSide;
		}
		
		public void setEligible(int i, boolean isEligible)
		{
			isEligibleSide[i] = isEligible;
		}

		public boolean isEligible(int i)
		{
			if (i < 0 || i >= isEligibleSide.length)
			{
				return false;
			}
			return isEligibleSide[i];
		}
	}
	
	/**
	 * For a specified rectangle, choose which sides to use for dividing the rectangle.
	 */
	protected EligibleSides selectEligibleSides(RectangleValue rectangle)
	{
		EligibleSides eligibleSides = new EligibleSides(rectangle.getWidth().length);
		boolean isHypercube = true;		// if all long sides have non-zero width.
		int nrEligibleSides = rectangle.getLongCount();
		int eligibleSide = rectangle.getLongIdx();
		int i;
		double highestPotential, totalPotential;
		// Default is to divide on all longest sides.
		for (i = 0; i < rectangle.getWidth().length; ++i)
		{
			eligibleSides.setEligible(i, rectangle.isLongSide(i));
			if (! rectangle.isLongSide(i) && boundDifference[i] > 0.0)
			{
				isHypercube = false;
			}
		}
		if (optDivide_oneSide == 1 && ! isHypercube)
		{
			// Divide on only one side.
			nrEligibleSides = 1;
		}
		else if (optDivide_oneSide == 2 && ! isHypercube)
		{
			// Divide on only the long side with the most potential.
			highestPotential = -Double.MAX_VALUE;
			nrEligibleSides = 1;
			for (i = 0; i < rectangle.getWidth().length; ++i)
			{
				if (rectangle.isLongSide(i) 
						&& rectangle.getPotential()[i] > highestPotential)
				{
					highestPotential = rectangle.getPotential()[i];
					eligibleSide = i;
				}
			}
		}
		else if (optDivide_oneSide == 2 && nrEligibleSides >= 4)
		{
			// Divide on long sides with above average potential.
			totalPotential = 0.0;
			nrEligibleSides = 0;
			for (i = 0; i < rectangle.getWidth().length; ++i)
			{
				if (rectangle.isLongSide(i))
				{
					totalPotential += rectangle.getPotential()[i];
				}
			}
			for (i = 0; i < rectangle.getWidth().length; ++i)
			{
				if (rectangle.isLongSide(i)
						&& rectangle.getPotential()[i] * rectangle.getLongCount()
						>= totalPotential)
				{
					eligibleSides.setEligible(i, true);
					eligibleSide = i;
					++nrEligibleSides;
				}
				else
				{
					eligibleSides.setEligible(i, false);
				}
			}
		}
		eligibleSides.setNrEligibleSides(nrEligibleSides);
		eligibleSides.setEligibleSide(eligibleSide);
		return eligibleSides;
	}

	protected class RectangleDivisionComparator implements Comparator<Integer>
	{
		@Override
		public int compare(Integer o1, Integer o2)
		{
			double fv1 = FastMath.min(fv[2 * o1], fv[2 * o1 + 1]);
			double fv2 = FastMath.min(fv[2 * o2], fv[2 * o2 + 1]);
			if (fv1 > fv2)
			{
				return 1;
			}
			if (fv1 < fv2)
			{
				return -1;
			}
			return 0;
		}
	}

	/**
	 * Divide a specified rectangle, already in rtree, into thirds,
	 * and update rtree accordingly.  Divide either on all the
	 * long sides, or only on one longest side,
	 * depending on optDivide_oneSide.
	 * @return Number of new function points that suggest there may
	 * be a better minimum within the original rectangle.
	 */
	protected int divideRectangle(RectangleKey rectKey,
			RectangleValue rectangle)
	{
		int i;
		int n = rectangle.getWidth().length;
		double[] c = rectangle.getCentre();
		double[] w = rectangle.getWidth();
		double csave;
		double centreF = rectKey.getfValue();	// f at old centre.
		double newF;			// f at new points.
		int nrPromising = 0;	// Number of new rectangles that may contain minimum.
		RectangleKey newKey;
		RectangleValue newRect;
		double[] new_c, new_w;

		EligibleSides eligibleSides = selectEligibleSides(rectangle);

		if (eligibleSides.getNrEligibleSides() > 1)
		{
			/* trisect all longest sides, in increasing order of the minimum
		       function value along that direction */
			for (i = 0; i < n; ++i)
			{
				isort[i] = i;
				if (eligibleSides.isEligible(i))
				{
					csave = c[i];
					c[i] = csave - w[i] * THIRD * boundDifference[i];
					newF = fv[2 * i] = computeObjectiveValue(c);
					if (isPromising(centreF, newF, n))
					{
						++nrPromising;
					}
					c[i] = csave + w[i] * THIRD * boundDifference[i];
					newF = fv[2 * i + 1] = computeObjectiveValue(c);
					if (isPromising(centreF, newF, n))
					{
						++nrPromising;
					}
					c[i] = csave;
				}
				else
				{
					fv[2 * i] = fv[2 * i + 1] = Double.MAX_VALUE;
				}
			}
			Arrays.sort(isort, new RectangleDivisionComparator());
			for (i = 0; i < eligibleSides.getNrEligibleSides(); ++i) {
				// Replace centre rectangle with smaller rectangle.
				w[isort[i]] *= THIRD;
				rtree.remove(rectKey);
				rectangle.updateLongSides();
				rectKey = new RectangleKey(rectangleDiameter(w),
						rectKey.getfValue());
				rtree.put(rectKey, rectangle);

				// Insert new rectangles for side divisions.
				new_c = Arrays.copyOf(c, c.length);
				new_w = Arrays.copyOf(w, w.length);
				new_c[isort[i]] = c[isort[i]] - w[isort[i]] * boundDifference[isort[i]];
				newKey = new RectangleKey(rectKey.getDiameter(),
						fv[2 * isort[i]]);
				newRect = new RectangleValue(new_c, new_w,
						Arrays.copyOf(rectangle.getPotential(), n));
				calculatePotential(newRect, isort[i], fv[2 * isort[i]], centreF, w[isort[i]]);
				rtree.put(newKey, newRect);
				new_c = Arrays.copyOf(c, c.length);
				new_w = Arrays.copyOf(w, w.length);
				new_c[isort[i]] = c[isort[i]] + w[isort[i]] * boundDifference[isort[i]];
				newKey = new RectangleKey(rectKey.getDiameter(),
						fv[2 * isort[i] + 1]);
				newRect = new RectangleValue(new_c, new_w,
						Arrays.copyOf(rectangle.getPotential(), n));
				calculatePotential(newRect, isort[i], fv[2 * isort[i] + 1], centreF, w[isort[i]]);
				rtree.put(newKey, newRect);
				calculatePotential(rectangle, i, centreF,
						FastMath.min(fv[2 * isort[i]], fv[2 * isort[i] + 1]), w[i]);
			}
		}
		else
		{
			// Replace centre rectangle with smaller rectangle.
			i = eligibleSides.getEligibleSide();
			w[i] *= THIRD;
			newKey = new RectangleKey(rectangleDiameter(w), rectKey.getfValue());
			rtree.remove(rectKey);
			rectangle.updateLongSides();
			rtree.put(newKey, rectangle);

			// Insert new rectangles for side divisions.
			new_c = Arrays.copyOf(c, c.length);
			new_w = Arrays.copyOf(w, w.length);
			new_c[i] = c[i] - w[i] * boundDifference[i];
			fv[0] = computeObjectiveValue(new_c);
			newKey = new RectangleKey(newKey.getDiameter(), fv[0]);
			newRect = new RectangleValue(new_c, new_w,
					Arrays.copyOf(rectangle.getPotential(), n));
			calculatePotential(newRect, i, fv[0], centreF, w[i]);
			rtree.put(newKey, newRect);
			if (isPromising(centreF, fv[0], n))
			{
				++nrPromising;
			}

			new_c = Arrays.copyOf(c, c.length);
			new_w = Arrays.copyOf(w, w.length);
			new_c[i] = c[i] + w[i] * boundDifference[i];
			fv[1] = computeObjectiveValue(new_c);
			newKey = new RectangleKey(newKey.getDiameter(), fv[1]);
			newRect = new RectangleValue(new_c, new_w,
					Arrays.copyOf(rectangle.getPotential(), n));
			calculatePotential(newRect, i, fv[1], centreF, w[i]);
			rtree.put(newKey, newRect);
			if (isPromising(centreF, fv[1], n))
			{
				++nrPromising;
			}
			calculatePotential(rectangle, i, centreF, FastMath.min(fv[0], fv[1]), w[i]);
		}
		return nrPromising;
	}
	
	/**
	 * Return true if a new function point indicates the possibility that there
	 * is a better minimum than the current best.
	 * @param centreF - function value at original centre point
	 * @param newF - function value at new point, 1/3 of width from centre
	 * @param dimension - problem dimension
	 */
	protected boolean isPromising(double centreF, double newF, int dimension)
	{
		// Extrapolate line from original centre through new point to
		// near edge of original rectangle, or within new central rectangle.
		// Return true if it leads to lower value than current best.
		// Increasing or decreasing factors (1.5 and 0.1) will make search
		// more thorough or less.
		if (newF < centreF && centreF - 1.5 * (centreF - newF) < currentBest.getValue())
		{
			return true;
		}
		if (newF > centreF && centreF - 0.1 * (newF - centreF) < currentBest.getValue())
		{
			return true;
		}
		return false;
	}

	protected void calculatePotential(RectangleValue rectangle, int dimension,
			double thisF, double neighbourF, double baseline)
	{
		rectangle.getPotential()[dimension] = (neighbourF - thisF);
	}

	/**
	 * Search for rectangles that might contain better global minimizers,
	 * and divide them.
	 * @return true if x threshold has not yet been reached;
	 *				there is more work to be done.
	 */
	protected boolean dividePotentiallyOptimal(double convergenceDiameter)
	{
		int i;
		int nhull;
		int nrSmall = 0;	// Number of POH too small to be worth dividing.
		int nrPromisingDivisions = 0;
		int ip;

		nhull = getPotentiallyOptimal(optHull_onePoint != 1);
		
		for (i = 0; i < nhull; ++i)
		{
			if (hull[i].getKey().getDiameter() < convergenceDiameter
					&& hull[i].getValue().isSmall())
			{
				// Rectangle already smaller than required accuracy.
				// Not worth dividing.
				++nrSmall;
			}
			else
			{
				/* "potentially optimal" rectangle, so subdivide */
				nrPromisingDivisions += divideRectangle(hull[i].getKey(), hull[i].getValue());

				/* for the DIRECT-L variant, we only divide one rectangle out
				   of all points with equal diameter and function values
				     ... note that for optHull_onePoint != 1, i == ip-1 should be a no-op
				         anyway, since we set allow_dups=0 in getPotentiallyOptimal above */
				if (optHull_onePoint == 1)
				{
					/* find next unequal points after i */
					for (ip = i+1; ip < nhull 
							&& hull[ip].getKey().getDiameter() == hull[i].getKey().getDiameter();
							++ip)
					{
					}
					i = ip - 1; /* skip to next unequal point for next iteration */
				}
			}
		}
		
		if (DISPLAY_PROGRESS)
		{
			System.out.println("DIRECT: " + rtree.size() + " rectangles, " + nhull + " POH ("
					+ nrSmall + " small, " + nrPromisingDivisions + " promising)."
					+ " Current best " + currentBest.getValue());
		}

		return nrSmall < 1 || nrPromisingDivisions > 0;
	}

	/* Convex hull algorithm, used to find the potentially optimal
	   points.  What we really have in DIRECT is a "dynamic convex hull"
	   problem, since we are dynamically adding/removing points and
	   updating the hull, but I haven't implemented any of the fancy
	   algorithms for this problem yet. */

	/**
	 * Find the lower convex hull of a set of points (x,y) stored in a rb-tree
	 * of pointers to {x,y} arrays sorted in ascending order by (x,y).
	 *
	 * Unlike standard convex hulls, we allow redundant points on the hull,
	 * and even allow duplicate points if allow_dups is nonzero.
	 * Also, we require the first segment of the hull to have a positive slope,
	 * the first point on the hull is that with the minimum y value so far,
	 * at the largest x value of such points.
	 *
	 * @return the number of points in the hull, with pointers
	 * stored in hull[i] (should be an array of length >= t->N).
	 */
	protected int getPotentiallyOptimal(boolean allow_dups)
	{
		int nhull = 0;
		double minslope;
		double xmax, ymaxmin;
		Entry<RectangleKey, RectangleValue> n, nmax;
		RectangleKey newKey;

		/* Monotone chain algorithm [Andrew, 1979]. */

		n = rtree.firstEntry();
		nmax = rtree.lastEntry();
		xmax = nmax.getKey().getDiameter();

		// Set nmax to first node with x == xmax
		//	while (nmax.getKey().getDiameter() == xmax)
		//	{
		//		max = rtree.lowerEntry(nmax.getKey());
		//	}
		//	nmax = rtree.higherEntry(nmax.getKey());
		// performance hack (see also below)
		RectangleKey testKey = new RectangleKey(xmax
				* (1 - DIAMETER_GRANULARITY));
		nmax = rtree.higherEntry(testKey);
		assert nmax.getKey().getDiameter() == xmax;

		ymaxmin = nmax.getKey().getfValue();

		double xlast = 0;						// Diameter of last entry in hull.
		double ylast = currentBest.getValue();	// f value of last entry in hull.
		minslope = (ymaxmin - ylast) / (xmax - xlast);

		RectangleKey k;
		for (; ! n.getKey().equals(nmax.getKey());
				n = rtree.higherEntry(n.getKey()))
		{
			k = n.getKey();

			/* performance hack: most of the points in DIRECT lie along
			   vertical lines at a few x values, and we can exploit this */
			if (nhull > 0 && k.getDiameter() == xlast)
			{
				/* x == previous x.  Skip all points with higher y. */
				if (k.getfValue() > ylast)
				{
					/* because of the round to float in rect_diameter, above,
					   it shouldn't be possible for two diameters (x values)
					   to have a fractional difference < 1e-13.  Note
					   that k.diameter > 0 always in DIRECT */
					newKey = new RectangleKey(k.getDiameter()
							* (1 + DIAMETER_GRANULARITY));
					n = rtree.floorEntry(newKey);
					continue;
				}
				else
				{
					/* equal y values, add to hull */
					if (allow_dups)
					{
						hull[nhull++] = new Rectangle(n);
					}
					continue;
				}
			}

			if (nhull > 0 && k.getfValue() > ylast + (k.getDiameter() - xlast) * minslope)
			{
				// This point is above the line from last point to nmax.
				continue;
			}

			/* Remove points until we are making a "left turn" to k */
			RectangleKey t1, t2;
			int it2;
			while (nhull >= 1)
			{
				t1 = hull[nhull - 1].getKey();

				/* because we allow equal points in our hull, we have
				   to modify the standard convex-hull algorithm slightly:
				   we need to look backwards in the hull list until we
				   find a point t2 != t1, instead of just t1 - 1. */
				it2 = getPrunePoint(hull, nhull, t1);
				if (it2 < 0)
				{
					if (t1.getfValue() < k.getfValue())
					{
						// Adding a first segment with positive slope.
						// No more pruning needed.
						break;
					}
				}
				else
				{
					t2 = hull[it2].getKey();
					/* cross product (t1-t2) x (k-t2) > 0 for a left turn: */
					if ((t1.getDiameter() - t2.getDiameter())
							* (k.getfValue() - t2.getfValue())
							- (t1.getfValue() - t2.getfValue())
							* (k.getDiameter() - t2.getDiameter()) >= 0)
					{
						// Adding a line segment steeper than prior segment.
						break;
					}
				}
				nhull = it2 + 1;
			}
			hull[nhull++] = new Rectangle(n);
			xlast = n.getKey().getDiameter();
			ylast = n.getKey().getfValue();
			minslope = (ymaxmin - ylast) / (xmax - xlast);
		}

		if (allow_dups)
		{
			do
			{
				/* include any duplicate points at (xmax,ymaxmin) */
				hull[nhull++] = new Rectangle(nmax);
				nmax = rtree.higherEntry(nmax.getKey());
			}
			while (nmax != null && nmax.getKey().getDiameter() == xmax
					&& nmax.getKey().getfValue() == ymaxmin);
		}
		else
		{
			hull[nhull++] = new Rectangle(nmax);
		}

		return nhull;
	}
	
	protected int getPrunePoint(Rectangle[] hull, int nhull, RectangleKey t1)
	{
		int it2 = nhull - 2;
		RectangleKey t2;
		while (it2 >= 0)
		{
			t2 = hull[it2].getKey();
			if (t2.getDiameter() != t1.getDiameter()
					|| t2.getfValue() != t1.getfValue())
			{
				return it2;
			}
			--it2;
		}
		return -1;
	}
}
