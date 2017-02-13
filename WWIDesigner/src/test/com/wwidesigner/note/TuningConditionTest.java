/**
 * 
 */
package com.wwidesigner.note;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import org.junit.Test;

import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

/**
 * @author Burton Patkau
 * 
 */
public class TuningConditionTest
{
	protected String inputTuningXML = "com/wwidesigner/optimization/example/A4-Just-Tuning.xml";

	@Test
	public final void testFingeringPatternConditions()
	{
		try
		{
			Tuning tuning = getTuningFromXml(inputTuningXML);

			assertFalse("Initial tuning has closable end", tuning.hasClosableEnd());
			tuning.getFingering().get(5).setOpenEnd(true);
			assertTrue("No closable end when openEnd is true", tuning.hasClosableEnd());
			tuning.getFingering().get(5).setOpenEnd(false);
			assertTrue("No closable end when openEnd is false", tuning.hasClosableEnd());
			tuning.getFingering().get(5).setOpenEnd(null);
			assertFalse("Final tuning has closable end", tuning.hasClosableEnd());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testTuningConditions()
	{
		try
		{
			Tuning tuning = getTuningFromXml(inputTuningXML);

			assertFalse("Initial tuning has min/max", tuning.hasMinMax());
			tuning.getFingering().get(3).getNote().setFrequencyMin(100.0);
			assertTrue("No Min/Max found when Min is set", tuning.hasMinMax());
			tuning.getFingering().get(3).getNote().setFrequencyMin(null);
			tuning.getFingering().get(3).getNote().setFrequencyMax(100.0);
			assertTrue("No Min/Max found when Max is set", tuning.hasMinMax());
			tuning.getFingering().get(3).getNote().setFrequencyMax(null);
			assertFalse("Final tuning has min/max", tuning.hasMinMax());

			assertFalse("Initial tuning has optimization weights", tuning.hasWeights());
			tuning.getFingering().get(8).setOptimizationWeight(2);
			assertTrue("No weights when weight is set to 2", tuning.hasWeights());
			tuning.getFingering().get(8).setOptimizationWeight(1);
			assertFalse("Weights found when weight is set to 1", tuning.hasWeights());
			tuning.getFingering().get(8).setOptimizationWeight(0);
			assertTrue("No weights when weight is set to 0", tuning.hasWeights());
			tuning.getFingering().get(8).setOptimizationWeight(null);
			assertFalse("Final tuning has optimization weights", tuning.hasWeights());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
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
