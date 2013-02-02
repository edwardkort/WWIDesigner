package com.wwidesigner.gui.util;

import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TableSourceRowTransferHandler extends TableSourceTransferHandler
{
	@Override
	public Transferable createTransferable(JComponent c)
	{
		JTable table = (JTable) c;
		int[] selectedRows = table.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Object[] data = new Object[selectedRows.length];
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			int numColumns = model.getColumnCount();
			Object[] rowData = new Object[numColumns];
			for (int col = 0; col < numColumns; col++)
			{
				rowData[col] = model.getValueAt(selectedRows[row], col);
			}
			data[row] = rowData;
		}

		return new ArrayTransferable(data);
	}

}
