package com.wwidesigner.note.view;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.wwidesigner.gui.util.DoubleCellRenderer;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.FingeringPattern;
import com.wwidesigner.note.Note;

/**
 * A panel that displays five columns of data from a Tuning: Note name, note
 * frequency, minimum tuning frequency, maximum tuning frequency, and fingering
 * pattern
 * 
 * @author Edward N. Kort
 *
 */
public class WhistleTuningPanel extends TuningPanel
{
	/**
	 * Create a panel with components of a specified preferred width.
	 * Optionally include columns for min and max frequency.
	 * @param componentWidth - preferred width of display/edit components.
	 */
	public WhistleTuningPanel(int width, boolean withMinMax)
	{
		super( width );
		if (withMinMax)
		{
			this.numberOfColumns = 5;
		}
		else
		{
			this.numberOfColumns = 3;
		}
	}

	public WhistleTuningPanel(int width)
	{
		super(width);
		numberOfColumns = 3;
		fingeringColumnIdx = 2;
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public WhistleTuningPanel()
	{
		super();
		numberOfColumns = 3;
		fingeringColumnIdx = 2;
	}

	@Override
	protected String[] columnNames()
	{
		if (numberOfColumns == 5)
		{
			return (new String[] { "Symbol", "Frequency",
					"Min Freq", "Max Freq", "Fingering" });
		}
		return (new String[] { "Symbol", "Frequency", "Fingering" });
	}

	@Override
	protected Object[] emptyRow()
	{
		if (numberOfColumns == 5)
		{
			return (new Object[] {null, null, null, null, new Fingering(numberOfHoles) });
		}
		return (new Object[] {null, null, new Fingering(numberOfHoles) });
	}

	@Override
	protected Object[] rowData(Fingering fingering)
	{
		Object[] newRow;
		if (numberOfColumns == 5)
		{
			newRow = new Object[] {null, null, null, null, fingering};
		}
		else
		{
			newRow = new Object[] {null, null, fingering};
		}

		if (fingering.getNote() != null)
		{
			newRow[0] = fingering.getNote().getName();
			newRow[1] = fingering.getNote().getFrequency();
			if (numberOfColumns == 5)
			{
				newRow[2] = fingering.getNote().getFrequencyMin();
				newRow[3] = fingering.getNote().getFrequencyMax();
			}
		}
		
		return newRow;
	}

	@Override
	protected Fingering getRowData(DefaultTableModel model, int row)
	{
		Fingering value = super.getRowData(model, row);
		if (value == null || numberOfColumns != 5)
		{
			return value;
		}

		// Table also contains min and max columns.
		// Add min and max to the data returned.
		Note note = value.getNote();
		Double freq = (Double) model.getValueAt(row, 2);
		note.setFrequencyMin(freq);
		freq = (Double) model.getValueAt(row, 3);
		note.setFrequencyMax(freq);
		return value;
	}
	
	@Override
	protected boolean isFingeringPopulated(Fingering fingering)
	{
		// Row must have a fingering.
		if (fingering == null)
		{
			return false;
		}
		// Row must have a note name.
		Note note = fingering.getNote();
		if (note.getName() == null
			|| note.getName().trim().length() <= 0)
		{
			return false;
		}
		// Row must have at least one frequency.
		if (note.getFrequency() == null
			&& note.getFrequencyMin() == null
			&& note.getFrequencyMax() == null)
		{
			return false;
		}
		return true;
	}

	@Override
	public void resetTableData(int numRows)
	{
		super.resetTableData(numRows);
		if (numberOfColumns != 5)
		{
			return;
		}
		TableColumn column = fingeringList.getColumn("Min Freq");
		if (column != null)
		{
			column.setCellRenderer(new DoubleCellRenderer(2));
		}
		column = fingeringList.getColumn("Max Freq");
		if (column != null)
		{
			column.setCellRenderer(new DoubleCellRenderer(2));
		}
	}

	/* (non-Javadoc)
	 * @see com.wwidesigner.note.view.FingeringPatternPanel#loadData(com.wwidesigner.note.FingeringPattern, boolean)
	 */
	@Override
	public void loadData(FingeringPattern fingerings, boolean suppressChangeEvent)
	{
		if (hasMinMax(fingerings))
		{
			// If the tuning has min or max frequency data, display it.
			numberOfColumns = 5;
			fingeringColumnIdx = 4;
		}
		super.loadData(fingerings, suppressChangeEvent);
	}

	/**
	 * Test whether a tuning has min/max frequency data.
	 */
	static protected boolean hasMinMax(FingeringPattern fingerings)
	{
		for (Fingering fingering : fingerings.getFingering())
		{
			Note note = fingering.getNote();
			if (note != null)
			{
				if (note.getFrequencyMin() != null
					|| note.getFrequencyMax() != null)
				{
					return true;
				}
			}
		}
		return false;
	}
}
