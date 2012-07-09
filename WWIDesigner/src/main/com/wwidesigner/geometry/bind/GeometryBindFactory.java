/**
 * 
 */
package com.wwidesigner.geometry.bind;

import java.util.HashMap;

import com.wwidesigner.util.BindFactory;

/**
 * @author kort
 * 
 */
public class GeometryBindFactory extends BindFactory
{

	private static GeometryBindFactory instance;

	private GeometryBindFactory()
	{
	}

	public static BindFactory getInstance()
	{
		if (instance == null)
		{
			instance = new GeometryBindFactory();
		}

		return instance;
	}

	@Override
	protected Object createElement(Object obj)
	{
		String objName = obj.getClass().getSimpleName();
		Object element = null;
		ObjectFactory objFactory = new ObjectFactory();

		switch (objName)
		{
			case "Instrument":
				element = objFactory.createInstrument((Instrument) obj);
				break;
		}

		return element;
	}

	@Override
	protected void setPackagePath()
	{
		packagePath = "com.wwidesigner.geometry.bind";

	}

	@Override
	protected void setSchemaName()
	{
		schema = "com/wwidesigner/geometry/bind/Instrument.xsd";

	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void createBindToDomaimMap()
	{
		if (bindToDomainMap == null)
		{
			bindToDomainMap = new HashMap<String, Class>();
			bindToDomainMap.put(Instrument.class.getName(),
					com.wwidesigner.geometry.Instrument.class);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void createDomainToBindMap()
	{
		if (domainToBindMap == null)
		{
			domainToBindMap = new HashMap<String, Class>();
			domainToBindMap.put(
					com.wwidesigner.geometry.Instrument.class.getName(),
					Instrument.class);
		}
	}

}
