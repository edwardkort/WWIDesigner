/**
 * 
 */
package com.wwidesigner.util;

import java.io.File;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.custommonkey.xmlunit.XMLConstants;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 * @author kort
 * 
 */
public abstract class BindFactory
{
	@SuppressWarnings("rawtypes")
	protected Map<String, Class> bindToDomainMap;
	@SuppressWarnings("rawtypes")
	protected Map<String, Class> domainToBindMap;

	protected abstract void createBindToDomaimMap();

	protected abstract void createDomainToBindMap();

	protected abstract void setPackagePath();

	protected abstract void setSchemaName();

	protected abstract Object createElement(Object obj);

	protected String packagePath;
	protected String schema;

	public BindFactory()
	{
		setPackagePath();
		setSchemaName();
		createBindToDomaimMap();
		createDomainToBindMap();
	}

	/**
	 * 
	 * @param inputFile
	 * @return The bind JAXBElement representing the root of the XML
	 * @throws Exception
	 */
	public Object unmarshalXml(File inputFile) throws Exception
	{
		return unmarshalXml(inputFile, false);
	}

	public Object unmarshalXml(String inputFileName, boolean toDomainObject)
			throws Exception
	{
		File inputFile = new File(inputFileName);
		return unmarshalXml(inputFile, toDomainObject);
	}

	public Object unmarshalXml(File inputFile, boolean toDomainObject)
			throws Exception
	{
		JAXBContext jc = JAXBContext.newInstance(packagePath);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		// Do validation
		unmarshaller.setSchema(getSchema());

		Object bindObject = ((JAXBElement<?>) unmarshaller.unmarshal(inputFile))
				.getValue();

		if (!toDomainObject)
		{
			return bindObject;
		}

		Object domainObject = mapObject(bindObject, bindToDomainMap);
		return domainObject;
	}

	public void marshalToXml(Object input, String outputXmlName)
			throws Exception
	{
		File outputXml = new File(outputXmlName);
		marshalToXml(input, outputXml);
	}

	public void marshalToXml(Object input, File outputXml) throws Exception
	{
		Object mappedInput = mapObject(input, domainToBindMap);
		if (mappedInput == null)
		{
			mappedInput = input;
		}
		JAXBContext context = JAXBContext.newInstance(mappedInput.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(createElement(mappedInput), outputXml);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object mapObject(Object source,
			Map<String, Class> sourceToDestinationClassMap)
	{
		String sourceName = source.getClass().getName();
		Class destinationClass = sourceToDestinationClassMap.get(sourceName);
		if (destinationClass == null)
		{
			return null;
		}

		Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();
		Object destination = mapper.map(source, destinationClass);

		return destination;
	}

	private Schema getSchema()
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

	private File getFileFromName(String name)
	{
		String filePath = ClassLoader.getSystemResource(name).getPath();

		return new File(filePath);
	}

}
