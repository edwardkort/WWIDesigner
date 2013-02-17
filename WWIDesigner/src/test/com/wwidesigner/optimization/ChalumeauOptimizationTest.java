/**
 * 
 */
package com.wwidesigner.optimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer;
import org.junit.Test;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.EvaluatorInterface;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.ReflectionEvaluator;
import com.wwidesigner.modelling.SimpleReedCalculator;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.SortedPositionList;

/**
 * @author kort
 * 
 */
public class ChalumeauOptimizationTest
{
	protected String inputInstrumentXML = "com/wwidesigner/optimization/example/chalumeau_alto.xml";
	protected String inputTuningXML = "com/wwidesigner/optimization/example/chalumeau_alto_tuning.xml";

	/**
	 * Complete workflow for optimizing an XML-defined instrument with a
	 * specified ObjectiveFunction.
	 * 
	 * @return An Instrument object after optimization, with all dimensions in
	 *         the original units.
	 * @throws Exception
	 */
	public Instrument doInstrumentOptimization() throws Exception
	{
		PhysicalParameters parameters = new PhysicalParameters(25.,
				TemperatureType.C);
		Instrument instrument = getInstrumentFromXml(inputInstrumentXML);
		InstrumentCalculator calculator = new SimpleReedCalculator(instrument,
				parameters);
		instrument.convertToMetres();

		Tuning tuning = getTuningFromXml(inputTuningXML);

		double lowerBound[] = new double[] { 0.32, 0.000, 0.00, 0.00, 0.015,
				0.015, 0.015, 0.02, 0.02, 0.02, 0.02, 0.004, 0.004, 0.005,
				0.004, 0.004, 0.004, 0.005, 0.005, 0.005, 0.005 };
		double upperBound[] = new double[] { 0.38, 0.001, 0.05, 0.05, 0.05,
				0.05, 0.05, 0.05, 0.05, 0.05, 0.100, 0.007, 0.007, 0.007,
				0.007, 0.007, 0.0075, 0.007, 0.007, 0.007, 0.007 };

		EvaluatorInterface evaluator = new ReflectionEvaluator(calculator);
		BaseObjectiveFunction objective = new HoleObjectiveFunction(calculator,
				tuning, evaluator);
		objective.setLowerBounds(lowerBound);
		objective.setUpperBounds(upperBound);

		// At present, the tuning is insufficiently constrained to uniquely
		// determine
		// both hole size and position. Slight changes in number of
		// interpolation points
		// can lead to drastic changes in the optimum found.
		BOBYQAOptimizer optimizer = new BOBYQAOptimizer(70);

		PointValuePair outcome = optimizer.optimize(20000, objective,
				GoalType.MINIMIZE, objective.getStartingPoint(), lowerBound,
				upperBound);
		objective.setGeometryPoint(outcome.getKey());

		// Convert back to the input unit-of-measure values
		instrument.convertToLengthType();

		// The optimizer modifies the input Instrument instance
		return instrument;
	}

	@Test
	public final void testInstrumentOptimization()
	{
		try
		{
			Instrument optimizedInstrument = doInstrumentOptimization();

			// Test bore length
			List<BorePoint> borePoints = optimizedInstrument.getBorePoint();
			SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(
					borePoints);
			BorePoint lastPoint = sortedPoints.getLast();

			// Test hole positions
			List<Hole> holes = optimizedInstrument.getHole();
			SortedPositionList<Hole> sortedHoles = new SortedPositionList<Hole>(
					holes);

			System.out.println("Hole position and diameter:");
			for (int i = 0; i < sortedHoles.size(); ++i)
			{
				System.out.print(sortedHoles.get(i).getBorePosition());
				System.out.print("  ");
				System.out.println(sortedHoles.get(i).getDiameter());
			}

			// System.out.print("last point = " + lastPoint.getBorePosition());
			assertEquals("Bore length incorrect", 337.5,
					lastPoint.getBorePosition(), 0.2);

			assertEquals("Hole 1 position incorrect", 111.2, sortedHoles.get(0)
					.getBorePosition(), 1.0);
			assertEquals("Hole 2 position incorrect", 112.3, sortedHoles.get(1)
					.getBorePosition(), 1.0);
			assertEquals("Hole 3 position incorrect", 129.6, sortedHoles.get(2)
					.getBorePosition(), 1.0);
			assertEquals("Hole 4 position incorrect", 135.0, sortedHoles.get(3)
					.getBorePosition(), 1.0);
			assertEquals("Hole 5 position incorrect", 159.0, sortedHoles.get(4)
					.getBorePosition(), 1.0);
			assertEquals("Hole 6 position incorrect", 184.7, sortedHoles.get(5)
					.getBorePosition(), 1.0);
			assertEquals("Hole 7 position incorrect", 205.1, sortedHoles.get(6)
					.getBorePosition(), 1.0);
			assertEquals("Hole 8 position incorrect", 225.7, sortedHoles.get(7)
					.getBorePosition(), 1.0);
			assertEquals("Hole 9 position incorrect", 249.4, sortedHoles.get(8)
					.getBorePosition(), 1.0);
			assertEquals("Hole 10 position incorrect", 277.2, sortedHoles
					.get(9).getBorePosition(), 1.0);

			assertEquals("Hole 1 diameter incorrect", 6.0, sortedHoles.get(0)
					.getDiameter(), 0.2);
			assertEquals("Hole 2 diameter incorrect", 5.4, sortedHoles.get(1)
					.getDiameter(), 0.2);
			assertEquals("Hole 3 diameter incorrect", 5.2, sortedHoles.get(2)
					.getDiameter(), 0.2);
			assertEquals("Hole 4 diameter incorrect", 5.3, sortedHoles.get(3)
					.getDiameter(), 0.2);
			assertEquals("Hole 5 diameter incorrect", 6.4, sortedHoles.get(4)
					.getDiameter(), 0.2);
			assertEquals("Hole 6 diameter incorrect", 7.1, sortedHoles.get(5)
					.getDiameter(), 0.2);
			assertEquals("Hole 7 diameter incorrect", 6.4, sortedHoles.get(6)
					.getDiameter(), 0.2);
			assertEquals("Hole 8 diameter incorrect", 6.4, sortedHoles.get(7)
					.getDiameter(), 0.2);
			assertEquals("Hole 9 diameter incorrect", 6.9, sortedHoles.get(8)
					.getDiameter(), 0.2);
			assertEquals("Hole 10 diameter incorrect", 6.8, sortedHoles.get(9)
					.getDiameter(), 0.2);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected Instrument getInstrumentFromXml(String instrumentXML)
			throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
	}

	protected Tuning getTuningFromXml(String tuningXML) throws Exception
	{
		BindFactory noteBindFactory = NoteBindFactory.getInstance();
		File inputFile = getInputFile(inputTuningXML, noteBindFactory);
		Tuning tuning = (Tuning) noteBindFactory.unmarshalXml(inputFile, true);

		return tuning;
	}

	/**
	 * This approach for get the input File is based on finding it in the
	 * classpath. The actual application will use an explicit file path - this
	 * approach will be unnecessary.
	 * 
	 * @param fileName
	 *            expressed as a package path.
	 * @param bindFactory
	 *            that manages the elements in the file.
	 * @return A file representation of the fileName, as found somewhere in the
	 *         classpath.
	 * @throws FileNotFoundException
	 */
	protected File getInputFile(String fileName, BindFactory bindFactory)
			throws FileNotFoundException
	{
		String inputPath = BindFactory.getPathFromName(fileName);
		File inputFile = new File(inputPath);

		return inputFile;
	}
}
