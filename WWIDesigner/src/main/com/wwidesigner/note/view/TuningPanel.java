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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.StringDoubleTableModel;
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
	 * @param componentWidth - preferred width of display/edit components.
	 */
	public TuningPanel(int width)
	{
		super( width );
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public TuningPanel()
	{
		super();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void populateWidgets(FingeringPattern fingerings, boolean isFromFile)
	{
		if (fingerings != null)
		{
			name = fingerings.getName();
			nameWidget.setText(name);
			description = fingerings.getComment();
			descriptionWidget.setText(description);
			numberOfHoles = (Integer) fingerings.getNumberOfHoles();
			numberOfHolesWidget.setText(numberOfHoles.toString());

			fingeringList.getModel().removeTableModelListener(this);
			resetTableData(0, numberOfHoles);
			DefaultTableModel model = (DefaultTableModel) fingeringList
					.getModel();
			fingeringList.setAutoResizeMode(JideTable.AUTO_RESIZE_FILL);
			fingeringList.setFillsRight(true);
			fingeringList.setCellSelectionEnabled(true);
			TableColumn column = fingeringList.getColumn("Fingering");
			column.setPreferredWidth(new FingeringComponent(fingerings
					.getNumberOfHoles()).getPreferredSize().width);
			for (Fingering fingering : fingerings.getFingering())
			{
				Vector row = new Vector();
				Note note = fingering.getNote();
				row.add(note.getName());
				row.add(note.getFrequency());
				row.add(fingering);
				model.addRow(row);
			}
			fingeringList.getModel().addTableModelListener(this);

			isNamePopulated();
			areFingeringsPopulated();
			if (! isFromFile)
			{
				fireDataStateChanged();
			}
		}
	}

	// @Override
	// public void saveFingeringPattern(File file)
	// {
	// Tuning tuning = (Tuning) getFingeringPattern();
	//
	// BindFactory bindery = NoteBindFactory.getInstance();
	// try
	// {
	// bindery.marshalToXml(tuning, file);
	// }
	// catch (Exception ex)
	// {
	// JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
	// }
	// }

	@Override
	public boolean loadFingeringPattern(File file)
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
					populateWidgets(tuning, true);
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
	public Tuning getFingeringPattern()
	{
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

		DefaultTableModel model = new StringDoubleTableModel();
		model.addTableModelListener(this);
		fingeringList = new JideTable(model);
		resetTableData(0, numberOfHoles);
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
	}

	@Override
	public void resetTableData(int numRows, int numHoles)
	{
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.setDataVector(new Object[0][3], new String[] { "Symbol",
				"Frequency", "Fingering" });
		fingeringList.setFillsGrids(false);
		TableCellRenderer renderer = new FingeringRenderer();
		TableColumn column = fingeringList.getColumn("Fingering");
		column.setCellRenderer(renderer);
		column.setCellEditor(new FingeringEditor());
		if (numRows > 0)
		{
			fingeringList.setAutoResizeMode(JideTable.AUTO_RESIZE_FILL);
			fingeringList.setFillsRight(true);
			column.setPreferredWidth(new FingeringComponent(numHoles == 0 ? 1
					: numHoles).getPreferredSize().width);
			for (int i = 0; i < numRows; i++)
			{
				model.addRow(new Object[] { null, null, new Fingering(numHoles) });
			}
		}
		fingeringList.setRowHeight(((FingeringRenderer) renderer)
				.getPreferredSize().height);

		areFingeringsPopulated();
	}

	@Override
	protected List<Fingering> getTableData()
	{
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
			if (freq != null)
			{
				note.setFrequency(freq);
			}
			Fingering value = (Fingering) model.getValueAt(i, 2);
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
			Fingering fingering = (Fingering) model.getValueAt(i, 2);
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

	@Override
	public void insertFingeringAboveSelection()
	{
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0 || numberOfHoles == null)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.insertRow(topIndex, new Object[] { null, null,
				new Fingering(numberOfHoles) });

		ListSelectionModel selModel = fingeringList.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.addSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	@Override
	public void insertFingeringBelowSelection()
	{
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int bottomIndex = selectedRows[selectedRows.length - 1];

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.insertRow(bottomIndex + 1, new Object[] { null, null,
				new Fingering(numberOfHoles) });
	}

	public void resetFingeringColumn(int numberOfHoles)
	{
		setNumberOfHoles(numberOfHoles);
		TableColumn column = fingeringList.getColumn("Fingering");
		column.setPreferredWidth(new FingeringComponent(numberOfHoles == 0 ? 1
				: numberOfHoles).getPreferredSize().width);
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		int numRows = model.getRowCount();
		for (int row = 0; row < numRows; row++)
		{
			model.setValueAt(new Fingering(numberOfHoles), row, 2);
		}
	}

}
