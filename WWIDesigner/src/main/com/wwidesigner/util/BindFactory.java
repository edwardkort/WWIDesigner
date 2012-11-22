/**
 * 
 */
package com.wwidesigner.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
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

	public Object unmarshalXml(String inputFileName, boolean fileInClasspath,
			boolean toDomainObject) throws Exception
	{
		if (fileInClasspath)
		{
			inputFileName = getPathFromName(inputFileName);
		}

		return unmarshalXml(new File(inputFileName), toDomainObject);
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

	public Object unmarshalXml(String xmlString, boolean toDomainObject)
			throws Exception
	{
		JAXBContext jc = JAXBContext.newInstance(packagePath);
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		// Do validation
		unmarshaller.setSchema(getSchema());

		StreamSource strmSource = new StreamSource(new StringReader(xmlString));
		Object bindObject = ((JAXBElement<?>) unmarshaller
				.unmarshal(strmSource)).getValue();

		if (!toDomainObject)
		{
			return bindObject;
		}

		Object domainObject = mapObject(bindObject, bindToDomainMap);
		return domainObject;
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

	public void marshalToXml(Object input, Writer writer) throws Exception
	{
		Object mappedInput = mapObject(input, domainToBindMap);
		if (mappedInput == null)
		{
			mappedInput = input;
		}
		JAXBContext context = JAXBContext.newInstance(mappedInput.getClass());
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setSchema(getSchema());
		marshaller.marshal(createElement(mappedInput), writer);

	}

	public void marshalToXml(Object input, File outputXml) throws Exception
	{
		Writer writer = new FileWriter(outputXml);
		marshalToXml(input, writer);
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

	public String getPathFromName(String name) throws FileNotFoundException
	{
		java.net.URL fileUrl = ClassLoader.getSystemResource(name);
		if (fileUrl == null)
		{
			throw new FileNotFoundException(name + " not found.");
		}
		try 
		{
			return fileUrl.toURI().getPath();
		} 
	    catch (URISyntaxException e) 
	    {
		    return fileUrl.getPath();
		}
	}

	public File getFileFromName(String name) throws FileNotFoundException
	{
		String filePath = getPathFromName(name);

		return new File(filePath);
	}
	
	public boolean isValidXml(String xmlString, String rootElementName, boolean isDomainObject) {
		try {
			Object root = unmarshalXml(xmlString, isDomainObject);
			String rootPath = packagePath;
			if (isDomainObject) {
				rootPath = rootPath.substring(0, rootPath.lastIndexOf('.'));
			}
			Class<?> rootElement = Class.forName(rootPath + "." + rootElementName);
			rootElement.cast(root);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

}
