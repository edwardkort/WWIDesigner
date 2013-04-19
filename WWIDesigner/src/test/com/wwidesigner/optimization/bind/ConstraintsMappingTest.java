package com.wwidesigner.optimization.bind;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.custommonkey.xmlunit.DetailedDiff;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.junit.Test;

import com.wwidesigner.util.AbstractXmlTest;
import com.wwidesigner.util.XmlDiff;

public class ConstraintsMappingTest extends AbstractXmlTest<Constraints>
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
			com.wwidesigner.optimization.Constraints domainConstraints = mapper
					.map(inputElement,
							com.wwidesigner.optimization.Constraints.class);
			Constraints bindConstraints = mapper.map(domainConstraints,
					Constraints.class);

			outputFile = new File(writePath + "mapperTest.xml");
			outputFile.delete();
			bindFactory.marshalToXml(bindConstraints, outputFile);
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
		inputSymbolXML = "com/wwidesigner/optimization/example/7-hole_broad-constraints.xml";
	}

	@Override
	protected void setOutputSymbolXML()
	{
		outputSymbolXML = "7-hole_constraints_test.xml";
	}

	@Override
	protected void setBindFactory()
	{
		bindFactory = OptimizationBindFactory.getInstance();
	}

}
