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
 *      fnom satisfies Im(Z(fnom)) = 0.0 and d/df Im(Z(fnom)) > 0.0
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
	/* Find playing ranges within a ratio of SearchBoundRatio
	 * of a specified frequency. */
	protected static final double SearchBoundRatio = 2.0;	// Within an octave.
	/* Acceptable solutions are within this ratio of specified frequency.
	 * Only search beyond this if necessary.
	 */
	protected static final double PreferredSolutionRatio = 1.12;		// Within 200 cents.

	/* Basic step size for bracket search, as a fraction of f.
	 * Big assumption: within the range of interest, function(f).value
	 * has no more than one root between f and f*(1+Granularity).
	 * A larger step size will find a bracket faster, but increase the risk
	 * that this assumption is violated. */
	protected static final double Granularity = 0.012;	// About 20 cents.
	/* Loop gain that defines fmin for a playing range. */
	protected static final double MinimumGain = 1.0;
	
	// A calculator for the instrument being modeled.
	protected InstrumentCalculator calculator;

	// Fingering for the current note being modeled.
	protected Fingering fingering;

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
			Complex z = calculator.calcZ(f, fingering);
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
		public Gain(double aTargetGain)
		{
			this.targetGain = aTargetGain;
		}

		public double value(double f)
		{
			return calculator.calcGain(f, fingering) - targetGain;
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
			Complex z = calculator.calcZ(f, fingering);
			return z.getImaginary()/z.getReal() - targetRatio;
		}
	}

	/**
	 *  UnivariateFunction class for finding local minima of |Z|,
	 *  or a specified value of |Z|, the impedance magnitude.
	 */
	protected class ZMagnitude implements UnivariateZFunction
	{
		double targetMagnitude;

		/**
		 * Create a function for finding zeros or minima of |Z|.
		 */
		public ZMagnitude()
		{
			targetMagnitude = 0.0;
		}
		
		/**
		 * Create a function for finding frequencies at which |Z|
		 * has a specified value.
		 * @param target - target value of |Z|.
		 */
		public ZMagnitude(double target)
		{
			targetMagnitude = target;
		}

		public double value(Complex z)
		{
			return z.abs() - targetMagnitude;
		}

		public double value(double f)
		{
			Complex z = calculator.calcZ(f, fingering);
			return z.abs() - targetMagnitude;
		}
	}

	protected Reactance reactance;
	protected Gain gainOne;
	protected ZRatio zRatio;
	protected ZMagnitude zMagnitude;
	protected UnivariateSolver solver;
	protected UnivariateOptimizer optimizer;
	
	public class NoPlayingRange extends RuntimeException
	{
		private static final long serialVersionUID = 8397354277027817459L;
		private final double freq;
		public NoPlayingRange(double aFreq)
		{
			this.freq = aFreq;
		}
		@Override
		public String getMessage()
		{
			return "No playing range near " + freq;
		}
	}

	/**
	 * Construct a playing-range calculator for a specified fingering.
	 * @param aCalculator
	 * @param aFingering
	 */
	public PlayingRange(InstrumentCalculator aCalculator, Fingering aFingering)
	{
		this.calculator = aCalculator;
		this.fingering = aFingering;
		this.reactance = new Reactance();
		this.zMagnitude = new ZMagnitude();
		this.gainOne = new Gain();
		this.zRatio = new ZRatio();
		this.solver = new BrentSolver();
		this.optimizer = new BrentOptimizer(0.0001, 0.0001);	// Approximate minimum is sufficient.
	}

	/**
	 * Find a bracket for a root of function.value(calcZ(f)) above a specified frequency.
	 * Pre:  zNear = calculator.calcZ(nearFreq)
	 * Post: Either returns {lowerFreq,upperFreq} that satisfy
	 *       function(lowerFreq) < 0 and function(upperFreq) > 0.
	 *       and nearFreq <= lowerFreq < upperFreq <= upperBound
	 *       or returns {-1,0} if no such bracket found.
	 * @param nearFreq - lower bound on the bracket
	 * @param zNear - impedance at nearFreq
	 * @param function - objective function.
	 * @param upperBound - upper bound on the bracket
	 * @returns array { lowerFreq, upperFreq }
	 */
	protected double[] findBracketAbove(double nearFreq, Complex zNear, UnivariateZFunction function,
			double upperBound)
	{
		final double stepSize = nearFreq * Granularity;		// Step size for search.
		double lowerFreq, upperFreq;
		Complex zLower, zUpper;
		lowerFreq = nearFreq;
		zLower = zNear;

		// First, ensure that function(lowerFreq) < 0.
		// This will usually be the case, but not always.

		while (function.value(zLower) >= 0.0)
		{
			lowerFreq += stepSize;
			if (lowerFreq >= upperBound)
			{
				double[] bracket = {-1.0,0.0};
				return bracket;
			}
			zLower = calculator.calcZ(lowerFreq, fingering);
		}

		// Search up until function(upperFreq) > 0.

		upperFreq = lowerFreq + stepSize;
		zUpper = calculator.calcZ(upperFreq, fingering);
		
		while (function.value(zUpper) <= 0.0)
		{
			if (function.value(zUpper) < 0.0)
			{
				// Move up the lower end of the bracket.
				lowerFreq = upperFreq;
				zLower = zUpper;
			}
			upperFreq += stepSize;
			if (upperFreq > upperBound)
			{
				double[] bracket = {-1.0,0.0};
				return bracket;
			}
			zUpper = calculator.calcZ(upperFreq, fingering);
		}

		double[] bracket = {lowerFreq, upperFreq};
		return bracket;
	} // findBracketAbove

	/**
	 * Find a bracket for a root of function.value(calcZ(f)) below a specified frequency.
	 * Pre:  zNear = calculator.calcZ(nearFreq)
	 * Post: Either returns {lowerFreq,upperFreq} that satisfy
	 *       function(lowerFreq) < 0 and function(upperFreq) > 0.
	 *       and lowerBound <= lowerFreq < upperFreq <= nearFreq
	 *       or returns {-1,0} if no such bracket found.
	 * @param nearFreq - upper bound on the bracket
	 * @param zNear - impedance at nearFreq
	 * @param function - objective function.
	 * @param lowerBound - lower bound on the bracket
	 * @returns array { lowerFreq, upperFreq }
	 */
	protected double[] findBracketBelow(double nearFreq, Complex zNear, UnivariateZFunction function,
			double lowerBound)
	{
		final double stepSize = nearFreq * Granularity;		// Step size for search.
		double lowerFreq, upperFreq;
		Complex zLower, zUpper;
		upperFreq = nearFreq;
		zUpper = zNear;

		// First, ensure that function(upperFreq) > 0.
		// This will usually be the case, but not always.

		while (function.value(zUpper) <= 0.0)
		{
			upperFreq -= stepSize;
			if (upperFreq <= lowerBound)
			{
				double[] bracket = {-1.0,0.0};
				return bracket;
			}
			zUpper = calculator.calcZ(upperFreq, fingering);
		}

		// Search down until function(lowerFreq) < 0.

		lowerFreq = upperFreq - stepSize;
		zLower = calculator.calcZ(lowerFreq, fingering);
		
		while (function.value(zLower) >= 0.0)
		{
			if (function.value(zLower) > 0.0)
			{
				// Move down the upper end of the bracket.
				upperFreq = lowerFreq;
				zUpper = zLower;
			}
			lowerFreq -= stepSize;
			if (lowerFreq < lowerBound)
			{
				double[] bracket = {-1.0,0.0};
				return bracket;
			}
			zLower = calculator.calcZ(lowerFreq, fingering);
		}

		double[] bracket = {lowerFreq, upperFreq};
		return bracket;
	} // findBracketBelow

	/**
	 * Find a bracket near a specified frequency for a specified impedance-valued function.
	 * The target frequency may be fnom or fmax, depending on the calculator.
	 * Post: nearFreq/SearchBoundRatio <= lowerFreq < upperFreq <= nearFreq*SearchBoundRatio.
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
		double freq = nearFreq;
		Complex zNear = calculator.calcZ(freq, fingering);
		double limitFreq;
		double [] upwardBracket;
		double [] downwardBracket;
		
		while (function.value(zNear) == 0.0)
		{
			// For the unlikely case that we landed right on a zero,
			// adjust the frequency slightly.
			// We don't know whether slope is positive or negative.
			freq = freq * 0.999;
			zNear = calculator.calcZ(freq, fingering);
		}
		
		if (function.value(zNear) < 0.0)
		{
			// If starting function value is negative, start searching upward.
			upwardBracket = findBracketAbove(freq, zNear, function,
					nearFreq*SearchBoundRatio);
			if (upwardBracket[0] <= 0.0
				|| upwardBracket[1] > nearFreq * PreferredSolutionRatio)
			{
				// If result isn't close enough, search downward as well.
				if (upwardBracket[0] <= 0.0)
				{
					limitFreq = nearFreq / SearchBoundRatio;
				}
				else
				{
					limitFreq = nearFreq * nearFreq/upwardBracket[1]; 
				}
				downwardBracket = findBracketBelow(freq, zNear, function,
						limitFreq);
				if (downwardBracket[0] > 0.0)
				{
					// We found a better solution searching downward.
					return downwardBracket;
				}
			}
			if (upwardBracket[0] <= 0.0)
			{
				// We didn't find a bracket searching upward.
				throw new NoPlayingRange(nearFreq);
			}
			return upwardBracket;
		}
		else
		{
			// If starting function value is positive, start searching downward.
			downwardBracket = findBracketBelow(freq, zNear, function, 
					nearFreq/SearchBoundRatio);
			if (downwardBracket[0] <= 0.0
				|| downwardBracket[0] < nearFreq / PreferredSolutionRatio)
			{
				// If result isn't close enough, search upward as well.
				if (downwardBracket[0] <= 0.0)
				{
					limitFreq = nearFreq * SearchBoundRatio;
				}
				else
				{
					limitFreq = nearFreq * nearFreq/downwardBracket[0]; 
				}
				upwardBracket = findBracketAbove(freq, zNear, function,
						limitFreq);
				if (upwardBracket[0] > 0.0)
				{
					// We found a better solution searching upward.
					return upwardBracket;
				}
			}
			if (downwardBracket[0] <= 0.0)
			{
				// We didn't find a bracket searching downward.
				throw new NoPlayingRange(nearFreq);
			}
			return downwardBracket;
		}
	} // findBracket

	/**
	 * Find the zero of reactance nearest to nearFreq
	 * satisfying nearFreq/SearchBoundRatio <= f <= nearFreq*SearchBoundRatio
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
			// For step tapers, this exception is hit with no other consequences.
			// Comment out the system message so as not to raise unneeded flags.
			// System.out.println("Exception in findXZero: " + e.getMessage());
			// e.printStackTrace();
			throw new NoPlayingRange(nearFreq);
		}
		return rootFreq;
	}

	/**
	 * Find the frequency with a specified reactance nearest to nearFreq
	 * satisfying nearFreq/SearchBoundRatio <= f <= nearFreq*SearchBoundRatio
	 * @param nearFreq
	 * @param targetX
	 * @throws NoPlayingRange if there is no zero of X
	 * within the specified range of nearFreq.
	 */
	public double findX(double nearFreq, double targetX) throws NoPlayingRange
	{
		double rootFreq;		// Frequency at which Z.imag == targetX.
		Reactance aReactance = new Reactance( targetX );
		double[] bracket = findBracket(nearFreq, aReactance);

		try {
			rootFreq = solver.solve( 50, aReactance, bracket[0], bracket[1] );
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
		Complex z_lo = calculator.calcZ(fmax, fingering);
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
			if ( lowerFreq < fmax/SearchBoundRatio )
			{
				throw new NoPlayingRange(fmax);
			}
			z_lo = calculator.calcZ(lowerFreq, fingering);
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
	 * satisfying nearFreq/SearchBoundRatio <= f <= nearFreq*SearchBoundRatio
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

	/**
	 * Find the minimum magnitude of Z nearest to nearFreq
	 * satisfying nearFreq/SearchBoundRatio <= f <= nearFreq*SearchBoundRatio.
	 * In principle, this should give the same results as findXZero.
	 * In practice, the results are slightly different, and optimization results
	 * change dramatically with changes in initial conditions, even with significantly
	 * tighter stopping criteria on the BrentOptimizer.  Not recommended.
	 * @param nearFreq
	 * @throws NoPlayingRange if there is no minimum of |Z|
	 * within the specified range of nearFreq.
	 */
	/*
	public double findZMinimum(double nearFreq) throws NoPlayingRange
	{
		double freqOfMin;		// Frequency at which |Z| is a minimum.
		double[] bracket = findBracket(nearFreq, reactance);

		try {
			UnivariatePointValuePair  minimum;
			minimum = optimizer.optimize(GoalType.MINIMIZE,
					new UnivariateObjectiveFunction(zMagnitude),
					new MaxEval(100), MaxIter.unlimited(),
					new SearchInterval(bracket[0], bracket[1],
							0.5*(bracket[0] + bracket[1])));
			freqOfMin = minimum.getPoint();
		}
		catch (Exception e)
		{
			System.out.println("Exception in findZMinimum: " + e.getMessage());
			// e.printStackTrace();
			throw new NoPlayingRange(nearFreq);
		}
		return freqOfMin;
	}
    */

	public Fingering getFingering()
	{
		return fingering;
	}

	public void setFingering(Fingering aFingering)
	{
		this.fingering = aFingering;
	}

}
