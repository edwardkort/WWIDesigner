/**
 * 
 */
package com.wwidesigner.note.bind;

import com.wwidesigner.util.BindFactory;

/**
 * @author kort
 * 
 */
public class NoteBindFactory extends BindFactory
{

	public Object createElement(Object obj)
	{
		String objName = obj.getClass().getSimpleName();
		Object element = null;
		ObjectFactory objFactory = new ObjectFactory();

		switch (objName)
		{
			case "XmlScaleSymbolList":
				element = objFactory
						.createScaleSymbolList((XmlScaleSymbolList) obj);
				break;
			case "XmlScale":
				element = objFactory.createScale((XmlScale) obj);
				break;
			case "XmlFingeringPattern":
				element = objFactory
						.createFingeringPattern((XmlFingeringPattern) obj);
				break;
			case "XmlTemperament":
				element = objFactory.createTemperament((XmlTemperament) obj);
				break;
			case "XmlTuning":
				element = objFactory.createTuning((XmlTuning) obj);
				break;
		}

		return element;
	}

	@Override
	protected void setPackagePath()
	{
		packagePath = "com.wwidesigner.note.bind";

	}

	@Override
	protected void setSchemaName()
	{
		schema = "com/wwidesigner/note/bind/Tuning.xsd";

	}

}
