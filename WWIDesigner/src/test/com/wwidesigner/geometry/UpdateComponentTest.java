/**
 * 
 */
package com.wwidesigner.geometry;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.util.BindFactory;

/**
 * @author kort
 * 
 */
public class UpdateComponentTest
{
	protected String inputInstrumentXML;
	protected String inputTuningXML;

	// Set this to true to use SimpleHolePositionAndDiameterOptimizer and the
	// associated bounds.
	// Set this to false to use HolePositionAndDiameterOptimizer and the
	// associated bounds.
	protected static boolean useSimpleOptimizer = false;

	public final void testNoHoleOptimization()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/NoHoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/NoHoleNAF1Tuning.xml";

			Instrument instrument = getInstrumentFromXml();
			instrument.convertToMetres();

			instrument.updateComponents();
			printComponents("No Holes, first update", instrument.components);
			printTermination(instrument.getTermination());

			instrument.updateComponents();
			printComponents("\nNo Holes, second update", instrument.components);
			printTermination(instrument.getTermination());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	private static void printTermination(Termination termination)
	{
		System.out.println("Termination:");
		System.out.println("  Flange diameter: " + termination.getFlangeDiameter());
		System.out.println("  Bore diameter: " + termination.getBoreDiameter());
		System.out.println("  Position: " + termination.getBorePosition());
	}

	private static void printComponents(String title,
			List<ComponentInterface> components)
	{
		System.out.println(title);
		for (ComponentInterface component : components)
		{
			String name = component.getClass().getSimpleName();
			switch (name)
			{
				case "Mouthpiece":
					printMouthpiece(component);
					break;
				case "BoreSection":
					printBoreSection(component, "");
					break;
				case "Hole":
					printHole(component);
			}
		}

	}

	private static void printHole(ComponentInterface component)
	{
		Hole hole = (Hole)component;
		System.out.println("Hole:");
		System.out.println("  Position: " + hole.getBorePosition());
		System.out.println("  Hole diameter: " + hole.getDiameter());
		System.out.println("  Bore diameter: " + hole.getBoreDiameter());
	}

	private static void printBoreSection(ComponentInterface component, String indent)
	{
		BoreSection bs = (BoreSection)component;
		System.out.println(indent + "Boresection:");
		System.out.println( indent + "  Length: " + bs.getLength());
		System.out.println(indent + "  Left radius: " + bs.getLeftRadius());
		System.out.println(indent + "  Right radius: " + bs.getRightRadius());
	}

	private static void printMouthpiece(ComponentInterface component)
	{
		System.out.println("Mouthpiece:");
		Mouthpiece mp = (Mouthpiece) component;
		System.out.println("  Position: " + mp.getBorePosition());
		System.out.println("  Bore diameter: " + mp.getBoreDiameter());
		System.out.println("  Headspace:");
		List<BoreSection> hs = mp.getHeadspace();
		for (BoreSection section : hs)
		{
			printBoreSection(section, "    ");
		}
	}

	@Test
	public final void test1HoleOptimization()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/1HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/1HoleNAF1Tuning.xml";

			Instrument instrument = getInstrumentFromXml();
			instrument.convertToMetres();

			instrument.updateComponents();
			printComponents("\n\nOne Hole, first update", instrument.components);
			printTermination(instrument.getTermination());

			instrument.updateComponents();
			printComponents("\nOne Hole, second update", instrument.components);
			printTermination(instrument.getTermination());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void test6HoleOptimization()
	{
		try
		{
			inputInstrumentXML = "com/wwidesigner/optimization/example/6HoleNAF1.xml";
			inputTuningXML = "com/wwidesigner/optimization/example/6HoleNAF1Tuning.xml";

			Instrument instrument = getInstrumentFromXml();
			instrument.convertToMetres();

			instrument.updateComponents();
			printComponents("\n\nSix Holes, first update", instrument.components);
			printTermination(instrument.getTermination());

			instrument.updateComponents();
			printComponents("\nSix Holes, second update", instrument.components);
			printTermination(instrument.getTermination());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected Instrument getInstrumentFromXml() throws Exception
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		File inputFile = getInputFile(inputInstrumentXML, geometryBindFactory);
		Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(
				inputFile, true);
		instrument.updateComponents();

		return instrument;
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

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		UpdateComponentTest test = new UpdateComponentTest();
		test.testNoHoleOptimization();
		test.test1HoleOptimization();
		test.test6HoleOptimization();
	}

}
