/**
 * 
 */
package com.wwidesigner.note.bind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import org.custommonkey.xmlunit.DetailedDiff;
import org.junit.Test;

import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.XmlDiff;

/**
 * @author kort
 * 
 */
public class ExplicitTuningMappingTest
{
	private BindFactory bindFactory;
	private String inputXmlName = "com/wwidesigner/note/bind/example/A_5-hole_NAF_standard_tuning.xml";
	private String outputXmlName = "mapperTest.xml";

	@Test
	public final void test()
	{
		bindFactory = NoteBindFactory.getInstance();
		try
		{
			File inputFile = getInputFile();
			File outputFile = getOutputFile();

			// Unmarshal XML to domain object Tuning
			Object domainObject = bindFactory.unmarshalXml(inputFile, true);

			// Marshal domain object to XML
			bindFactory.marshalToXml(domainObject, outputFile);

			// Compare the input and output XML files
			XmlDiff diff = new XmlDiff(inputFile, outputFile);
			DetailedDiff detailedDiff = new DetailedDiff(diff);
			assertTrue(detailedDiff.toString(), detailedDiff.identical());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

	private File getInputFile() throws FileNotFoundException
	{
		String inputXmlPath = bindFactory.getPathFromName(inputXmlName);
		File inputFile = new File(inputXmlPath);

		return inputFile;
	}

	private File getOutputFile() throws FileNotFoundException
	{
		String inputFilePath = bindFactory.getPathFromName(inputXmlName);
		String writePath = inputFilePath.substring(0,
				inputFilePath.lastIndexOf('/') + 1);

		// Delete output file
		File outputFile = new File(writePath + outputXmlName);
		outputFile.delete();

		return outputFile;
	}

}
