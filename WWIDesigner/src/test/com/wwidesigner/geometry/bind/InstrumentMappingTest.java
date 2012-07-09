/**
 * 
 */
package com.wwidesigner.geometry.bind;

import static org.junit.Assert.assertEquals;
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
public class InstrumentMappingTest extends AbstractXmlTest<Instrument>
{

	@Test
	public final void testInstrumentMapping()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
			com.wwidesigner.geometry.Instrument domainInstrument = mapper.map(
					inputElement, com.wwidesigner.geometry.Instrument.class);
			String instrumentName = domainInstrument.getName();
			assertEquals("Domain instrument name does not match bind name",
					"D NAF", instrumentName);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

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
			com.wwidesigner.geometry.Instrument domainInstrument = mapper.map(
					inputElement, com.wwidesigner.geometry.Instrument.class);
			Instrument bindInstrument = mapper.map(domainInstrument,
					Instrument.class);

			outputFile = new File(writePath + "mapperTest.xml");
			outputFile.delete();
			bindFactory.marshalToXml(bindInstrument, outputFile);
			XmlDiff diff = new XmlDiff(inputFile, outputFile);
			DetailedDiff detailedDiff = new DetailedDiff(diff);
			assertTrue(detailedDiff.toString(), detailedDiff.identical());
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

	@Test
	public final void testRoundTripWithConversion()
	{
		try
		{
			if (inputElement == null)
			{
				unmarshalInput();
			}
			Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
			com.wwidesigner.geometry.Instrument domainInstrument = mapper.map(
					inputElement, com.wwidesigner.geometry.Instrument.class);
			domainInstrument.convertToMetres();
			domainInstrument.convertToLengthType();
			Instrument bindInstrument = mapper.map(domainInstrument,
					Instrument.class);

			outputFile = new File(writePath + "mapperTest.xml");
			outputFile.delete();
			bindFactory.marshalToXml(bindInstrument, outputFile);
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
		inputSymbolXML = "com/wwidesigner/geometry/bind/example/D_NAF.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "D_NAF_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = GeometryBindFactory.getInstance();
	}

}
