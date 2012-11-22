package com.wwidesigner.gui.util;

import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class TableSourceTransferHandler extends TransferHandler
{
	@Override
	public int getSourceActions(JComponent c)
	{
		return COPY;
	}

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
			data[row] = model.getValueAt(selectedRows[row], 0);
		}

		return new ArrayTransferable(data);
	}
}
