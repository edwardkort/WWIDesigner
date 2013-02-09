package com.wwidesigner.note.view;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.wwidesigner.note.Fingering;

public class FingeringRenderer implements TableCellRenderer
{
	public Dimension getPreferredSize()
	{
		FingeringComponent fingering = new FingeringComponent(0);

		Dimension dim = fingering.getPreferredSize();

		return new Dimension(dim.width, dim.height);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (value != null && value instanceof Fingering)
		{
			return new FingeringComponent((Fingering) value, isSelected);
		}

		return null;
	}

}
