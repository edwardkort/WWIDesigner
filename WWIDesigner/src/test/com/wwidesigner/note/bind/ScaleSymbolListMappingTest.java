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
public class ScaleSymbolListMappingTest extends
		AbstractXmlTest<ScaleSymbolList>
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
			com.wwidesigner.note.ScaleSymbolList domainScaleList = mapper.map(
					inputElement, com.wwidesigner.note.ScaleSymbolList.class);
			ScaleSymbolList bindScaleList = mapper.map(domainScaleList,
					ScaleSymbolList.class);

			outputFile = new File(writePath + "mapperTest.xml");
			outputFile.delete();
			bindFactory.marshalToXml(bindScaleList, outputFile);
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
		inputSymbolXML = "com/wwidesigner/note/bind/example/Western_Chromatic_Symbols.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "Western_Chromatic_Symbols_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = new NoteBindFactory();

	}

}
