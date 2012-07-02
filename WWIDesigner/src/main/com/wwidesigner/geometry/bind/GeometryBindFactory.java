/**
 * 
 */
package com.wwidesigner.geometry.bind;

import com.wwidesigner.util.BindFactory;

/**
 * @author kort
 * 
 */
public class GeometryBindFactory extends BindFactory
{

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

}
