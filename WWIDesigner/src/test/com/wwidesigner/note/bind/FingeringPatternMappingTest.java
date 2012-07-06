/**
 * 
 */
package com.wwidesigner.note.bind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.custommonkey.xmlunit.DetailedDiff;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.junit.Test;

import com.wwidesigner.util.AbstractXmlTest;
import com.wwidesigner.util.XmlDiff;

/**
 * @author kort
 * 
 */
public class FingeringPatternMappingTest extends
		AbstractXmlTest<FingeringPattern>
{

	@Test
	public final void testRoundtripMapping()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
			com.wwidesigner.note.FingeringPattern domainFingeringPattern = mapper
					.map(inputElement,
							com.wwidesigner.note.FingeringPattern.class);
			FingeringPattern bindFingeringPattern = mapper.map(
					domainFingeringPattern, FingeringPattern.class);

			outputFile = new File(writePath + "mapperTest.xml");
			outputFile.delete();
			bindFactory.marshalToXml(bindFingeringPattern, outputFile);
			XmlDiff diff = new XmlDiff(inputFile, outputFile);
			DetailedDiff detailedDiff = new DetailedDiff(diff);
			assertTrue(detailedDiff.toString(), detailedDiff.identical());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

	@Override
	protected void setInputSymbolXML()
	{
		inputSymbolXML = "com/wwidesigner/note/bind/example/5-hole_NAF_standard_fingering.xml";

	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "5-hole_NAF_standard_fingering_test.xml";

	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = new NoteBindFactory();

	}

}
