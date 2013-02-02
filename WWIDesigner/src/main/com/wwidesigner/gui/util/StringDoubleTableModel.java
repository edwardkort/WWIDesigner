package com.wwidesigner.gui.util;

import javax.swing.table.DefaultTableModel;

public class StringDoubleTableModel extends DefaultTableModel
{
	@Override
	public void setValueAt(Object value, int row, int column)
	{
		if (column == 1)
		{
			Double dblValue = null;

			try
			{
				if (value instanceof String)
				{
					dblValue = Double.valueOf((String) value);
				}
				else
				{
					dblValue = (Double) value;
				}
			}
			catch (Exception e)
			{
			}

			super.setValueAt(dblValue, row, column);
		}
		else
		{
			super.setValueAt(value, row, column);
		}
	}

}
