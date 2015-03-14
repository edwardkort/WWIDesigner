/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.ReactanceEvaluator;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class HoleGroupSpacingOptimizerTest extends AbstractOptimizationTest
{
	protected int[][] holeGroups;

	@Override
	protected void setup() throws Exception
	{
		setInputInstrumentXML("com/wwidesigner/optimization/example/G7HoleNAF.xml");
		setInputTuningXML("com/wwidesigner/optimization/example/G7HoleNAFTuning.xml");
		setParams(new PhysicalParameters(22.22, TemperatureType.C));
		super.setup();
	}

	@Test
	public void testNoGroupingNAF()
	{
		try
		{
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.018, 0.018, 0.018, 0.018, 0.018, 0.0005, 0.018,
					0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025 };
			upperBound = new double[] { 0.5, 0.05,  0.05,  0.1,   0.05,  0.05,  0.003,  0.20,
					0.017, 0.017, 0.017, 0.017, 0.017, 0.010, 0.010 };
			holeGroups = new int[][] { { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
					{ 5 }, { 6 } };
			evaluator = new ReactanceEvaluator(calculator);
			objective = new HoleGroupObjectiveFunction(calculator, tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective.setOptimizerType(BaseObjectiveFunction.OptimizerType.BOBYQAOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("No hole groups, NAF Calculator");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect with NAF Calculator", 13.93,
					lastPoint.getBorePosition(), 0.1);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void test2GroupingNAF()
	{
		try
		{
			setCalculator(new NAFCalculator());
			setup();
			lowerBound = new double[] { 0.2, 0.018, 0.018, 0.018, 0.0005, 0.018,
					0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0012, 0.0012 };
			upperBound = new double[] { 0.7, 0.05,  0.05,  0.1,   0.003,  0.20,
					0.017, 0.017, 0.017, 0.017, 0.017, 0.010, 0.010 };
			holeGroups = new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6 } };
			evaluator = new ReactanceEvaluator(calculator);
			objective = new HoleGroupObjectiveFunction(calculator, tuning, evaluator, holeGroups);
			objective.setMaxEvaluations(20000);
			objective.setOptimizerType(BaseObjectiveFunction.OptimizerType.CMAESOptimizer);

			Instrument optimizedInstrument = doInstrumentOptimization("Two hole groups, NAF Calculator");

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			PositionInterface[] sortedPoints = Instrument.sortList(borePoints);
			PositionInterface lastPoint = sortedPoints[sortedPoints.length - 1];
			assertEquals("Bore length incorrect with NAF Calculator", 13.92,
					lastPoint.getBorePosition(), 0.4);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		HoleGroupSpacingOptimizerTest test = new HoleGroupSpacingOptimizerTest();
		test.testNoGroupingNAF();
		test.test2GroupingNAF();
	}
}
