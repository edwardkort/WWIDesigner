/**
 * 
 */
package com.wwidesigner.note.bind;

import java.util.HashMap;

import com.wwidesigner.util.BindFactory;

/**
 * @author kort
 * 
 */
public class NoteBindFactory extends BindFactory
{

	@Override
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

	@SuppressWarnings("rawtypes")
	@Override
	protected void createBindToDomaimMap()
	{
		if (bindToDomainMap == null)
		{
			bindToDomainMap = new HashMap<String, Class>();
			bindToDomainMap.put(FingeringPattern.class.getName(),
					com.wwidesigner.note.FingeringPattern.class);
			bindToDomainMap.put(Scale.class.getName(),
					com.wwidesigner.note.Scale.class);
			bindToDomainMap.put(ScaleSymbolList.class.getName(),
					com.wwidesigner.note.ScaleSymbolList.class);
			bindToDomainMap.put(Temperament.class.getName(),
					com.wwidesigner.note.Temperament.class);
			bindToDomainMap.put(Tuning.class.getName(),
					com.wwidesigner.note.Tuning.class);
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
					com.wwidesigner.note.FingeringPattern.class.getName(),
					FingeringPattern.class);
			domainToBindMap.put(com.wwidesigner.note.Scale.class.getName(),
					Scale.class);
			domainToBindMap.put(
					com.wwidesigner.note.ScaleSymbolList.class.getName(),
					ScaleSymbolList.class);
			domainToBindMap.put(
					com.wwidesigner.note.Temperament.class.getName(),
					Temperament.class);
			domainToBindMap.put(com.wwidesigner.note.Tuning.class.getName(),
					Tuning.class);
		}
	}

}
