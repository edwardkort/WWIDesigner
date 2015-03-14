package com.wwidesigner.gui.util;

import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public abstract class NumberFormatTableCellRenderer extends
		DefaultTableCellRenderer
{
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col)
	{
		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, col);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		NumberFormat format = NumberFormat.getNumberInstance();
		int decimalPrecision = getDecimalPrecision(table, row, col);
		format.setMinimumFractionDigits(decimalPrecision);
		format.setMaximumFractionDigits(decimalPrecision);
		label.setText(value == null ? "" : format.format(value));

		return label;
	}

	public abstract int getDecimalPrecision(JTable table, int row, int col);
}
