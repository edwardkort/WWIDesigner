/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.optimization.BoreLengthAdjustmentInterface.BoreLengthAdjustmentType;
import com.wwidesigner.optimization.multistart.GridRangeProcessor;
import com.wwidesigner.util.BoreProfileOptimizationException;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Tests those optimizers that work on a fixed bore, except for length.
 * 
 * @author Edward Kort
 *
 */
public class NafFixedBoreOptimizationTest extends AbstractOptimizationTest
{

	/**
	 * Using a 1-hole, tapered bore flute with "Hole size and position"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. One failure scenario occurs when the length
	 * constraints include the penultimate bore point. This consistently fails
	 * when multi-start optimization is used (even with 1 start); I haven't seen
	 * it without multi-start.
	 * 
	 * The solution (for now) is to switch off multi-start optimization in the
	 * NAF study model for this optimizer. If this is not sufficient, a
	 * <code>BoreProfileOptimizationException</code> is thrown.
	 * 
	 * This first test is the fail scenario: length constraint includes
	 * penultimate bore point and multi-start optimization. The prior
	 * implementation, with
	 * <code>BoreLengthAdjustmentType.PRESERVE.TAPER</code>, would fail by
	 * moving the penultimate bore point. This implementation, with
	 * <code>BoreLengthAdjustmentType.PRESERVE.BORE</code>, fails with a
	 * <code>BoreProfileOptimizationException</code>.
	 */
	@Test
	public final void test1HoleHoleSizePositionOptimizationWithTooWideConstraintsMultistartFail()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.20) is less than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.20, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_BORE);

			// Run multi-start optimizations with 1 start.
			GridRangeProcessor rangeProcessor = new GridRangeProcessor(
					lowerBound, upperBound, new int[] { 0 }, 1);
			objective.setRangeProcessor(rangeProcessor);
			objective.setMaxEvaluations(1 * objective.getMaxEvaluations());
			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, constraints too broad, fail");

			// Test bore length: a reality check
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has been moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertNotEquals("Penultimate bore point has been moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			// Although a <code>BoreProfileOptimizationException</code> is not
			// expected in this instance, it is an acceptable outcome.
			if (!(e instanceof BoreProfileOptimizationException))
			{
				fail(e.getMessage());
			}
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Hole size and position"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. One failure scenario occurs when the length
	 * constraints include the penultimate bore point. This consistently fails
	 * when multi-start optimization is used (even with 1 start); I haven't seen
	 * it without multi-start.
	 * 
	 * The solution (for now) is to switch off multi-start optimization in the
	 * NAF study model for this optimizer. If this is not sufficient, a
	 * <code>BoreProfileOptimizationException</code> is thrown.
	 * 
	 * This second test is a pass scenario: shorten the length constraint so
	 * they don't include the penultimate bore point with multi-start
	 * optimization.
	 */
	@Test
	public final void test1HoleHoleSizePositionOptimizationWithNarrowConstraintsMultistartPass()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.26) is more than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.26, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_BORE);

			// Run multi-start optimizations with 1 start.
			GridRangeProcessor rangeProcessor = new GridRangeProcessor(
					lowerBound, upperBound, new int[] { 0 }, 1);
			objective.setRangeProcessor(rangeProcessor);
			objective.setMaxEvaluations(1 * objective.getMaxEvaluations());

			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, constraints adjusted");

			// Test bore length: a reality check
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has not moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertEquals("Penultimate bore point has been moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			fail(e.getMessage());
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Hole size and position"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. One failure scenario occurs when the length
	 * constraints include the penultimate bore point. This consistently fails
	 * when multi-start optimization is used (even with 1 start); I haven't seen
	 * it without multi-start.
	 * 
	 * The solution (for now) is to switch off multi-start optimization in the
	 * NAF study model for this optimizer. If this is not sufficient, a
	 * <code>BoreProfileOptimizationException</code> is thrown.
	 * 
	 * This third test is a pass scenario: length constraint includes
	 * penultimate bore point and single-start optimization. A
	 * <code>BoreProfileOptimizationException</code> is also acceptable.
	 */
	@Test
	public final void test1HoleHoleSizePositionOptimizationWithTooWideConstraintsSinglestartPass()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.20) is less than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.20, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_BORE);

			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, constraints too broad, single start, pass");

			// Test bore length: a reality check
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has not moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertEquals("Penultimate bore point has moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			// Although a <code>BoreProfileOptimizationException</code> is not
			// expected in this instance, it is an acceptable outcome.
			if (!(e instanceof BoreProfileOptimizationException))
			{
				fail(e.getMessage());
			}
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Hole size and position"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. Another failure scenario occurs when the optimized
	 * length is shorter than the penultimate bore point. The prior
	 * implementation pushed the penultimate point upward, which is unacceptable
	 * in a fixed-bore scenario.
	 * 
	 * This fail scenario may occur transiently during optimization (the length
	 * constraint is too wide, with multi-start). Or it may occur because the
	 * final bore length is less than the penultimate bore point (which can
	 * easily by accommodated by deleting to last bore point). Therefore, the
	 * solution (for now) is to throw a
	 * <code>BoreProfileOptimizationException</code>.
	 * 
	 * This fourth test is a fail scenario: optimized length is less than the
	 * penultimate bore point. A <code>BoreProfileOptimizationException</code>
	 * is thrown.
	 */
	@Test
	public final void test1HoleWithHoleSizePositionOptimizationTooLongBoreFail()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.20) is less than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.20, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			objective = new HoleFromTopObjectiveFunction(calculator, tuning,
					evaluator, BoreLengthAdjustmentType.PRESERVE_BORE);

			// Force bore-length change above penultimate bore point
			List<Fingering> fingerings = tuning.getFingering();
			Fingering fundamentalFingering = fingerings.get(0);
			Note fundamental = fundamentalFingering.getNote();
			fundamental.setFrequency(500d);

			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, bore too short, single start, fail");

			// Test bore length
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has not been moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertEquals("Penultimate bore point has been moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			// A <code>BoreProfileOptimizationException</code> is the acceptable
			// outcome.
			if (!(e instanceof BoreProfileOptimizationException))
			{
				fail(e.getMessage());
			}
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Grouped-hole position & size"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. One failure scenario occurs when the length
	 * constraints include the penultimate bore point. This consistently fails
	 * when multi-start optimization is used (even with 1 start); I haven't seen
	 * it without multi-start.
	 * 
	 * The solution (for now) is to switch off multi-start optimization in the
	 * NAF study model for this optimizer. If this is not sufficient, a
	 * <code>BoreProfileOptimizationException</code> is thrown.
	 * 
	 * This first test is the fail scenario: length constraint includes
	 * penultimate bore point and multi-start optimization. The prior
	 * implementation, with
	 * <code>BoreLengthAdjustmentType.PRESERVE.TAPER</code>, would fail by
	 * moving the penultimate bore point. This implementation, with
	 * <code>BoreLengthAdjustmentType.PRESERVE.BORE</code>, fails with a
	 * <code>BoreProfileOptimizationException</code>.
	 */
	@Test
	public final void test1HoleWithGroupedHoleSizePositionOptimizationFail()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			double[] lowerBnd = new double[] { 0.20, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			int[][] holeGroups = new int[][] { { 0 } };
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups,
					BoreLengthAdjustmentType.PRESERVE_BORE);

			// Run multi-start optimizations with 1 start.
			GridRangeProcessor rangeProcessor = new GridRangeProcessor(
					lowerBound, upperBound, new int[] { 0 }, 1);
			objective.setRangeProcessor(rangeProcessor);
			objective.setMaxEvaluations(1 * objective.getMaxEvaluations());

			Instrument optimizedInstrument = doInstrumentOptimization(
					"1-hole, grouping, constraints too broad, fail");

			// Test bore length: a reality check
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has been moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertNotEquals("Penultimate bore point has been moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			// Although a <code>BoreProfileOptimizationException</code> is not
			// expected in this instance, it is an acceptable outcome.
			if (!(e instanceof BoreProfileOptimizationException))
			{
				fail(e.getMessage());
			}
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Grouped-hole position & size"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. One failure scenario occurs when the length
	 * constraints include the penultimate bore point. This consistently fails
	 * when multi-start optimization is used (even with 1 start); I haven't seen
	 * it without multi-start.
	 * 
	 * The solution (for now) is to switch off multi-start optimization in the
	 * NAF study model for this optimizer. If this is not sufficient, a
	 * <code>BoreProfileOptimizationException</code> is thrown.
	 * 
	 * This second test is a pass scenario: shorten the length constraint so
	 * they don't include the penultimate bore point with multi-start
	 * optimization.
	 */
	@Test
	public final void test1HoleHoleWithGroupedSizePositionOptimizationWithNarrowConstraintsMultistartPass()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.26) is more than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.26, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			int[][] holeGroups = new int[][] { { 0 } };
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups,
					BoreLengthAdjustmentType.PRESERVE_BORE);

			// Run multi-start optimizations with 1 start.
			GridRangeProcessor rangeProcessor = new GridRangeProcessor(
					lowerBound, upperBound, new int[] { 0 }, 1);
			objective.setRangeProcessor(rangeProcessor);
			objective.setMaxEvaluations(1 * objective.getMaxEvaluations());

			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, grouped, constraints adjusted");

			// Test bore length: a reality check
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has not moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertEquals("Penultimate bore point has been moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			fail(e.getMessage());
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Grouped-hole position & size"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. One failure scenario occurs when the length
	 * constraints include the penultimate bore point. This consistently fails
	 * when multi-start optimization is used (even with 1 start); I haven't seen
	 * it without multi-start.
	 * 
	 * The solution (for now) is to switch off multi-start optimization in the
	 * NAF study model for this optimizer. If this is not sufficient, a
	 * <code>BoreProfileOptimizationException</code> is thrown.
	 * 
	 * This third test is a pass scenario: length constraint includes
	 * penultimate bore point and single-start optimization. A
	 * <code>BoreProfileOptimizationException</code> is also acceptable.
	 */
	@Test
	public final void test1HoleGroupedHoleSizePositionOptimizationWithTooWideConstraintsSinglestartPass()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.20) is less than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.20, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			int[][] holeGroups = new int[][] { { 0 } };
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups,
					BoreLengthAdjustmentType.PRESERVE_BORE);

			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, constraints too broad, single start, pass");

			// Test bore length: a reality check
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has not moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertEquals("Penultimate bore point has moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			// Although a <code>BoreProfileOptimizationException</code> is not
			// expected in this instance, it is an acceptable outcome.
			if (!(e instanceof BoreProfileOptimizationException))
			{
				fail(e.getMessage());
			}
		}
	}

	/**
	 * Using a 1-hole, tapered bore flute with "Grouped-hole position & size"
	 * optimization, except for the bore end (which will be optimized for
	 * position and interpolated/extrapolated size), all other bore points
	 * should be respected. Another failure scenario occurs when the optimized
	 * length is shorter than the penultimate bore point. The prior
	 * implementation pushed the penultimate point upward, which is unacceptable
	 * in a fixed-bore scenario.
	 * 
	 * This fail scenario may occur transiently during optimization (the length
	 * constraint is too wide, with multi-start). Or it may occur because the
	 * final bore length is less than the penultimate bore point (which can
	 * easily by accommodated by deleting to last bore point). Therefore, the
	 * solution (for now) is to throw a
	 * <code>BoreProfileOptimizationException</code>.
	 * 
	 * This fourth test is a fail scenario: optimized length is less than the
	 * penultimate bore point. A <code>BoreProfileOptimizationException</code>
	 * is thrown.
	 */
	@Test
	public final void test1HoleWithGroupedHoleSizePositionOptimizationTooLongBoreFail()
	{
		try
		{
			String instrumentStr = "com/wwidesigner/optimization/example/1HoleTaperedNAF1.xml";
			String tuningStr = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";
			// The lower bore-length bound (0.20) is less than the penultimate
			// bore point (0.254).
			double[] lowerBnd = new double[] { 0.20, 0.25, 0.0075 };
			double[] upperBnd = new double[] { 0.4, 1.0, 0.010 };
			double penultimateBorePointPos = setup(instrumentStr, tuningStr,
					lowerBnd, upperBnd);
			int[][] holeGroups = new int[][] { { 0 } };
			objective = new HoleGroupFromTopObjectiveFunction(calculator,
					tuning, evaluator, holeGroups,
					BoreLengthAdjustmentType.PRESERVE_BORE);

			// Force bore-length change above penultimate bore point
			List<Fingering> fingerings = tuning.getFingering();
			Fingering fundamentalFingering = fingerings.get(0);
			Note fundamental = fundamentalFingering.getNote();
			fundamental.setFrequency(500d);

			Instrument optimizedInstrument = doInstrumentOptimization(
					"One-hole, bore too short, single start, fail");

			// Test bore length
			PositionInterface lastPoint = optimizedInstrument
					.getLowestBorePoint();
			assertEquals("Bore length incorrect", 11.40,
					lastPoint.getBorePosition(), 0.1);

			// Test that penultimate point has not been moved
			PositionInterface newPenultimateBorePoint = optimizedInstrument
					.getNextLowestBorePoint();
			assertEquals("Penultimate bore point has been moved",
					penultimateBorePointPos,
					newPenultimateBorePoint.getBorePosition(), 0.001);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			// A <code>BoreProfileOptimizationException</code> is the acceptable
			// outcome.
			if (!(e instanceof BoreProfileOptimizationException))
			{
				fail(e.getMessage());
			}
		}
	}

	protected double setup(String instrumentXml, String tuningXml,
			double[] lowerBnd, double[] upperBnd) throws Exception
	{
		setInputInstrumentXML(instrumentXml);
		setInputTuningXML(tuningXml);
		setParams(new PhysicalParameters(22.22, TemperatureType.C));
		setCalculator(new NAFCalculator());
		setup();
		evaluator = new ReactanceEvaluator(calculator);
		setLowerBound(lowerBnd);
		setUpperBound(upperBnd);

		// Get the original penultimate bore point position.
		instrument.convertToLengthType();
		double penultimateBorePointPos = instrument.getNextLowestBorePoint()
				.getBorePosition();
		return penultimateBorePointPos;
	}
}