package com.wwidesigner.gui.util;

import java.awt.datatransfer.DataFlavor;

import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

public class TableTransferHandler extends TransferHandler
{
	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int column = dl.getColumn();

		if (column == 0)
		{
			if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
				return false;
			}
		}
		else if (column == 1)
		{
			if (!info.isDataFlavorSupported(new DataFlavor(Double.class,
					Double.class.getName())))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		if (!canImport(info))
		{
			return false;
		}

		TableModel model = ((JTable) info.getComponent()).getModel();
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int row = dl.getRow();
		int column = dl.getColumn();
		DataFlavor dataFlavor = null;
		switch (column)
		{
			case 0:
				dataFlavor = DataFlavor.stringFlavor;
				break;
			case 1:
				dataFlavor = new DataFlavor(Double.class,
						Double.class.getName());
				break;
			default:
				dataFlavor = DataFlavor.stringFlavor;
		}
		try
		{
			Object[] data = (Object[]) info.getTransferable().getTransferData(
					dataFlavor);
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
