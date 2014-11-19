/**
 * JPanel to display and edit tunings.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.FingeringPattern;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

public class TuningPanel extends FingeringPatternPanel
{
	/**
	 * Create a panel with components of a specified preferred width.
	 * Optionally include columns for min and max frequency.
	 * @param componentWidth - preferred width of display/edit components.
	 */
	public TuningPanel(int width, boolean withMinMax)
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

	/**
	 * Create a panel with components of a specified preferred width.
	 * @param componentWidth - preferred width of display/edit components.
	 */
	public TuningPanel(int width)
	{
		super( width );
		this.numberOfColumns = 3;
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public TuningPanel()
	{
		super();
		this.numberOfColumns = 3;
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
	public boolean loadFromFile(File file)
	{
		Tuning tuning = null;

		if (file != null)
		{
			BindFactory bindery = NoteBindFactory.getInstance();
			try
			{
				tuning = (Tuning) bindery.unmarshalXml(file, true);
				if (tuning != null)
				{
					loadData(tuning, true);
					return true;
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid Tuning file.");
			}
		}

		return false;
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

	@Override
	public Tuning getData()
	{
		stopTableEditing();
		if (! namePopulated || ! fingeringsPopulated)
		{
			return null;
		}
		Tuning fingerings = new Tuning();
		fingerings.setName(nameWidget.getText());
		fingerings.setComment(descriptionWidget.getText());
		fingerings.setNumberOfHoles(Integer.parseInt(numberOfHolesWidget
				.getText()));
		fingerings.setFingering(getTableData());

		return fingerings;
	}

	@Override
	protected void setFingeringListWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Fingering list: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new NumericTableModel(String.class,Fingering.class);
		fingeringList = new JideTable(model);
		resetTableData(0);
		fingeringList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(fingeringList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(componentWidth, 210));
		scrollPane.setMinimumSize(new Dimension(250, 200));
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
		model.addTableModelListener(this);
	}

	@Override
	protected List<Fingering> getTableData()
	{
		stopTableEditing();
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		ArrayList<Fingering> data = new ArrayList<Fingering>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Note note = new Note();
			String name = (String) model.getValueAt(i, 0);
			if (name == null || name.trim().length() == 0)
			{
				// Skip lines with no name.
				continue;
			}
			note.setName(name.trim());
			Double freq = (Double) model.getValueAt(i, 1);
			note.setFrequency(freq);
			if (numberOfColumns == 5)
			{
				freq = (Double) model.getValueAt(i, 2);
				note.setFrequencyMin(freq);
				freq = (Double) model.getValueAt(i, 3);
				note.setFrequencyMax(freq);
			}
			Fingering value = (Fingering) model.getValueAt(i, numberOfColumns - 1);
			if (value != null)
			{
				value.setNote(note);
				data.add(value);
			}
		}
		return data;
	}

	@Override
	protected void areFingeringsPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		fingeringsPopulated = false;

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String noteName = (String) model.getValueAt(i, 0);
			Fingering fingering = (Fingering) model.getValueAt(i, numberOfColumns - 1);
			if ( noteName != null && noteName.trim().length() > 0
					&& fingering != null)
			{
				fingeringsPopulated = true;
				break;
			}
		}
	}

	protected void configureDragAndDrop()
	{
		fingeringList.setTransferHandler(new TuningTableTransferHandler(this));
		nameWidget.setTransferHandler(new NoOpTransferHandler());
		descriptionWidget.setTransferHandler(new NoOpTransferHandler());
		numberOfHolesWidget.setTransferHandler(new NoOpTransferHandler());
	}

	public void resetFingeringColumn(int numberOfHoles)
	{
		stopTableEditing();
		setNumberOfHoles(numberOfHoles);
		FingeringRenderer renderer = new FingeringRenderer(numberOfHoles);
		TableColumn column = fingeringList.getColumn("Fingering");
		column.setPreferredWidth(renderer.getPreferredSize().width);
		column.setMinWidth(renderer.getMinimumSize().width);
		fingeringList.setRowHeight(renderer.getPreferredSize().height);
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		int numRows = model.getRowCount();
		for (int row = 0; row < numRows; row++)
		{
			model.setValueAt(new Fingering(numberOfHoles), row, numberOfColumns - 1);
		}
	}

}
