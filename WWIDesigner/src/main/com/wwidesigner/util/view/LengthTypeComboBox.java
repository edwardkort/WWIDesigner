package com.wwidesigner.util.view;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.wwidesigner.util.Constants.LengthType;

public class LengthTypeComboBox extends JComboBox<LengthType>
{
	public LengthTypeComboBox()
	{
		super(LengthType.values());
		((JLabel) getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		setEditable(false);
	}

	@Override
	public void setSelectedItem(Object value)
	{
		if (value instanceof LengthType)
		{
			super.setSelectedItem(value);
		}
		else if (value instanceof String)
		{
			LengthType type = LengthType.valueOf((String) value);
			if (type != null)
			{
				super.setSelectedItem(type);
			}
		}
	}

	public void setSelectedLengthType(LengthType type)
	{
		setSelectedItem(type);
	}

	public void setSelectedLengthType(String typeName)
	{
		setSelectedItem(typeName);
	}

	public LengthType getSelectedLengthType()
	{
		return (LengthType) getSelectedItem();
	}

	public String getSelectedLengthTypeName()
	{
		LengthType type = getSelectedLengthType();
		String name = null;
		if (type != null)
		{
			name = type.name();
		}

		return name;
	}

}
