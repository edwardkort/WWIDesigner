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

import java.util.Map.Entry;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.util.FastMath;

/**
 * DIRECT-C (DIRECT Centred) variant of the DIRECT optimization algorithm.
 * Chooses sides to divide as in DIRECT-1. Uses an alternate strategy for
 * choosing potentially-optimal hyperrectangles: when standard convex hull of
 * DIRECT is not improving best function value after a few iterations, or after
 * several iterations even if function value is improving.
 * 
 * Two alternate strategies currently used, both centred around a promising
 * point: pick large hyperrectangles close to current best point (pruned to a
 * convex hull), or pick hyperrectangles with smallest function values close to
 * current best point (pruned to monotone, but not necessarily convex, hull).
 * For the latter, DIRECT-C uses a simple bin sort to sort hyperrectangles by
 * distance from the target point, and find the POH with lowest function value
 * in each bin.
 * 
 * This algorithm takes inspiration from the multi-level DIRECT-HD optimizer
 * described in:
 * 
 *		Arash Tavassoli, Kambiz Haji Hajikolaei, Soheil Sadeqi, G. Gary Wang,
 *		and Erik Kjeang, "Modification of DIRECT for high-dimensional design problems,"
 *		Engineering Optimization, 2013, Taylor and Francis,
 *		DOI:10.1080/0305215X.2013.800057
 *
 * Unlike DIRECT-HD, DIRECT-C does not shrink the constraint bounds, and so
 * it does not require a diversification routine. 
 */
public class DIRECTCOptimizer extends DIRECT1Optimizer
{
	protected static final int DEFAULT_ITERATION_INTERVAL = 3;
	protected static final int DEFAULT_DISTANCE_BINS = 50;

	/**
	 * Number of iterations without improvement, and overall, before trying
	 * alternate strategy for selecting POH.
	 */
	protected int iterationIntervalNoImprovement, iterationIntervalMax;
	
	/**
	 * Last interation at which a variant selection strategy was used.
	 */
	protected int iterationOfLastVariant;
	
	/**
	 * Order in which to use variant selection strategies.
	 */
	protected int[] typeOfVariant;
	
	/**
	 * Index into typeOfVariant.
	 */
	protected int nextVariantIndex;

	/**
	 * Distances from the current best point are divided into bins.
	 * This array gives the upper bound of the distances in each bin,
	 * relative to the maximum possible distance.  Values increase
	 * monotonically from zero to 1.
	 */
	protected double[] relativeDistance;

	/**
	 * Create an optimizer that uses the DIRECT-C variant of the DIRECT algorithm,
	 * with default convergence threshold on hyperrectangle sizes.
	 */
	public DIRECTCOptimizer()
	{
		this(DEFAULT_X_THRESHOLD, DEFAULT_ITERATION_INTERVAL);
	}

	/**
	 * Create an optimizer that uses the DIRECT-C variant of the DIRECT
	 * algorithm.
	 * 
	 * @param convergenceThreshold
	 *            - The optimizer converges when the best solution is in a
	 *            hyperrectangle with all sides smaller than this threshold,
	 *            relative to the distance between the upper and lower bounds.
	 */
	public DIRECTCOptimizer(double convergenceThreshold)
	{
		this(convergenceThreshold, DEFAULT_ITERATION_INTERVAL);
	}

	/**
	 * Create an optimizer that uses the DIRECT-C variant of the DIRECT
	 * algorithm.
	 * 
	 * @param convergenceThreshold
	 *            - The optimizer converges when the best solution is in a
	 *            hyperrectangle with all sides smaller than this threshold,
	 *            relative to the distance between the upper and lower bounds.
	 * @param iterationInterval
	 *            - execute the alternate algorithm for choosing POH every time
	 *            the number of iterations without an improvement in current
	 *            best point reaches this threshold.
	 */
	public DIRECTCOptimizer(double convergenceThreshold, int iterationInterval)
	{
		super(convergenceThreshold);
		this.iterationIntervalNoImprovement = iterationInterval;
		this.iterationIntervalMax = 4 * iterationInterval;
		this.iterationOfLastVariant = 0;
		this.nextVariantIndex = 0;
		this.typeOfVariant = new int[]{ 2, 1, 1, 2, 1 };
		this.relativeDistance = new double[DEFAULT_DISTANCE_BINS];
		for (int i = 0; i < relativeDistance.length; ++i)
		{
			relativeDistance[i] = FastMath.pow(1.7, i - relativeDistance.length);
		}
	}
	
	@Override
	protected PointValuePair doOptimize()
	{
		this.iterationOfLastVariant = 0;
		this.nextVariantIndex = 0;
		return super.doOptimize();
	}
	
	protected int selectVariant()
	{
		if (iterationOfLastVariant + iterationIntervalNoImprovement > getIterations())
		{
			// Too soon to use a variant.
			return 0;
		}
		if (iterationOfLastImprovement + iterationIntervalNoImprovement <= getIterations()
			|| iterationOfLastVariant + iterationIntervalMax <= getIterations())
		{
			// No improvement since last variant, or no variant for some time.
			iterationOfLastVariant = getIterations();
			if (nextVariantIndex >= typeOfVariant.length)
			{
				nextVariantIndex = 0;
			}
			return typeOfVariant[nextVariantIndex++];
		}
		return 0;
	}

	@Override
	protected int getPotentiallyOptimal(boolean allow_dups)
	{
		int variant = selectVariant();
		if (variant == 1)
		{
			if (DISPLAY_PROGRESS)
			{
				System.out.println("DIRECT: select large POH near current best");
			}
			return getPotentiallyOptimalLargeAndNear(currentBest.getPoint(), true);
		}
		if (variant == 2)
		{
			if (DISPLAY_PROGRESS)
			{
				System.out.println("DIRECT: select low value POH near current best");
			}
			return getPotentiallyOptimalNearByValue(currentBest.getPoint(), false);
		}
		return super.getPotentiallyOptimal(allow_dups);

	}

	/**
	 * Calculate Cartesian distance between two points, relative
	 * to distance between bounds in each dimension.
	 */
	protected double distance(double[] x1, double[] x2)
	{
		double sumSquares = 0.0;
		double side;
		for (int i = 0; i < x1.length; ++i)
		{
			if (boundDifference[i] > 0)
			{
				side = (x1[i] - x2[i]) / boundDifference[i];
				sumSquares += side * side;
			}
		}
		return FastMath.sqrt(sumSquares);
	}

	/**
	 * Return maximum possible distance from a specified point, relative
	 * to the distance between bounds in each dimension.
	 */
	protected double maxDistance(double[] x)
	{
		double sumSquares = 0.0;
		double lowerSide, upperSide;
		double side;
		for (int i = 0; i < x.length; ++i)
		{
			if (boundDifference[i] > 0)
			{
				lowerSide = x[i] - getLowerBound()[i];
				upperSide = getUpperBound()[i] - x[i];
				if (lowerSide >= upperSide)
				{
					side = lowerSide / boundDifference[i];
				}
				else
				{
					side = upperSide / boundDifference[i];
				}
				sumSquares += side * side;
			}
		}
		return FastMath.sqrt(sumSquares);
	}

	/**
	 * Find rectangles that are large, and close to a target point, and return
	 * them in <code>hull</code>.
	 * 
	 * @param target
	 *            - Point on which to centre search.
	 * @param useConvexHull
	 *            - If true, limit rectangles to the lower right convex hull on
	 *            the graph of rectangle distance as a function of size. If
	 *            false, returned rectangles have monotonically increasing
	 *            distance as a function of size, but are not limited to the
	 *            convex hull.
	 * @return Number of rectangles returned in <code>hull</code>.
	 */
	protected int getPotentiallyOptimalLargeAndNear(double[] target, boolean useConvexHull)
	{
		int nhull = 0;
		Entry<RectangleKey, RectangleValue> nearest = null;
		double nDist = 0.0;
		double y;

		for (Entry<RectangleKey, RectangleValue> n : rtree.entrySet())
		{
			if (n.getValue().isSmall())
			{
				// Ignore rectangles that are too small to divide further.
				// With no small rectangles returned, we know this will not be
				// the last iteration of optimization.
				continue;
			}
			if (nearest == null)
			{
				// First potential rectangle.
				nearest = n;
				nDist = distance(nearest.getValue().getCentre(), target);
				continue;
			}
			if (n.getKey().getDiameter() == nearest.getKey().getDiameter())
			{
				// Rectangle at same distance as prior rectangle.
				// Check whether it is nearer the current best.
				y = distance(n.getValue().getCentre(), target);
				if (y < nDist)
				{
					nearest = n;
					nDist = y;
				}
				continue;
			}
			else
			{
				// Checked all rectangles with same diameter as nearest.
				// Add nearest to hull, pruning to convex hull.
				nhull = pruneHullLargeAndNear(target, nearest.getKey(), nDist, nhull, useConvexHull);
				checkHullLength(nhull);
				hull[nhull++] = new Rectangle(nearest);
			
				// Start search for nearest rectangle at new diameter.
				nearest = n;
				nDist = distance(nearest.getValue().getCentre(), target);
			}
		}

		nhull = pruneHullLargeAndNear(target, nearest.getKey(), nDist, nhull, useConvexHull);
		checkHullLength(nhull);
		hull[nhull++] = new Rectangle(nearest);

		return nhull;
	}
	
	protected int pruneHullLargeAndNear(double[] target, RectangleKey nKey,
			double nDistance, int nhull, boolean useConvexHull)
	{
		// Remove points until we are making a "left turn" to nearest
		RectangleKey t1, t2;
		int it2;
		double t1Dist, t2Dist;
		int thisNhull = nhull;
		while (thisNhull >= 1)
		{
			t1 = hull[thisNhull - 1].getKey();
			t1Dist = distance(hull[thisNhull - 1].getValue().getCentre(), target);
			it2 = thisNhull - 2;
			if (t1Dist > nDistance)
			{
				// Not even monotone.  Keep pruning.
			}
			else if (it2 < 0)
			{
				// Adding a first segment with positive slope.
				// No more pruning needed.
				break;
			}
			else if (! useConvexHull)
			{
				// Monotone hull is sufficient.
				// No need to prune to convex hull.
				break;
			}
			else
			{
				t2 = hull[it2].getKey();
				t2Dist = distance(hull[it2].getValue().getCentre(), target);
				// cross product (t1-t2) x (nKey-t2) > 0 for a left turn: 
				if ((t1.getDiameter() - t2.getDiameter())
						* (nDistance - t2Dist)
						- (t1Dist - t2Dist)
						* (nKey.getDiameter() - t2.getDiameter()) >= 0)
				{
					// Adding a line segment steeper than prior segment.
					break;
				}
			}
			thisNhull = it2 + 1;
		}
		
		return thisNhull;
	}
	
	protected int getDistanceBin(double[] x, double[] target, double maxDistance)
	{
		double dist = distance(x, target) / maxDistance;
		for (int i = 0; i < relativeDistance.length; ++i)
		{
			if (dist <= relativeDistance[i])
			{
				return i;
			}
		}
		return relativeDistance.length - 1;
	}

	/**
	 * Find rectangles that are close to a target point and have low function
	 * values, and return them in <code>hull</code>.
	 * 
	 * @param target
	 *            - Point on which to centre search.
	 * @param useConvexHull
	 *            - If true, limit rectangles to the lower right convex hull on
	 *            the graph of function value as a function of distance from
	 *            target. If false, returned rectangles have monotonically
	 *            increasing function value as a function of distance, but are
	 *            not limited to the convex hull.
	 * @return Number of rectangles returned in <code>hull</code>.
	 */
	protected int getPotentiallyOptimalNearByValue(double[] target, boolean useConvexHull)
	{
		int nhull = 0;
		double maxDistance = maxDistance(target);
		Rectangle[] lowest = new Rectangle[DEFAULT_DISTANCE_BINS];
		int distBin;

		// Perform a bin sort, grouping the available rectangles into
		// distance bins, and finding the rectangle with lowest function value
		// that falls in each distance bin.
		for (Entry<RectangleKey, RectangleValue> n : rtree.entrySet())
		{
			if (! n.getValue().isSmall())
			{
				distBin = getDistanceBin(n.getValue().getCentre(), target, maxDistance);
				if (lowest[distBin] == null
						|| lowest[distBin].getKey() == null
						|| n.getKey().getfValue() < lowest[distBin].getKey().getfValue())
				{
					lowest[distBin] = new Rectangle(n);
				}
			}
		}
		
		// Transfer the list of lowest-function-value rectangles to hull,
		// pruning as needed to produce a monotone or convex hull.
		int it2;
		double nDist, t1Dist, t2Dist;		// x values of current and two previous points.
		double nValue, t1Value, t2Value;	// y values of current and two previous points.
		nhull = 0;
		for (int i = 0; i < lowest.length; ++i)
		{
			if (lowest[i] != null && lowest[i].getKey() != null)
			{
				// Remove points until we are making a "left turn" to nearest
				nDist = distance(lowest[i].getValue().getCentre(), target);
				nValue = lowest[i].getKey().getfValue();
				while (nhull >= 1)
				{
					t1Dist = distance(hull[nhull - 1].getValue().getCentre(), target);
					t1Value = hull[nhull - 1].getKey().getfValue();
					it2 = nhull - 2;
					if (t1Value > nValue)
					{
						// Not even monotone.  Keep pruning.
					}
					else if (it2 < 0)
					{
						// Adding a first segment with positive slope.
						// No more pruning needed.
						break;
					}
					else if (! useConvexHull)
					{
						// Monotone hull is sufficient.
						// No need to prune to convex hull.
						break;
					}
					else
					{
						t2Dist = distance(hull[it2].getValue().getCentre(), target);
						t2Value = hull[it2].getKey().getfValue();
						// cross product (t1-t2) x (nKey-t2) > 0 for a left turn: 
						if ((t1Dist - t2Dist) * (nValue - t2Value)
								- (t1Value - t2Value) * (nDist - t2Dist) >= 0)
						{
							// Adding a line segment steeper than prior segment.
							break;
						}
					}
					nhull = it2 + 1;
				}
				checkHullLength(nhull);
				hull[nhull++] = lowest[i];
			}
		}

		return nhull;
	}
	
}
