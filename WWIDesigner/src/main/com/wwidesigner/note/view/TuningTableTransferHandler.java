package com.wwidesigner.note.view;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import com.wwidesigner.note.Fingering;

public class TuningTableTransferHandler extends TransferHandler
{
	private TuningPanel tuningPanel;

	public TuningTableTransferHandler(TuningPanel tuningPanel)
	{
		this.tuningPanel = tuningPanel;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		if (!canImport(info))
		{
			return false;
		}

		JTable table = (JTable) info.getComponent();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
		int row = dl.getRow();
		List<? extends List> dropData = getDropData(info);

		if (dropData.size() == 0)
		{
			return false;
		}

		int numColumns = dropData.get(0).size();
		if (numColumns == 1) // Handle number of holes in source and target
		{
			Fingering sourceFingering = (Fingering) dropData.get(0).get(0);
			int sourceNumberOfHoles = sourceFingering.getNumberOfHoles();
			Fingering targetFingering = (Fingering) model.getValueAt(row, 2);
			int targetNumberOfHoles = targetFingering.getNumberOfHoles();
			if (sourceNumberOfHoles != targetNumberOfHoles)
			{
				tuningPanel.resetFingeringColumn(sourceNumberOfHoles);
			}
		}
		int numberOfHoles = tuningPanel.getNumberOfHoles();
		for (List rowData : dropData)
		{
			if (row >= model.getRowCount())
			{
				model.insertRow(row, new Object[] { null, null,
						new Fingering(numberOfHoles) });
			}
			if (numColumns == 1)
			{
				model.setValueAt(rowData.get(0), row++, 2);
			}
			else if (numColumns == 2)
			{
				model.setValueAt(rowData.get(0), row, 0);
				model.setValueAt(rowData.get(1), row++, 1);
			}
		}

		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<? extends List> getDropData(
			TransferHandler.TransferSupport info)
	{
		ArrayList goodData = new ArrayList<ArrayList>();
		try
		{
			Object[] data = (Object[]) info.getTransferable().getTransferData(
					new DataFlavor(Object[].class, Object[].class.getName()));
			if (data == null || data.length == 0)
			{
				return goodData;
			}
			for (Object datum : data)
			{
				Object[] row = (Object[]) datum;
				if (row == null)
				{
					continue;
				}
				// No check for different row lengths
				int numColumns = row.length;
				// A fingering
				if (numColumns == 1)
				{
					Object cellDatum = row[0];
					if (cellDatum != null && cellDatum instanceof Fingering)
					{
						ArrayList goodRow = new ArrayList();
						goodRow.add(cellDatum);
						goodData.add(goodRow);
					}
				}
				// A note
				else if (numColumns == 2)
				{
					ArrayList goodRow = new ArrayList();
					try
					{
						String cell0 = (String) row[0];
						if (cell0 != null && cell0.length() > 0)
						{
							goodRow.add(cell0);
							Double cell1 = (Double) row[1];
							goodRow.add(cell1);
							goodData.add(goodRow);
						}
					}
					catch (Exception e)
					{
						e.getMessage();
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.getMessage();
		}

		return goodData;
	}

}
