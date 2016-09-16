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
 *   other solution (hyperrectangle) examined in the search space shows
 *   promise of providing a better solution.
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
 * include a rectangle containing fmin; if there is more than one
 * such rectangle, it will include the rectangle(s) with the largest
 * diameter.
 * 
 * Java implementation: Copyright (C) 2016, Edward Kort, Antoine Lefebvre, Burton Patkau.
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

	/* which measure of hyper-rectangle diameter to use:
	 *  0 = Jones, centre-to-vertex distance
	 *  1 = Gablonsky, half longest side
	 */
	protected int which_diam;

	/* which way to divide rects:
	 *  0: orig. Jones, divide all longest sides
	 *  1: Gablonsky, cubes divide all sides, otherwise divide first long side
	 */
	protected int which_div;

	/* which rectangles are considered "potentially optimal"
	 *  0: Jones, all points on convex hull, even equal points
	 *  1: Gablonsky, pick one of each equal point
	 */
	protected int which_opt;
 
	protected double convergenceThreshold;

	protected double[] fv;		// workspace of function values, of length >= 2*n
	protected Integer[] isort;	// workspace for index sort, length >= n
	protected PointValuePair currentBest;	// Best point found so far
	protected double maxK;		// largest slope found so far

	/** Differences between the upper and lower bounds. */
	private double[] boundDifference;

	public DIRECTOptimizer()
	{
		this(DEFAULT_X_THRESHOLD);
	}

	public DIRECTOptimizer(double convergenceThreshold)
	{
		super(null);
		this.convergenceThreshold = convergenceThreshold;
		which_diam = 1;		// Gablonsky diameter measure.
		which_div  = 0;		// Jones long side division.
		which_opt  = 1;		// Gablonsky hull selection: allow duplicate points.
	}

	/* Basic data structure:
	*
	* A hyper-rectangle has a Rectangle Key, with the value (f) of the
	* function at the center, the "size" measure (d) of the rectangle,
	* an "age" measure for tie-breaking purposes, and a RectangleValue with
	* the coordinates of the center (c), and the widths of the sides (w).
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
		 * @param diameter
		 * @param fValue
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
		protected  double[] centre;
		protected  double[] width;

		public RectangleValue(double[] centre, double[] width)
		{
			this.centre = centre;
			this.width = width;
		}

		public double[] getCentre()
		{
			return centre;
		}

		public double[] getWidth()
		{
			return width;
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
		maxK = 0;

		// Validity checks.
		setup();

		do
		{
			incrementIterationCount();
		}
		while (dividePoteniallyOptimal());

		if (getGoalType() == GoalType.MAXIMIZE)
		{
			return new PointValuePair(currentBest.getPoint(),
					- currentBest.getValue());
		}

		return currentBest;
	}
	
	// To maximize a function, minimize negative value of the function.
	@Override
	public double computeObjectiveValue(double[] params)
	{
		double fval = super.computeObjectiveValue(params);
		if (getGoalType() == GoalType.MAXIMIZE)
		{
			fval = -fval;
		}
		if (fval < currentBest.getValue())
		{
			currentBest = new PointValuePair(params, fval, true);
		}
		return fval;
	}

	/* Evaluate the "diameter" (d) of a rectangle of widths w[n] 

	   We round the result to single precision, which should be plenty for
	   the use we put the diameter to (rect sorting), to allow our
	   performance hack in convex_hull to work (in the Jones and Gablonsky
	   DIRECT algorithms, all of the rects fall into a few diameter
	   values, and we don't want rounding error to spoil this) */
	protected double rect_diameter(double[] w)
	{
		int i;
		if (which_diam == 0)
		{
			/* Jones measure */
			/* distance from center to a vertex */
			double sum = 0.0;
			for (i = 0; i < w.length; ++i)
			{
				if (boundDifference[i] > 0)
				{
					sum += w[i] * w[i]
							/ (boundDifference[i] * boundDifference[i]);
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
				if (boundDifference[i] > 0 && w[i] > wmax * boundDifference[i])
				{
					wmax = w[i] / boundDifference[i];
				}
			}
			return ((float) (wmax * 0.5));
		}
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

		for (int i = 0; i < dimension; i++)
		{
			boundDifference[i] = getUpperBound()[i] - getLowerBound()[i];
			centre[i] = 0.5 * (getUpperBound()[i] + getLowerBound()[i]);
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

		RectangleValue firstRect = new RectangleValue(centre,
				Arrays.copyOf(boundDifference, dimension));
		RectangleKey   firstKey = new RectangleKey(
				rect_diameter(boundDifference), computeObjectiveValue(centre));

		rtree.put(firstKey, firstRect);
		divideRectangle(firstKey, firstRect);
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
	 * long sides, or only on the longest side,
	 * depending on which_div.
	 */
	protected double divideRectangle(RectangleKey rectKey,
			RectangleValue rectangle)
	{
		int i;
		int n = rectangle.getWidth().length;
		double[] c = rectangle.getCentre();
		double[] w = rectangle.getWidth();
		double wmax = -1.0;		// Width of longest side.
		int imax = 0;			// Dimension index of longest side.
		int nlongest = 0;		// Number of sides (about) the same size as longest.
		double csave;
		double maxK = 0;		// Maximum improvement in K. 
		double newK;
		RectangleKey newKey;
		RectangleValue newRect;
		double[] new_c, new_w;

		// Find longest side.
		for (i = 0; i < n; ++i)
		{
			if (boundDifference[i] > 0 && w[i] > wmax * boundDifference[i])
			{
				imax = i;
				wmax = w[i] / boundDifference[i];
			}
		}
		// Count number of long sides.
		for (i = 0; i < n; ++i)
		{
			if (boundDifference[i] > 0
					&& wmax - w[i] / boundDifference[i] <= wmax * EQUAL_SIDE_TOL)
			{
				++nlongest;
			}
		}
		if (which_div == 1 || (which_div == 0 && nlongest == n))
		{
			/* trisect all longest sides, in increasing order of the average
		       function value along that direction */
			for (i = 0; i < n; ++i)
			{
				isort[i] = i;
				if (boundDifference[i] > 0
						&& wmax - w[i] / boundDifference[i] <= wmax * EQUAL_SIDE_TOL)
				{
					csave = c[i];
					c[i] = csave - w[i] * THIRD;
					fv[2 * i] = computeObjectiveValue(c);
					newK = FastMath.abs(3.0 * (fv[2 * i] - rectKey.getfValue())
							/ (w[i] / boundDifference[i]));
					if (newK > maxK)
					{
						maxK = newK;
					}
					c[i] = csave + w[i] * THIRD;
					fv[2 * i + 1] = computeObjectiveValue(c);
					newK = FastMath.abs(3.0
							* (fv[2 * i + 1] - rectKey.getfValue())
							/ (w[i] / boundDifference[i]));
					if (newK > maxK)
					{
						maxK = newK;
					}
					c[i] = csave;
				}
				else
				{
					fv[2 * i] = fv[2 * i + 1] = Double.MAX_VALUE;
				}
			}
			Arrays.sort(isort, new RectangleDivisionComparator());
			for (i = 0; i < nlongest; ++i) {
				w[isort[i]] *= THIRD;
				rtree.remove(rectKey);
				rectKey = new RectangleKey(rect_diameter(w),
						rectKey.getfValue());
				rtree.put(rectKey, rectangle);

				new_c = Arrays.copyOf(c, c.length);
				new_w = Arrays.copyOf(w, w.length);
				new_c[isort[i]] = c[isort[i]] - w[isort[i]];
				newKey = new RectangleKey(rectKey.getDiameter(),
						fv[2 * isort[i]]);
				newRect = new RectangleValue(new_c, new_w);
				rtree.put(newKey, newRect);
				new_c = Arrays.copyOf(c, c.length);
				new_w = Arrays.copyOf(w, w.length);
				new_c[isort[i]] = c[isort[i]] + w[isort[i]];
				newKey = new RectangleKey(rectKey.getDiameter(),
						fv[2 * isort[i] + 1]);
				newRect = new RectangleValue(new_c, new_w);
				rtree.put(newKey, newRect);
			}
		}
		else
		{
			i = imax; /* trisect longest side */
			w[i] *= THIRD;
			newKey = new RectangleKey(rect_diameter(w), rectKey.getfValue());
			rtree.remove(rectKey);
			rtree.put(newKey, rectangle);

			new_c = Arrays.copyOf(c, c.length);
			new_w = Arrays.copyOf(w, w.length);
			new_c[i] = c[i] - w[i];
			newKey = new RectangleKey(newKey.getDiameter(),
					computeObjectiveValue(new_c));
			newRect = new RectangleValue(new_c, new_w);
			rtree.put(newKey, newRect);
			newK = FastMath.abs((newKey.getfValue() - rectKey.getfValue())
					/ (w[i] / boundDifference[i]));
			if (newK > maxK)
			{
				maxK = newK;
			}

			new_c = Arrays.copyOf(c, c.length);
			new_w = Arrays.copyOf(w, w.length);
			new_c[i] = c[i] + w[i];
			newKey = new RectangleKey(newKey.getDiameter(),
					computeObjectiveValue(new_c));
			newRect = new RectangleValue(new_c, new_w);
			rtree.put(newKey, newRect);
			newK = FastMath.abs((newKey.getfValue() - rectKey.getfValue())
					/ (w[i] / boundDifference[i]));
			if (newK > maxK)
			{
				maxK = newK;
			}
		}
		return maxK;
	}

	/**
	 * Search for rectangles that might contain better global minimizers,
	 * and divide them.
	 * @return true if x threshold has not yet been reached;
	 *				there is more work to be done.
	 */
	protected boolean dividePoteniallyOptimal()
	{
		int i;
		int nhull;
		int nrSmall = 0;	// Number of POH too small to be worth dividing.
		double localK;		// K found by dividing rectangle.
		int nrPromisingDivisions = 0;
		int ip;

		nhull = getPotentiallyOptimal(which_opt != 1);

		for (i = 0; i < nhull; ++i)
		{
			if (small(hull[i].getValue().getWidth()))
			{
				// Rectangle already smaller than required accuracy.
				// Not worth dividing.
				++nrSmall;
			}
			else
			{
				/* "potentially optimal" rectangle, so subdivide */
				double requiredK = (hull[i].getKey().getfValue() - currentBest.getValue())
						/ hull[i].getKey().getDiameter() ;
				localK = divideRectangle(hull[i].getKey(), hull[i].getValue());
				if (localK > maxK)
				{
					maxK = localK;
				}
				if (localK > requiredK)
				{
					++nrPromisingDivisions;
				}

				/* for the DIRECT-L variant, we only divide one rectangle out
				   of all points with equal diameter and function values
				     ... note that for which_opt != 1, i == ip-1 should be a no-op
				         anyway, since we set allow_dups=0 in convex_hull above */
				if (which_opt == 1)
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
		return nrSmall == 0 || nrPromisingDivisions > 0;
	}

	/**
	 * Test whether a rectangle is smaller than the established termination thresholds.
	 * @param w - width of rectangle
	 * @return true iff each side of the rectangle is, relative to the distance between the bounds,
	 *				is smaller than the convergence threshold.
	 */
	protected boolean small(double[] w)
	{
		int i;
		for (i = 0; i < w.length; ++i)
		{
			if (boundDifference[i] > 0.0 
					&& w[i] > 0.5 * boundDifference[i] * convergenceThreshold)
			{
				return false;
			}
		}
		return true;
	}

	/* Convex hull algorithm, used to find the potentially optimal
	   points.  What we really have in DIRECT is a "dynamic convex hull"
	   problem, since we are dynamically adding/removing points and
	   updating the hull, but I haven't implemented any of the fancy
	   algorithms for this problem yet. */

	/* Find the lower convex hull of a set of points (x,y) stored in a rb-tree
	   of pointers to {x,y} arrays sorted in lexigraphic order by (x,y).

	   Unlike standard convex hulls, we allow redundant points on the hull,
	   and even allow duplicate points if allow_dups is nonzero.
	   Also, we require the first segment of the hull to have a positive slope,
	   the first point on the hull is that with the minimum y value so far,
	   at the largest x value of such points.

	   The return value is the number of points in the hull, with pointers
	   stored in hull[i] (should be an array of length >= t->N).
	 */
	protected int getPotentiallyOptimal(boolean allow_dups)
	{
		int nhull = 0;
		double minslope;
		double xmin, xmax, yminmin, ymaxmin;
		Entry<RectangleKey, RectangleValue> n, nmax;
		RectangleKey newKey;

		/* Monotone chain algorithm [Andrew, 1979]. */

		n = rtree.firstEntry();
		nmax = rtree.lastEntry();

		xmin = n.getKey().getDiameter();
		yminmin = n.getKey().getfValue();
		xmax = nmax.getKey().getDiameter();

		if (allow_dups)
		{
			/* include any duplicate points at (xmin,yminmin) */
			do
			{
				hull[nhull++] = new Rectangle(n);
				n = rtree.higherEntry(n.getKey());
			} while (n != null && n.getKey().getDiameter() == xmin
					&& n.getKey().getfValue() == yminmin);
		}
		else
		{
			// include just the point at (xmin,yminmin)
			hull[nhull++] = new Rectangle(n);
		}

		if (xmin == xmax)
		{
			return nhull;
		}

		// Set nmax to first node with x == xmax
		//	while (nmax.getKey().getDiameter() == xmax)
		//	{
		//		max = rtree.lowerEntry(nmax.getKey()); /* non-NULL since xmin != xmax */
		//	}
		//	nmax = rtree.higherEntry(nmax.getKey());
		// performance hack (see also below)
		RectangleKey testKey = new RectangleKey(xmax
				* (1 - DIAMETER_GRANULARITY));
		nmax = rtree.higherEntry(testKey);	// non-NULL since xmin != xmax
		assert nmax.getKey().getDiameter() == xmax;

		ymaxmin = nmax.getKey().getfValue();
		minslope = (ymaxmin - yminmin) / (xmax - xmin);

		// Set n to first node with x != xmin */

		// while (n.getKey().getDiameter() == xmin)
		//	n = rtree.higherEntry(n.getKey()); /* non-NULL since xmin != xmax */
		/* performance hack (see also below) */
		testKey = new RectangleKey(xmin * (1 + DIAMETER_GRANULARITY));
		n = rtree.higherEntry(testKey);	// non-NULL since xmin != xmax
		assert n.getKey().getDiameter() > xmin;

		RectangleKey k;
		for (; ! n.getKey().equals(nmax.getKey());
				n = rtree.higherEntry(n.getKey()))
		{ 
			k = n.getKey();
			if (k.getfValue() > yminmin + (k.getDiameter() - xmin) * minslope)
			{
				// This point is above the line from nmin to nmax.
				continue;
			}

			/* performance hack: most of the points in DIRECT lie along
			   vertical lines at a few x values, and we can exploit this */
			if (nhull > 0
					&& k.getDiameter()
						== hull[nhull - 1].getKey().getDiameter())
			{
				/* x == previous x.  Skip all points with higher y. */
				if (k.getfValue() > hull[nhull - 1].getKey().getfValue())
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

			/* Remove points until we are making a "left turn" to k */
			RectangleKey t1, t2;
			int it2;
			while (nhull >= 1)
			{
				t1 = hull[nhull - 1].getKey();

				/* because we allow equal points in our hull, we have
				   to modify the standard convex-hull algorithm slightly:
				   we need to look backwards in the hull list until we
				   find a point t2 != t1 */
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
