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
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.DoubleCellRenderer;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

/**
 * A panel that displays three columns of data from a Tuning: Note name, note
 * frequency, and fingering pattern
 * 
 * @author Edward N. Kort
 *
 */
public class TuningPanel extends FingeringPatternPanel
{
	int fingeringColumnIdx;

	/**
	 * Create a panel with components of a specified preferred width.
	 * 
	 * @param componentWidth
	 *            - preferred width of display/edit components.
	 */
	public TuningPanel(int width)
	{
		super(width);
		numberOfColumns = 3;
		fingeringColumnIdx = 2;
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public TuningPanel()
	{
		super();
		numberOfColumns = 3;
		fingeringColumnIdx = 2;
	}

	@Override
	protected String[] columnNames()
	{
		return (new String[] { "Symbol", "Frequency", "Fingering" });
	}

	@Override
	protected Object[] emptyRow()
	{
		return (new Object[] { null, null, new Fingering(numberOfHoles) });
	}

	@Override
	protected Object[] rowData(Fingering fingering)
	{
		Object[] newRow;
		newRow = new Object[] { null, null, fingering };

		if (fingering.getNote() != null)
		{
			newRow[0] = fingering.getNote().getName();
			newRow[1] = fingering.getNote().getFrequency();
		}

		return newRow;
	}

	@Override
	public void resetTableData(int numRows)
	{
		super.resetTableData(numRows);
		fingeringList.getColumn("Frequency").setCellRenderer(
				new DoubleCellRenderer(4));
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
					loadData(tuning, false);
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

	@Override
	public Tuning getData()
	{
		stopTableEditing();
		Tuning fingerings = new Tuning();
		fingerings.setName(nameWidget.getText());
		fingerings.setComment(descriptionWidget.getText());
		fingerings.setNumberOfHoles(Integer.parseInt(numberOfHolesWidget
				.getText()));
		fingerings.setFingering(getTableData());

		return fingerings;
	}
	
	/**
	 * @return	The fingering from the first selected row of the fingering table,
	 * 		 	or from the first row, if no rows are selected.
	 */
	public Fingering getSelectedFingering()
	{
		int row;
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			row = 0;
		}
		else
		{
			if (selectedRows.length > 1)
			{
				Arrays.sort(selectedRows);
			}
			row = selectedRows[0];
		}
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		return getRowData(model, row);
	}

	protected DefaultTableModel getTableModel()
	{
		return new NumericTableModel(String.class, Fingering.class);
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

		DefaultTableModel model = getTableModel();
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
	protected Fingering getRowData(DefaultTableModel model, int row)
	{
		Note note = new Note();
		String name = (String) model.getValueAt(row, 0);
		if (name == null || name.trim().length() == 0)
		{
			// Skip lines with no name.
			return null;
		}
		note.setName(name.trim());
		Double freq = (Double) model.getValueAt(row, 1);
		note.setFrequency(freq);
		Fingering value = (Fingering) model.getValueAt(row,
				fingeringColumnIdx);
		if (value != null)
		{
			value.setNote(note);
		}
		return value;
	}
	
	@Override
	protected boolean isFingeringPopulated(Fingering fingering)
	{
		if (fingering == null)
		{
			return false;
		}
		Note note = fingering.getNote();
		if (note.getName() == null
			|| note.getName().trim().length() <= 0
			|| note.getFrequency() == null)
		{
			return false;
		}
		return true;
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
			model.setValueAt(new Fingering(numberOfHoles), row,
					fingeringColumnIdx);
		}
	}

}
