package com.wwidesigner.note.view;

import java.util.List;

import javax.swing.table.DefaultTableModel;

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
	protected List<Fingering> getTableData()
	{
		List<Fingering> data = super.getTableData();
		if (numberOfColumns != 5)
		{
			return data;
		}
		
		// Table also contains min and max columns.
		// Add min and max to the data returned.
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Fingering value = (Fingering) model.getValueAt(i,
					fingeringColumnIdx);
			if (value != null)
			{
				Fingering fingering = data.get(i);
				Note note = fingering.getNote();
				Double freq = (Double) model.getValueAt(i, 2);
				note.setFrequencyMin(freq);
				freq = (Double) model.getValueAt(i, 3);
				note.setFrequencyMax(freq);
			}
		}
		
		return data;
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
