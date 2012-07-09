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
public class TuningMappingTest extends AbstractXmlTest<Tuning>
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
			com.wwidesigner.note.Tuning domainTuning = mapper.map(inputElement,
					com.wwidesigner.note.Tuning.class);
			Tuning bindTuning = mapper.map(domainTuning, Tuning.class);

			outputFile = new File(writePath + "mapperTest.xml");
			outputFile.delete();
			bindFactory.marshalToXml(bindTuning, outputFile);
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
		inputSymbolXML = "com/wwidesigner/note/bind/example/A_5-hole_NAF_standard_tuning.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "A_5-hole_NAF_standard_tuning_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = NoteBindFactory.getInstance();

	}

}
