package com.wwidesigner.gui.util;

import java.text.DecimalFormat;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class DoubleCellRenderer extends DefaultTableCellRenderer
{
	private int fractionalDigits;

	public DoubleCellRenderer(int aFractionalDigits)
	{
		this.fractionalDigits = aFractionalDigits;
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	public void setValue(Object value)
	{
		try
		{
			DecimalFormat format = new DecimalFormat();
			format.setMaximumFractionDigits(fractionalDigits);
			format.setMinimumFractionDigits(fractionalDigits);
			if (value == null)
			{
				super.setValue("");
			}
			else if (value instanceof String)
			{
				super.setValue(format.format(Double.valueOf((String) value)));
			}
			else
			{
				super.setValue(format.format(value));
			}
		}
		catch (Exception e)
		{
			super.setValue("");
		}
	}
}
