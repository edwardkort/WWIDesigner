/**
 * 
 */
package com.wwidesigner.util;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.XMLConstants;

/**
 * @author kort
 * 
 */
public abstract class BindFactory
{

	protected abstract void setPackagePath();

	protected abstract void setSchemaName();

	protected abstract Object createElement(Object obj);

	protected String packagePath;
	protected String schema;

	public BindFactory()
	{
		setPackagePath();
		setSchemaName();
	}

	public Object unmarshalXml(File inputFile) throws Exception
	{
		JAXBContext jc = JAXBContext.newInstance(packagePath);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		// Do validation
		unmarshaller.setSchema(getSchema());

		return ((JAXBElement<?>) unmarshaller.unmarshal(inputFile)).getValue();
	}

	public void marshalToXml(Object input, File outputXml) throws Exception
	{
		JAXBContext context = JAXBContext.newInstance(input.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(createElement(input), outputXml);
	}

	public Schema getSchema()
	{
		try
		{
			SchemaFactory sf = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			return sf.newSchema(getFileFromName(schema));
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public String getPathFromName(String name)
	{
		return ClassLoader.getSystemResource(name).getPath();
	}

	public File getFileFromName(String name)
	{
		String filePath = ClassLoader.getSystemResource(name).getPath();

		return new File(filePath);
	}

}
