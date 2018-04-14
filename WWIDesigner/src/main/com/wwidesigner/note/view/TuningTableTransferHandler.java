/** Class used for transferring one or more columns of data to a table on a TuningPanel. */
package com.wwidesigner.note.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;
import com.wwidesigner.gui.util.ArrayTransferable;
import com.wwidesigner.gui.util.TableTransferHandler;
import com.wwidesigner.note.Fingering;

public class TuningTableTransferHandler extends TableTransferHandler
{
	private TuningPanel tuningPanel;

	public TuningTableTransferHandler(TuningPanel tuningPanel)
	{
		this.tuningPanel = tuningPanel;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		// Find the target for the import.

		int row, column;
		JTable table = (JTable) info.getComponent();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
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

		Transferable source = info.getTransferable();
		Class<?> colClass = table.getColumnClass(column);
		DataFlavor dataFlavor;

		if (source.isDataFlavorSupported(ArrayTransferable.TABLE_FLAVOUR))
		{
			// Import data has multiple columns.
			dataFlavor = ArrayTransferable.TABLE_FLAVOUR;
		}
		else if (colClass.equals(Double.class))
		{
			dataFlavor = ArrayTransferable.DOUBLES_FLAVOUR;
		}
		else if (colClass.equals(Fingering.class))
		{
			dataFlavor = ArrayTransferable.FINGERINGS_FLAVOUR;
		}
		else
		{
			dataFlavor= ArrayTransferable.STRINGS_FLAVOUR; 
		}
		
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
			if (source.isDataFlavorSupported(ArrayTransferable.TABLE_FLAVOUR))
			{
				// Import data has multiple columns.
				dataFlavor = ArrayTransferable.TABLE_FLAVOUR;
			}
			else if (! source.isDataFlavorSupported(dataFlavor))
			{
				return false;
			}
		}

		// Retrieve the data.

		Object[] data;
		try
		{
			data = (Object[]) source.getTransferData(dataFlavor);
		}
		catch (Exception e)
		{
			return false;
		}

		if (data == null || data.length == 0)
		{
			return false;
		}

		// Handle number of holes in source and target

		if (colClass == Fingering.class && dataFlavor == ArrayTransferable.FINGERINGS_FLAVOUR)
		{
			// Importing a single fingering column.  Check for changes in number of holes,
			// and for closable end.
			Fingering sourceFingering = (Fingering) data[0];
			int sourceNumberOfHoles = sourceFingering.getNumberOfHoles();
			Fingering targetFingering = (Fingering) model
					.getValueAt(row, column);
			int targetNumberOfHoles = targetFingering.getNumberOfHoles();
			if (sourceNumberOfHoles != targetNumberOfHoles)
			{
				tuningPanel.setNumberOfHoles(sourceNumberOfHoles);
			}
			if (sourceFingering.getOpenEnd() != null
					&& targetFingering.getOpenEnd() == null)
			{
				tuningPanel.setClosableEnd(true);
			}
		}

		// Check the data type of all elements before changing any table cells.

		for (Object datum : data)
		{
			if (dataFlavor == ArrayTransferable.TABLE_FLAVOUR)
			{
				Object[] rowData = (Object[]) datum;
				int col;
				// Check for type compatibility of all columns before pasting any columns.
				for (col = 0; col < rowData.length; ++ col)
				{
					if (! rowData[col].getClass().equals(table.getColumnClass(column + col)))
					{
						return false;
					}
				}
			}
			else
			{
				if (! datum.getClass().equals(colClass))
				{
					return false;
				}
			}
		}

		// Load the data in the table.

		try
		{
			for (Object datum : data)
			{
				if (row >= model.getRowCount())
				{
					model.insertRow(row, tuningPanel.emptyRow());
				}
				if (dataFlavor == ArrayTransferable.TABLE_FLAVOUR)
				{
					Object[] rowData = (Object[]) datum;
					int col;
					for (col = 0; col < rowData.length; ++ col)
					{
						model.setValueAt(rowData[col], row, column + col);
					}
				}
				else
				{
					model.setValueAt(datum, row, column);
				}
				++row;
			}
		}
		catch (Exception e)
		{
			return false;
		}
		
		return true;
	}

}
