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

	protected Object createElement(Object obj)
	{
		String objName = obj.getClass().getSimpleName();
		Object element = null;
		ObjectFactory objFactory = new ObjectFactory();

		switch (objName)
		{
			case "ScaleSymbolList":
				element = objFactory
						.createScaleSymbolList((ScaleSymbolList) obj);
				break;
			case "Scale":
				element = objFactory.createScale((Scale) obj);
				break;
			case "FingeringPattern":
				element = objFactory
						.createFingeringPattern((FingeringPattern) obj);
				break;
			case "Temperament":
				element = objFactory.createTemperament((Temperament) obj);
				break;
			case "Tuning":
				element = objFactory.createTuning((Tuning) obj);
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
