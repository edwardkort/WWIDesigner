package com.wwidesigner.note.view;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.wwidesigner.note.Fingering;

/**
 * A panel that displays four columns of data from a Tuning: Note name, note
 * frequency, fingering pattern, and optimization weight
 * 
 * @author Edward N. Kort
 *
 */
public class TuningWithWeightsPanel extends TuningPanel
{
	public TuningWithWeightsPanel(int width)
	{
		super(width);
		numberOfColumns = 4;
		setEditableNumberOfHoles(false);
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public TuningWithWeightsPanel()
	{
		super();
		numberOfColumns = 4;
		setEditableNumberOfHoles(false);
	}

	@Override
	protected String[] columnNames()
	{
		return (new String[] { "Symbol", "Frequency", "Fingering", "Weight" });
	}

	@Override
	protected Object[] emptyRow()
	{
		return (new Object[] { null, null, new Fingering(numberOfHoles), null });
	}

	@Override
	protected Object[] rowData(Fingering fingering)
	{
		Object[] newRow;
		newRow = new Object[] { null, null, fingering, null };

		if (fingering.getNote() != null)
		{
			newRow[0] = fingering.getNote().getName();
			newRow[1] = fingering.getNote().getFrequency();
			newRow[3] = fingering.getOptimizationWeight();
		}

		return newRow;
	}

	@Override
	protected DefaultTableModel getTableModel()
	{
		DefaultTableModel model = new DefaultTableModel()
		{
			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				if (columnIndex == 0)
				{
					return String.class;
				}
				else if (columnIndex == getColumnCount() - 1)
				{
					return Integer.class;
				}
				return Double.class;
			}
		};

		return model;
	}

	@Override
	protected List<Fingering> getTableData()
	{
		List<Fingering> data = super.getTableData();
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Fingering value = (Fingering) model.getValueAt(i,
					fingeringColumnIdx);
			if (value != null)
			{
				Fingering fingering = data.get(i);
				Integer weight = (Integer) model.getValueAt(i, 3);
				fingering.setOptimizationWeight(weight);
			}
		}

		return data;
	}

}
