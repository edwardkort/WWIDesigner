/* Transfer handler for transferring data from selected full rows (all columns) of a JIDE table. */
package com.wwidesigner.gui.util;

import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.grid.JideTableTransferHandler;

public class TableSourceRowTransferHandler extends JideTableTransferHandler
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
		int numColumns = table.getColumnCount();
		Object[] data = new Object[selectedRows.length];
		if (numColumns == 1)
		{
			for (int row = selectedRows.length - 1; row >= 0; row--)
			{
				data[row] = model.getValueAt(selectedRows[row], 0);
			}
		}
		else
		{
			for (int row = selectedRows.length - 1; row >= 0; row--)
			{
				Object[] rowData = new Object[numColumns];
				for (int col = 0; col < numColumns; col++)
				{
					rowData[col] = model.getValueAt(selectedRows[row], col);
				}
				data[row] = rowData;
			}
		}

		return new ArrayTransferable(data);
	}

}
