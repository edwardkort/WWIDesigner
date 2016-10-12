/**
 * Re-implementation of the DIRECT-L algorithm described in:
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

/**
 * Implementation of the DIRECT-L optimization algorithm described in:
 *
 *		J. M. Gablonsky and C. T. Kelley, "A locally-biased form
 *		of the DIRECT algorithm," J. Global Optimization 21 (1),
 *		p. 27-37 (2001).
 */
public class DIRECT_L_Optimizer extends DIRECTOptimizer
{
	public DIRECT_L_Optimizer()
	{
		this(DEFAULT_X_THRESHOLD);
	}

	public DIRECT_L_Optimizer(double convergenceThreshold)
	{
		super(convergenceThreshold);
		allowDuplicatesInHull = false;		// Gablonsky hull selection: no duplicate points.
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
	@Override
	protected double rectangleDiameter(double[] w)
	{
		int i;
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
	
	/**
	 * Return the threshold diameter required for convergence, for a given
	 * relative convergence threshold.
	 */
	@Override
	protected double thresholdDiameter(double convergenceThreshold, int dimension)
	{
		// Diameter is half of longest side.
		return 0.5 * convergenceThreshold;
	}

}
