/**
 * Class for finding the playing frequencies of an instrument.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
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
 */
package com.wwidesigner.modelling;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;

import com.wwidesigner.note.Fingering;

/**
 * Class for finding playing frequencies of an instrument.
 * Depending on the calculator, some of the following conditions will
 * determine the playing frequency:
 * 
 *      fnom satisfies Im(Z(fnom)) = 0.0
 * or   fnom satisfies Im(Z(fnom)) = x0, for some x0.
 * 
 *      fmin < fmax
 *      fmax satisfies Im(Z(fnom)) = 0.0 and d/df Im(Z(fnom)) > 0.0
 *                     and Gain(fmax) > MinimumGain
 *      fmin satisfies Gain(fmin) = MinimumGain
 *                  or Gain(fmin) > MinimumGain
 *                     and fmin is a local minimum of Im(Z)/Re(Z).
 */
public class PlayingRange
{
	/* Find playing ranges within a ratio of DefaultRangeWithin
	 * of a specified frequency. */
	protected static final double DefaultRangeWithin = 1.4;	// Within half an octave.
	/* Basic step size for bracket search, as a fraction of f.
	 * Big assumption: within the range of interest, function(f).value
	 * has no more than one root between f and f+stepSize.
	 * A larger step size will find a bracket faster, but increase the risk
	 * that this assumption is violated. */
	protected static final double Granularity = 0.012;	// About 20 cents.
	/* Loop gain that defines fmin for a playing range. */
	protected static final double MinimumGain = 1.0;
	
	// A calculator for the instrument being modeled.
	protected InstrumentCalculator calculator;

	// Classes used to find solutions.
	
	/**
	 * Extension of UnivariateFunction that includes a function
	 * to provide a value at a specified impedance.
	 */
	protected interface UnivariateZFunction extends UnivariateFunction
	{
		double value(Complex z);
	}

	/**
	 * UnivariateFunction class for finding frequencies
	 * at which reactance is zero, or other specified value.
	 * Satisfies value = 0 when X = x0, and d/df value = d/df X.
	 */
	protected class Reactance implements UnivariateZFunction
	{
		double targetX;

		/**
		 * Create a function for finding zeros of reactance.
		 */
		public Reactance()
		{
			targetX = 0.0;
		}
		
		/**
		 * Create a function for finding frequencies at which reactance
		 * has a specified value.
		 * @param targetReactance
		 */
		public Reactance(double targetReactance)
		{
			targetX = targetReactance;
		}

		public double value(double f)
		{
			Complex z = calculator.calcZ(f);
			return z.getImaginary() - targetX;
		}

		public double value(Complex z)
		{
			return z.getImaginary() - targetX;
		}
	}

	/**
	 * UnivariateFunction class for finding frequencies
	 * at which loop gain is MinimumGain, or other specified value.
	 */
	protected class Gain implements UnivariateFunction
	{
		double targetGain;

		/**
		 * Create a function for finding frequencies
		 * where gain(f) = MinimumGain.
		 */
		public Gain()
		{
			this.targetGain = MinimumGain;
		}
		
		/**
		 * Create a function for finding frequencies
		 * where gain(f) has a specified value.
		 */
		public Gain(double targetGain)
		{
			this.targetGain = targetGain;
		}

		public double value(double f)
		{
			return calculator.calcGain(f) - targetGain;
		}
	}

	/**
	 *  UnivariateFunction class for finding local minima of Im(Z)/Re(Z),
	 *  or a specified value of Im(Z)/Re(Z), the tangent of the phase angle.
	 */
	protected class ZRatio implements UnivariateZFunction
	{
		double targetRatio;

		/**
		 * Create a function for finding zeros or minima of the Z ratio.
		 */
		public ZRatio()
		{
			targetRatio = 0.0;
		}
		
		/**
		 * Create a function for finding frequencies at which the Z ratio
		 * has a specified value.
		 * @param target - target value of Im(Z)/Re(Z).
		 */
		public ZRatio(double target)
		{
			targetRatio = target;
		}

		public double value(Complex z)
		{
			return z.getImaginary()/z.getReal() - targetRatio;
		}

		public double value(double f)
		{
			Complex z = calculator.calcZ(f);
			return z.getImaginary()/z.getReal() - targetRatio;
		}
	}

	protected Reactance reactance;
	protected Gain gainOne;
	protected ZRatio zRatio;
	protected UnivariateSolver solver;
	protected UnivariateOptimizer optimizer;
	
	public class NoPlayingRange extends RuntimeException
	{
		private static final long serialVersionUID = 8397354277027817459L;
		private final double freq;
		public NoPlayingRange(double freq)
		{
			this.freq = freq;
		}
		@Override
		public String getMessage()
		{
			return "No playing range near " + freq;
		}
	}

	/**
	 * Construct a playing-range calculator for a specified fingering.
	 * @param calculator
	 * @param fingering
	 */
	public PlayingRange(InstrumentCalculator calculator, Fingering fingering)
	{
		this.calculator = calculator;
		this.calculator.setFingering(fingering);
		this.reactance = new Reactance();
		this.gainOne = new Gain();
		this.zRatio = new ZRatio();
		this.solver = new BrentSolver();
		this.optimizer = new BrentOptimizer(0.0001, 0.0001);	// Approximate minimum is sufficient.
	}

	/**
	 * Construct a playing-range calculator for the current instrument fingering.
	 * @param calculator
	 */
	public PlayingRange(InstrumentCalculator calculator)
	{
		this.calculator = calculator;
		this.reactance = new Reactance();
		this.gainOne = new Gain();
		this.zRatio = new ZRatio();
		this.solver = new BrentSolver();
		this.optimizer = new BrentOptimizer(0.0001, 0.0001);	// Approximate minimum is sufficient.
	}

	/**
	 * Find a bracket near a specified frequency for a specified impedance-valued function.
	 * The target frequency may be fnom or fmax, depending on the calculator.
	 * Post: nearFreq/DefaultRangeWithin <= lowerFreq < upperFreq <= nearFreq*DefaultRangeWithin.
	 *       function(lowerFreq).value < 0.
	 *       function(upperFreq).value > 0.
	 *       There is a single root of function(freq).value
	 * 	     between lowerFreq and upperFreq.
	 *       Hence, the slope of function(freq).value is positive
	 *       at that root.
	 *       nearFreq is not necessarily between the two bounds.
	 * @param nearFreq - The target frequency for the bracket.
	 * @param function - A function with a zero at the target impedance.
	 * @returns array { lowerFreq, upperFreq }
	 * @throws NoPlayingRange if no bracket is found to satisfy the post-condition.
	 */
	public double[] findBracket(double nearFreq, UnivariateZFunction function)
			throws NoPlayingRange
	{
		final double stepSize = nearFreq * Granularity;		// Step size for search.
		double lowerFreq;
		double upperFreq; 
		Complex zLower, zUpper;
		lowerFreq = nearFreq - 0.5*stepSize;
		upperFreq = nearFreq + 0.5*stepSize;
		zLower = calculator.calcZ(lowerFreq);
		zUpper = calculator.calcZ(upperFreq);

		// If value(lowerFreq) < 0, then we have a lower bound,
		// and need only look higher for an upper bound.
		// If value(upperFreq) > 0, but the slope is negative,
		// we could look lower for a lower bound, but it is
		// possible that a closer root is actually higher.
		// Ideally, we would try both, and find the closer one,
		// but for now, we assume that the closer root is higher.
		
		while (function.value(zLower) >= 0.0
				&& function.value(zUpper) <= function.value(zLower) )
		{
			// Search higher.
			lowerFreq = upperFreq;
			zLower = zUpper;
			upperFreq += stepSize;
			if ( upperFreq > nearFreq*DefaultRangeWithin )
			{
				throw new NoPlayingRange(nearFreq);
			}
			zUpper = calculator.calcZ(upperFreq);
		}
		// In searching upward, we will most likely hit function.value(lowerFreq) < 0.0
		// before we hit a positive slope, so we do not need to worry
		// about lowerFreq re-tracing its steps, which would be inefficient.

		// At this point, function.value(lowerFreq) < 0.0,
		// or function.value(upperFreq) > function.value(lowerFreq) >= 0.0.

		// Search lower for lower bound, if necessary.
		while ( function.value(zLower) >= 0.0 )
		{
			lowerFreq -= stepSize;
			if ( lowerFreq < nearFreq/DefaultRangeWithin )
			{
				throw new NoPlayingRange(nearFreq);
			}
			zLower = calculator.calcZ(lowerFreq);
		}

		// Search higher for upper bound, if necessary.
		while ( function.value(zUpper) <= 0.0 )
		{
			upperFreq += stepSize;
			if ( upperFreq > nearFreq*DefaultRangeWithin )
			{
				throw new NoPlayingRange(nearFreq);
			}
			zUpper = calculator.calcZ(upperFreq);
		}

		double[] bracket = {lowerFreq, upperFreq};
		return bracket;
	}

	/**
	 * Find the zero of reactance nearest to nearFreq
	 * satisfying nearFreq/DefaultRangeWithin <= f <= nearFreq*DefaultRangeWithin
	 * @param nearFreq
	 * @throws NoPlayingRange if there is no zero of X
	 * within the specified range of nearFreq.
	 */
	public double findXZero(double nearFreq) throws NoPlayingRange
	{
		double rootFreq;		// Frequency at which Z.imag == 0.
		double[] bracket = findBracket(nearFreq, reactance);

		try {
			rootFreq = solver.solve( 50, reactance, bracket[0], bracket[1] );
		}
		catch (Exception e)
		{
			System.out.println("Exception in findXZero: " + e.getMessage());
			// e.printStackTrace();
			throw new NoPlayingRange(nearFreq);
		}
		return rootFreq;
	}

	/**
	 * Find the frequency with a specified reactance nearest to nearFreq
	 * satisfying nearFreq/DefaultRangeWithin <= f <= nearFreq*DefaultRangeWithin
	 * @param nearFreq
	 * @param targetX
	 * @throws NoPlayingRange if there is no zero of X
	 * within the specified range of nearFreq.
	 */
	public double findX(double nearFreq, double targetX) throws NoPlayingRange
	{
		double rootFreq;		// Frequency at which Z.imag == targetX.
		Reactance reactance = new Reactance( targetX );
		double[] bracket = findBracket(nearFreq, reactance);

		try {
			rootFreq = solver.solve( 50, reactance, bracket[0], bracket[1] );
		}
		catch (Exception e)
		{
			System.out.println("Exception in findX: " + e.getMessage());
			// e.printStackTrace();
			throw new NoPlayingRange(nearFreq);
		}
		return rootFreq;
	}

	/**
	 * Find fmin for a playing range, given fmax.
	 * fmin is the highest frequency <= fmax that satisfies
	 * either gain(fmin) == MinimumGain
	 * or fmin is a local minimum of Im(Z)/Re(Z).
	 * @param fmax - maximum frequency, as returned by findFmax().
	 */
	public double findFmin(double fmax)
	{
		final double stepSize = fmax * Granularity;		// Step size for search.

		// Upper bound on fmin is fmax.
		// findFmax ensures Im(Z(fmax)) == 0.0.
		double lowerFreq = fmax;
		Complex z_lo = calculator.calcZ(fmax);
		double g_lo = calculator.calcGain(lowerFreq,z_lo);
		double ratio = z_lo.getImaginary()/z_lo.getReal();
		double minRatio = ratio + 1.0;
		if ( g_lo < MinimumGain )
		{
			// Loop gain is too small, even at fmax.
			// There is no playing range here.
			throw new NoPlayingRange(fmax);
		}
		
		// Lower bound on fmin either has gain < MinimumGain
		// or is past a local minimum of Im(Z)/Re(Z).
		while ( g_lo >= MinimumGain && ratio < minRatio )
		{
			minRatio = ratio;
			lowerFreq -= stepSize;
			if ( lowerFreq < fmax/DefaultRangeWithin )
			{
				throw new NoPlayingRange(fmax);
			}
			z_lo = calculator.calcZ(lowerFreq);
			g_lo = calculator.calcGain(lowerFreq,z_lo);
			ratio = z_lo.getImaginary()/z_lo.getReal();
		}
		
		double freqGain;		// Frequency at which gain == MinimumGain.
		double freqRatio;		// Frequency of local minimum of Im(Z)/Re(Z).

		if ( g_lo < MinimumGain )
		{
			// Find the point at which gain == MinimumGain.
			try {
				freqGain = solver.solve( 50, gainOne, lowerFreq, fmax );
			}
			catch (Exception e)
			{
				System.out.println("Exception solving for fmin (gain): " + e.getMessage());
				// e.printStackTrace();
				throw new NoPlayingRange(fmax);
			}
		}
		else {
			freqGain = lowerFreq;
		}
		// Find the local minimum of Im(Z)/Re(Z).
		try {
			UnivariatePointValuePair  minimum;
			minimum = optimizer.optimize(GoalType.MINIMIZE,
					new UnivariateObjectiveFunction(zRatio),
					new MaxEval(50), MaxIter.unlimited(),
					new SearchInterval(lowerFreq, fmax, 0.5*(lowerFreq+fmax)));
			freqRatio = minimum.getPoint();
		}
		catch (Exception e)
		{
			System.out.println("Exception solving for fmin (ratio): " + e.getMessage());
			// e.printStackTrace();
			throw new NoPlayingRange(fmax);
		}
		if ( freqRatio > freqGain ) {
			return freqRatio;
		}
		return freqGain;
	}
	
	/**
	 * Find the frequency with a specified reactance nearest to nearFreq
	 * satisfying nearFreq/DefaultRangeWithin <= f <= nearFreq*DefaultRangeWithin
	 * @param nearFreq
	 * @param targetX
	 * @throws NoPlayingRange if there is no zero of X
	 * within the specified range of nearFreq.
	 */
	public double findZRatio(double nearFreq, double targetRatio) throws NoPlayingRange
	{
		double rootFreq;		// Frequency at which Z.imag == targetX.
		ZRatio ratio = new ZRatio( targetRatio );
		double[] bracket = findBracket(nearFreq, ratio);

		try {
			rootFreq = solver.solve( 50, ratio, bracket[0], bracket[1] );
		}
		catch (Exception e)
		{
			System.out.println("Exception in findZRatio: " + e.getMessage());
			// e.printStackTrace();
			throw new NoPlayingRange(nearFreq);
		}
		return rootFreq;
	}
}
