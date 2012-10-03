/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.GordonCalculator;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.Constants.TemperatureType;

/**
 * @author kort
 * 
 */
public class HoleGroupSpacingOptimizerTest extends AbstractOptimizationTest
{
	protected int[][] holeGroups;

	@Override
	protected void setupCustomOptimizer() throws Exception
	{
		((HoleGroupSpacingOptimizer) optimizer).setHoleGroups(holeGroups);
	}

	private void setup() throws Exception
	{
		setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
		setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
		setParams(new PhysicalParameters(22.22, TemperatureType.C));
		setCalculator(new GordonCalculator());
		setOptimizerClass(HoleGroupSpacingOptimizer.class);
		setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
	}

	@Test
	public void testNoGrouping()
	{
		try
		{
			setup();
			setLowerBound(new double[] { 0.2, 0.012, 0.012, 0.012, 0.012,
					0.012, 0.0005, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.05, 0.05 });
			setUpperBound(new double[] { 0.5, 0.05, 0.05, 0.1, 0.05, 0.05,
					0.003, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7, 0.4, 0.4 });
			setNumberOfInterpolationPoints(30);
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
					{ 5 }, { 6 } };

			Instrument optimizedInstrument = doInstrumentOptimization("No hole groups, Gordon Calculator");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect with Gordon Calculator", 13.93,
					lastPoint.getBorePosition(), 0.1);

			setCalculator(new NAFCalculator());
			optimizedInstrument = doInstrumentOptimization("No hole groups, NAF Calculator");

			// Test bore length
			borePoints = optimizedInstrument.getBorePoint();
			sortedPoints = Instrument.sortList(borePoints);
			lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect with NAF Calculator", 13.93,
					lastPoint.getBorePosition(), 0.1);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void test2Grouping()
	{
		try
		{
			setup();
			setLowerBound(new double[] { 0.2, 0.012, 0.012, 0.012, 0.0005,
					0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.05, 0.05 });
			setUpperBound(new double[] { 0.5, 0.05, 0.1, 0.05, 0.003, 0.2, 0.7,
					0.7, 0.7, 0.7, 0.7, 0.4, 0.4 });
			setNumberOfInterpolationPoints(28);
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };

			Instrument optimizedInstrument = doInstrumentOptimization("Two hole groups, Gordon Calculator");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect with Gordon Calculator", 13.92,
					lastPoint.getBorePosition(), 0.1);

			setCalculator(new NAFCalculator());
			optimizedInstrument = doInstrumentOptimization("Two hole groups, NAF Calculator");

			// Test bore length
			borePoints = optimizedInstrument.getBorePoint();
			sortedPoints = Instrument.sortList(borePoints);
			lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect with NAF Calculator", 13.92,
					lastPoint.getBorePosition(), 0.1);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		HoleGroupSpacingOptimizerTest test = new HoleGroupSpacingOptimizerTest();
		// test.testNoGrouping();
		test.test2Grouping();
	}
}
