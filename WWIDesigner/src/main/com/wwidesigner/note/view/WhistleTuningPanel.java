/**
 * JPanel to display and edit tuning data, including up to 6 columns of tabular data.
 * 
 * Copyright (C) 2016, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.note.view;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.wwidesigner.gui.util.DoubleCellRenderer;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.FingeringPattern;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * JPanel to display and edit all available Tuning data, including up to six
 * columns of tabular data: Note name, note frequency, minimum tuning frequency,
 * maximum tuning frequency, fingering pattern, and optimization weight. For
 * fingering pattern, uses alternate renderer and editor to include open/closed
 * end flag if any such flags are present in the Tuning.
 * 
 * @author Edward N. Kort
 *
 */
public class WhistleTuningPanel extends TuningPanel
{
	/**
	 * Create a panel with components of a specified preferred width.
	 * Optionally include columns for min and max frequency and optimization weight.
	 * @param componentWidth - preferred width of display/edit components.
	 */
	public WhistleTuningPanel(int width, boolean withMinMax, boolean withWeight)
	{
		super( width );
		setNumberOfColumns(withMinMax, withWeight);
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
	
	protected void setNumberOfColumns(boolean withMinMax, boolean withWeight)
	{
		if (withMinMax)
		{
			this.numberOfColumns = 5;
		}
		else
		{
			this.numberOfColumns = 3;
		}
		fingeringColumnIdx = this.numberOfColumns - 1;
		if (withWeight)
		{
			++this.numberOfColumns;
		}
	}

	@Override
	protected String[] columnNames()
	{
		if (numberOfColumns == 6)
		{
			return (new String[] { "Symbol", "Frequency",
					"Min Freq", "Max Freq", "Fingering", "Weight" });
		}
		if (numberOfColumns == 5)
		{
			return (new String[] { "Symbol", "Frequency",
					"Min Freq", "Max Freq", "Fingering" });
		}
		if (numberOfColumns == 4)
		{
			return (new String[] { "Symbol", "Frequency",
					"Fingering", "Weight" });
		}
		return (new String[] { "Symbol", "Frequency", "Fingering" });
	}

	@Override
	protected Object[] emptyRow()
	{
		if (numberOfColumns == 6)
		{
			return (new Object[] {null, null, null, null, new Fingering(numberOfHoles), null });
		}
		if (numberOfColumns == 5)
		{
			return (new Object[] {null, null, null, null, new Fingering(numberOfHoles) });
		}
		if (numberOfColumns == 4)
		{
			return (new Object[] {null, null, new Fingering(numberOfHoles), null });
		}
		return (new Object[] {null, null, new Fingering(numberOfHoles) });
	}

	@Override
	protected Object[] rowData(Fingering fingering)
	{
		Object[] newRow;
		if (numberOfColumns == 6)
		{
			newRow = new Object[] {null, null, null, null, fingering, null};
		}
		else if (numberOfColumns == 5)
		{
			newRow = new Object[] {null, null, null, null, fingering};
		}
		else if (numberOfColumns == 4)
		{
			newRow = new Object[] {null, null, fingering, null};
		}
		else
		{
			newRow = new Object[] {null, null, fingering};
		}

		if (fingering.getNote() != null)
		{
			newRow[0] = fingering.getNote().getName();
			newRow[1] = fingering.getNote().getFrequency();
			if (numberOfColumns >= 5)
			{
				newRow[2] = fingering.getNote().getFrequencyMin();
				newRow[3] = fingering.getNote().getFrequencyMax();
				if (numberOfColumns == 6)
				{
					newRow[5] = fingering.getOptimizationWeight();
				}
			}
			else if (numberOfColumns == 4)
			{
				newRow[3] = fingering.getOptimizationWeight();
			}
		}
		
		return newRow;
	}

	@Override
	protected Fingering getRowData(DefaultTableModel model, int row)
	{
		Fingering value = super.getRowData(model, row);
		if (value == null || numberOfColumns == 3)
		{
			return value;
		}

		if (numberOfColumns >= 5)
		{
			// Table also contains min and max columns.
			// Add min and max to the data returned.
			Note note = value.getNote();
			Double freq = (Double) model.getValueAt(row, 2);
			note.setFrequencyMin(freq);
			freq = (Double) model.getValueAt(row, 3);
			note.setFrequencyMax(freq);
			if (numberOfColumns == 6)
			{
				Integer weight = (Integer) model.getValueAt(row, 5);
				value.setOptimizationWeight(weight);
			}
		}
		else if (numberOfColumns == 4)
		{
			Integer weight = (Integer) model.getValueAt(row, 3);
			value.setOptimizationWeight(weight);
		}
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
	public void resetTableData(int numRows, boolean hasClosableEnd)
	{
		super.resetTableData(numRows, hasClosableEnd);
		if (! hasMinMax())
		{
			return;
		}
		TableColumn column;
		column = fingeringList.getColumn("Min Freq");
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
		if (numberOfColumns < 5 && fingerings.hasMinMax())
		{
			// If the tuning has min or max frequency data, display it.
			if (numberOfColumns == 4 || fingerings.hasWeights())
			{
				// Display weights if they were already displayed,
				// or the tuning has non-trivial optimization weights.
				numberOfColumns = 6;
			}
			else
			{
				numberOfColumns = 5;
			}
			fingeringColumnIdx = 4;
		}
		else if (numberOfColumns == 3 && fingerings.hasWeights())
		{
			// If the tuning has non-trivial optimization weights, display them last.
			numberOfColumns = 4;
		}
		super.loadData(fingerings, suppressChangeEvent);
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
					// First column is the note symbol.
					return String.class;
				}
				else if (columnIndex == fingeringColumnIdx)
				{
					// Fingering pattern is last or second-last column.
					return Fingering.class;
				}
				else if (columnIndex == getColumnCount() - 1)
				{
					// Last column, if not fingering pattern, is optimization weight.
					return Integer.class;
				}
				// Everything else is a frequency.
				return Double.class;
			}
		};

		return model;
	}

	/**
	 * Re-build this panel, adding or removing columns for min and max frequency
	 * and optimization weight.
	 */
	public void reloadData(boolean withMinMax, boolean withWeight)
	{
		Tuning tuning = getData();
		if (! withMinMax || ! withWeight)
		{
			// Delete existing min/max or weight data from the tuning.
			for (Fingering fingering : tuning.getFingering())
			{
				if (! withMinMax && fingering.getNote() != null)
				{
					fingering.getNote().setFrequencyMin(null);
					fingering.getNote().setFrequencyMax(null);
				}
				if (! withWeight)
				{
					fingering.setOptimizationWeight(null);
				}
			}
		}
		setNumberOfColumns(withMinMax, withWeight);
		fingeringList.setModel(getTableModel());
		loadData(tuning, false);
	}

	/**
	 * Test whether this panel has min/max frequency columns.
	 */
	public boolean hasMinMax()
	{
		return numberOfColumns >= 5;
	}

	/**
	 * Test whether this panel has an optimization weight column.
	 */
	public boolean hasWeights()
	{
		return numberOfColumns == 4 || numberOfColumns == 6;
	}

}
