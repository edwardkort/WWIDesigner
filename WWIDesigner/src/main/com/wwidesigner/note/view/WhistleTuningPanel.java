package com.wwidesigner.note.view;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.wwidesigner.note.Fingering;
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
	public WhistleTuningPanel(int width)
	{
		super(width);
		numberOfColumns = 5;
		fingeringColumnIdx = 4;
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public WhistleTuningPanel()
	{
		super();
		numberOfColumns = 5;
		fingeringColumnIdx = 4;
	}

	@Override
	protected String[] columnNames()
	{
		return (new String[] { "Symbol", "Frequency", "Min Freq", "Max Freq",
				"Fingering" });
	}

	@Override
	protected Object[] emptyRow()
	{
		return (new Object[] { null, null, null, null,
				new Fingering(numberOfHoles) });
	}

	@Override
	protected Object[] rowData(Fingering fingering)
	{
		Object[] newRow = new Object[] { null, null, null, null, fingering };

		if (fingering.getNote() != null)
		{
			newRow[0] = fingering.getNote().getName();
			newRow[1] = fingering.getNote().getFrequency();
			newRow[2] = fingering.getNote().getFrequencyMin();
			newRow[3] = fingering.getNote().getFrequencyMax();
		}

		return newRow;
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
				Note note = fingering.getNote();
				Double freq = (Double) model.getValueAt(i, 2);
				note.setFrequencyMin(freq);
				freq = (Double) model.getValueAt(i, 3);
				note.setFrequencyMax(freq);
			}
		}
		
		return data;
	}

}
