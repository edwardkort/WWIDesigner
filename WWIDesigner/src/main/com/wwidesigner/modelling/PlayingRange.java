/**
 * 
 */
package com.wwidesigner.modelling;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.note.Fingering;

/**
 * Representation of a complex spectrum, along with information about its
 * extreme points.
 */
public class PlayingRange
{
	/* Find playing ranges within a ratio of RangeWithin
	 * of a specified frequency. */
	protected static final double RangeWithin = 1.25;	// Within a major third.
	/* Granularity for bracket search.  Used to estimate derivatives.
	 * Basic step size for search is 10 times the granularity. */
	protected static final double Granularity = 1.003;	// About 5 cents.
	
	// The instrument being modeled.
	protected Instrument instrument;
	protected InstrumentCalculator calculator;

	// Classes used to find solutions.

	/* Function for finding roots of Im(Z). */
	protected class ZImag implements UnivariateFunction
	{
		public double value(double f)
		{
			Complex z = calculator.calcZ(f);
			return z.getImaginary();
		}
	}

	protected UnivariateFunction zImag;
	protected UnivariateSolver solver;
	
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

	public PlayingRange(Instrument instrument, 
			InstrumentCalculator calculator, Fingering fingering)
	{
		this.instrument = instrument;
		this.calculator = calculator;
		this.calculator.setFingering(fingering);
		this.zImag = new ZImag();
		this.solver = new BrentSolver();
	}

	/**
	 * Find a bracket for fmax in a playing range near a specified frequency.
	 * Post: nearFreq/RangeWithin <= lowerFreq < upperFreq <= nearFreq*RangeWithin, 
	 * 	     and there is a playing range with fmax between lowerFreq and upperFreq.
	 *       nearFreq is not necessarily between the two bounds.
	 * @param nearFreq - The target frequency for the bracket.
	 * @returns array { lowerFreq, upperFreq }
	 * @throws NoPlayingRange if no playing range is found to satisfy the post-condition.
	 */
	public double[] findBracket(double nearFreq)
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

		// Upper bound on fmax has Im(Z) > 0 and d/df Im(Z) > 0.
		// If Im(Z) > 0 and d/df Im(Z) < 0, search lower for upper bound.
		// If Im(Z) < 0 and d/df Im(Z) < 0, (an awkward spot) search lower for upper bound.
		// If Im(Z) < 0 and d/df Im(Z) > 0, search higher for upper bound.
		if ( z2.getImaginary() <= z1.getImaginary() )
		{
			// d/df Im(Z) <= 0.  Search lower.
			// Invariant z2 = Z(upperFreq), z1 = Z(upperFreq-delta).
			while ( z2.getImaginary() <= z1.getImaginary() 
					|| z2.getImaginary() <= 0.0 )
			{
				upperFreq -= stepSize;
				if ( upperFreq < nearFreq/RangeWithin )
				{
					throw new NoPlayingRange(nearFreq);
				}
				z2 = calculator.calcZ(upperFreq);
				z1 = calculator.calcZ(upperFreq-delta);
			}
			lowerFreq = upperFreq;
			// For lowerFreq, Im(Z) > 0 and d/df Im(Z) > 0.
		}
		else {
			// Search higher, if necessary.
			while ( z2.getImaginary() <= 0.0 )
			{
				upperFreq += stepSize;
				if ( upperFreq > nearFreq*RangeWithin )
				{
					throw new NoPlayingRange(nearFreq);
				}
				z2 = calculator.calcZ(upperFreq);
			}
			lowerFreq = nearFreq;
			// For lowerFreq, d/df Im(Z) > 0.
		}

		// Lower bound on fmax has Im(Z) < 0 and d/df Im(Z) > 0.
		// At this point, we know that d/df Im(Z) > 0.
		// Search lower for lower bound, if necessary.
		z2 = calculator.calcZ(lowerFreq);
		while ( z2.getImaginary() >= 0.0 )
		{
			lowerFreq -= stepSize;
			if ( lowerFreq < nearFreq/RangeWithin )
			{
				throw new NoPlayingRange(nearFreq);
			}
			z2 = calculator.calcZ(lowerFreq);
		}
		double[] bracket = {lowerFreq, upperFreq};
		return bracket;
	}

	/**
	 * Find fmax for the playing range nearest to nearFreq.
	 * @param nearFreq
	 */
	public double findFmax(double nearFreq)
	{
		double rootFreq;		// Frequency at which Z.imag == 0.
		double[] bracket = findBracket(nearFreq);

		try {
			rootFreq = solver.solve( 50, zImag, bracket[0], bracket[1] );
		}
		catch (Exception e)
		{
			System.out.println("Exception solving for fmax: " + e.getMessage());
			rootFreq = 0.0;
		}
		return rootFreq;
	}
}
