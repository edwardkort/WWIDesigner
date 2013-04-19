package com.wwidesigner.optimization.bind;

import java.util.HashMap;

import com.wwidesigner.util.BindFactory;

public class OptimizationBindFactory extends BindFactory
{
	private static OptimizationBindFactory instance;

	private OptimizationBindFactory()
	{

	}

	public static BindFactory getInstance()
	{
		if (instance == null)
		{
			instance = new OptimizationBindFactory();
		}

		return instance;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void createBindToDomaimMap()
	{
		if (bindToDomainMap == null)
		{
			bindToDomainMap = new HashMap<String, Class>();
			bindToDomainMap.put(Constraints.class.getName(),
					com.wwidesigner.optimization.Constraints.class);
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
					com.wwidesigner.optimization.Constraints.class.getName(),
					Constraints.class);
		}
	}

	@Override
	protected void setPackagePath()
	{
		packagePath = "com.wwidesigner.optimization.bind";

	}

	@Override
	protected void setSchemaName()
	{
		schema = "com/wwidesigner/optimization/bind/Constraints.xsd";

	}

	@Override
	protected Object createElement(Object obj)
	{
		String objName = obj.getClass().getSimpleName();
		Object element = null;
		ObjectFactory objFactory = new ObjectFactory();

		switch (objName)
		{
			case "Constraints":
				element = objFactory.createConstraints((Constraints) obj);
				break;
		}

		return element;
	}

}
