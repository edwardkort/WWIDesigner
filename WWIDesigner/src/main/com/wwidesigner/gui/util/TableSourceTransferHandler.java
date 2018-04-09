/* Transfer handler for transferring data from selected rows and columns of a JIDE table. */
package com.wwidesigner.gui.util;

import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.grid.JideTableTransferHandler;

public class TableSourceTransferHandler extends JideTableTransferHandler
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
		int[] selectedCols = table.getSelectedColumns();
		Arrays.sort(selectedRows);
		Arrays.sort(selectedCols);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int numColumns = selectedCols.length;
		Object[] data = new Object[selectedRows.length];
		if (numColumns == 1)
		{
			for (int row = selectedRows.length - 1; row >= 0; row--)
			{
				data[row] = model.getValueAt(selectedRows[row], selectedCols[0]);
			}
		}
		else
		{
			for (int row = selectedRows.length - 1; row >= 0; row--)
			{
				Object[] rowData = new Object[numColumns];
				for (int col = 0; col < numColumns; col++)
				{
					rowData[col] = model.getValueAt(selectedRows[row], selectedCols[col]);
				}
				data[row] = rowData;
			}
		}

		return new ArrayTransferable(data);
	}
}
