/**
 * 
 */
package com.wwidesigner.modelling;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.univariate.BrentOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optimization.univariate.UnivariatePointValuePair;

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
	/* Find playing ranges within a ratio of RangeWithin
	 * of a specified frequency. */
	protected static final double DefaultRangeWithin = 1.25;	// Within a major third.
	/* Granularity for bracket search.  Used to estimate derivatives.
	 * Basic step size for search is 10 times the granularity. */
	protected static final double Granularity = 1.003;	// About 5 cents.
	/* Loop gain that defines fmin for a playing range. */
	protected static final double MinimumGain = 1.0;
	
	// A calculator for the instrument being modeled.
	protected InstrumentCalculator calculator;

	// Classes used to find solutions.

	/**
	 * UnivariateFunction class for finding frequencies
	 * at which reactance is zero, or other specified value.
	 * Satisfies value = 0 when X = x0, and d/df value = d/df X.
	 */
	protected class Reactance implements UnivariateFunction
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
		 * Create a function for frequencies at which reactance
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
	 *  UnivariateFunction class for finding local minima of Im(Z)/Re(Z).
	 */
	protected class ZRatio implements UnivariateFunction
	{
		public double value(double f)
		{
			Complex z = calculator.calcZ(f);
			return z.getImaginary()/z.getReal();
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
	 * Find a bracket for a specified reactance near a specified frequency.
	 * The target frequency may be fnom or fmax, depending on the calculator.
	 * Post: nearFreq/RangeWithin <= lowerFreq < upperFreq <= nearFreq*RangeWithin.
	 *       reactance(lowerFreq).value < 0.
	 *       reactance(upperFreq).value > 0.
	 *       So there a freq with reactance(freq).value = 0
	 * 	     between lowerFreq and upperFreq.
	 *       nearFreq is not necessarily between the two bounds.
	 * @param nearFreq - The target frequency for the bracket.
	 * @param reactance - A function with a zero at the target reactance.
	 * @returns array { lowerFreq, upperFreq }
	 * @throws NoPlayingRange if no playing range is found to satisfy the post-condition.
	 */
	public double[] findBracket(double nearFreq, Reactance reactance)
			throws NoPlayingRange
	{
		final double delta = nearFreq * (Granularity-1.0);	// Step size for derivatives.
		final double stepSize = 10.0 * delta;					// Step size for search.
		double lowerFreq;
		double upperFreq; 
		Complex z1, z2;
		upperFreq = nearFreq + delta;
		z1 = calculator.calcZ(nearFreq);
		z2 = calculator.calcZ(upperFreq);

		// Upper bound on freq has X > x0 and d/df X > 0.
		// If X > x0 and d/df X < 0, search lower for upper bound.
		// If X < x0 and d/df X < 0, (an awkward spot) search lower for upper bound.
		// If X < x0 and d/df X > 0, search higher for upper bound.
		if ( reactance.value(z2) <= reactance.value(z1) )
		{
			// d/df X <= 0.  Search lower.
			// Invariant z2 = Z(upperFreq), z1 = Z(upperFreq-delta).
			while ( reactance.value(z2) <= reactance.value(z1) 
					|| reactance.value(z2) <= 0.0 )
			{
				upperFreq -= stepSize;
				if ( upperFreq < nearFreq/DefaultRangeWithin )
				{
					throw new NoPlayingRange(nearFreq);
				}
				z2 = calculator.calcZ(upperFreq);
				z1 = calculator.calcZ(upperFreq-delta);
			}
			lowerFreq = upperFreq;
			// For lowerFreq, X > x0 and d/df X > 0.
		}
		else {
			// Search higher, if necessary.
			while ( reactance.value(z2) <= 0.0 )
			{
				upperFreq += stepSize;
				if ( upperFreq > nearFreq*DefaultRangeWithin )
				{
					throw new NoPlayingRange(nearFreq);
				}
				z2 = calculator.calcZ(upperFreq);
			}
			lowerFreq = nearFreq;
			// For lowerFreq, d/df X > 0.
		}

		// Lower bound on frequency has X < x0 and d/df X > 0.
		// At this point, we know that d/df X > 0.
		// Search lower for lower bound, if necessary.
		z2 = calculator.calcZ(lowerFreq);
		while ( reactance.value(z2) >= 0.0 )
		{
			lowerFreq -= stepSize;
			if ( lowerFreq < nearFreq/DefaultRangeWithin )
			{
				throw new NoPlayingRange(nearFreq);
			}
			z2 = calculator.calcZ(lowerFreq);
		}
		double[] bracket = {lowerFreq, upperFreq};
		return bracket;
	}

	/**
	 * Find the zero of reactance nearest to nearFreq
	 * satisfying nearFreq/RangeWithin <= f <= nearFreq*RangeWithin
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
	 * satisfying nearFreq/RangeWithin <= f <= nearFreq*RangeWithin
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
		final double delta = fmax * (Granularity-1.0);	// Step size for derivatives.
		final double stepSize = 10.0 * delta;				// Step size for search.

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
			UnivariatePointValuePair  minimum
				= optimizer.optimize( 50, zRatio, GoalType.MINIMIZE, lowerFreq, fmax );
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
}
