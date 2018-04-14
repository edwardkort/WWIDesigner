/** Class used for transferring a single column to a table. */
package com.wwidesigner.gui.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

import com.jidesoft.grid.JideTableTransferHandler;
import com.wwidesigner.note.Fingering;

public class TableTransferHandler extends JideTableTransferHandler
{
	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		// Find the target of the import.

		int column;
		JTable table = (JTable) info.getComponent();
		if (info.isDrop())
		{
			JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
			column = dl.getColumn();
		}
		else
		{
			int[] selectedCols = table.getSelectedColumns();
			Arrays.sort(selectedCols);
			column = selectedCols[0];
		}
		Class<?> colClass = table.getColumnClass(column);

		// Test whether datatypes are compatible.

		if (colClass.equals(Double.class))
		{
			return info.isDataFlavorSupported(ArrayTransferable.DOUBLES_FLAVOUR);
		}
		if (colClass.equals(Fingering.class))
		{
			return info.isDataFlavorSupported(ArrayTransferable.FINGERINGS_FLAVOUR);
		}
		if (colClass.equals(String.class))
		{
			if (info.isDataFlavorSupported(ArrayTransferable.STRINGS_FLAVOUR))
			{
				return true;
			}

			if (info.isDataFlavorSupported(ArrayTransferable.DOUBLES_FLAVOUR)
					|| info.isDataFlavorSupported(ArrayTransferable.FINGERINGS_FLAVOUR))
			{
				// Don't allow Doubles or Fingerings to be dropped on string columns.
				return false;
			}
			return info.isDataFlavorSupported(DataFlavor.stringFlavor);
		}
		return false;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		// Find the target for the import.

		int row, column;
		JTable table = (JTable) info.getComponent();
		TableModel model = table.getModel();
		if (info.isDrop())
		{
			JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
			row = dl.getRow();
			column = dl.getColumn();
		}
		else
		{
			int[] selectedRows = table.getSelectedRows();
			int[] selectedCols = table.getSelectedColumns();
			Arrays.sort(selectedRows);
			Arrays.sort(selectedCols);
			row = selectedRows[0];
			column = selectedCols[0];
		}
		
		// Determine what is being imported, and check compatibility.

		Class<?> colClass = table.getColumnClass(column);
		DataFlavor dataFlavor = ArrayTransferable.STRINGS_FLAVOUR;
		if (colClass.equals(Double.class))
		{
			dataFlavor = ArrayTransferable.DOUBLES_FLAVOUR;
		}
		else if (colClass.equals(Fingering.class))
		{
			dataFlavor = ArrayTransferable.FINGERINGS_FLAVOUR;
		}
		
		Transferable source = info.getTransferable();
		if (! info.isDataFlavorSupported(dataFlavor))
		{
			// Primary flavor not supported.  Try converting from String.
			if (! info.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
				return false;
			}
			try
			{
				source = new ArrayTransferable((String) source.getTransferData(DataFlavor.stringFlavor));
			}
			catch (Exception ex)
			{
				return false;
			}
			if (! source.isDataFlavorSupported(dataFlavor))
			{
				return false;
			}
		}

		try
		{
			Object[] data = (Object[]) source.getTransferData(dataFlavor);
			// Check that all array elements are of the required class,
			// before setting any table cells.
			for (Object datum : data)
			{
				if (! datum.getClass().equals(colClass))
				{
					return false;
				}
			}
			for (Object datum : data)
			{
				model.setValueAt(datum, row++, column);
			}
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}
}
