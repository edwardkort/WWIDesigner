/**
 * JPanel to display and edit tuning scales.
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DoubleCellRenderer;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.gui.util.TableSourceRowTransferHandler;
import com.wwidesigner.note.Scale;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

public class ScalePanel extends JPanel implements KeyListener,
		TableModelListener
{
	private JTextField nameWidget;
	private JTextPane descriptionWidget;
	private JTable noteTable;
	private boolean namePopulated;
	private boolean notesPopulated;
	private List<DataPopulatedListener> populatedListeners;

	public static final String LOAD_PAGE_ID = "loadData";
	public static final int DEFAULT_WIDTH = 190;

	public ScalePanel()
	{
		setLayout(new GridBagLayout());
		setNameWidget();
		setDescriptionWidget();
		setNoteTableWidget();
		configureDragAndDrop();
	}

	private void setNameWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Name: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		nameWidget = new JTextField();
		nameWidget.addKeyListener(this);
		nameWidget.setBorder(new LineBorder(Color.BLACK));
		nameWidget.setPreferredSize(new Dimension(DEFAULT_WIDTH, 20));
		nameWidget.setMinimumSize(new Dimension(100, 20));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(nameWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		add(panel, gbc);
	}

	public void setName(String name)
	{
		nameWidget.setText(name);
	}

	private void setDescriptionWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Description: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		descriptionWidget = new JTextPane();
		descriptionWidget.setBorder(new LineBorder(Color.BLACK));
		descriptionWidget.setPreferredSize(new Dimension(DEFAULT_WIDTH, 65));
		descriptionWidget.setMinimumSize(new Dimension(100, 20));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(descriptionWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 10, 0);
		add(panel, gbc);
	}

	public void setDescription(String description)
	{
		descriptionWidget.setText(description);
	}

	private void setNoteTableWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		JLabel label = new JLabel("Notes:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new NumericTableModel();
		model.addTableModelListener(this);
		noteTable = new JideTable(model);
		setTableCellSelectionEnabled(true);
		resetTableData();
		noteTable.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(noteTable);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(DEFAULT_WIDTH, 200));
		scrollPane.setMinimumSize(new Dimension(140, 180));
		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		gbc.gridy = 2;
		add(panel, gbc);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateWidgets(Scale scale)
	{
		if (scale != null)
		{
			nameWidget.setText(scale.getName());
			isNamePopulated();

			descriptionWidget.setText(scale.getComment());

			Vector rows = new Vector();
			for (Scale.Note note : scale.getNote())
			{
				Vector row = new Vector();
				row.add(note.getName());
				row.add(note.getFrequency());
				rows.add(row);
			}

			setTableData(rows);
			areNotesPopulated();
		}
	}

	public void resetTableData()
	{
		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		model.setDataVector(new Object[60][2], new String[] { "Symbol",
				"Frequency" });
		noteTable.getColumn("Frequency").setCellRenderer(
				new DoubleCellRenderer(4));

	}

	public void deleteSelectedNotes()
	{
		int[] selectedRows = noteTable.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	public void deleteUnselectedNotes()
	{
		int[] selectedRows = noteTable.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		int numRows = model.getRowCount();
		for (int row = numRows - 1; row >= 0; row--)
		{
			if (Arrays.binarySearch(selectedRows, row) < 0)
			{
				model.removeRow(row);
			}
		}
	}

	public void insertNoteAboveSelection()
	{
		int[] selectedRows = noteTable.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		model.insertRow(topIndex, (Object[]) null);

		ListSelectionModel selModel = noteTable.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.setSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	public void insertNoteBelowSelection()
	{
		int[] selectedRows = noteTable.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int bottomIndex = selectedRows[selectedRows.length - 1];

		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		model.insertRow(bottomIndex + 1, (Object[]) null);
	}

	public Scale loadScale(File file)
	{
		Scale scale = null;

		if (file != null)
		{
			BindFactory bindery = NoteBindFactory.getInstance();
			try
			{
				scale = (Scale) bindery.unmarshalXml(file, true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid Scale file.");
			}
		}

		return scale;
	}

	public void saveScale(File file)
	{
		Scale scale = getScale();

		BindFactory bindery = NoteBindFactory.getInstance();
		try
		{
			bindery.marshalToXml(scale, file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
		}
	}

	@SuppressWarnings("rawtypes")
	public Scale getScale()
	{
		Scale scale = new Scale();
		scale.setName(nameWidget.getText());
		scale.setComment(descriptionWidget.getText());
		Vector notes = (Vector) getTableData();
		for (Object noteObj : notes)
		{
			Vector noteValue = (Vector) noteObj;
			Scale.Note note = new Scale.Note();
			note.setName((String) noteValue.get(0));
			note.setFrequency((Double) noteValue.get(1));
			scale.addNote(note);
		}

		return scale;
	}

	@SuppressWarnings("rawtypes")
	public void setTableData(Vector data)
	{
		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("Symbol");
		columnNames.add("Frequency");
		model.setDataVector(data, columnNames);
		noteTable.getColumn("Frequency").setCellRenderer(
				new DoubleCellRenderer(4));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getTableData()
	{
		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		Vector data = (Vector) model.getDataVector();
		Vector clonedData = new Vector();
		for (Object row : data)
		{
			Vector rowData = (Vector) row;
			Vector clonedRow = new Vector();
			String name = (String) rowData.get(0);
			if (name == null)
			{
				clonedRow.add(null);
			}
			else
			{
				clonedRow.add(new String(name));
			}
			Double interval = (Double) rowData.get(1);
			if (interval == null)
			{
				clonedRow.add(null);
			}
			else
			{
				clonedRow.add(new Double(interval));
			}
			clonedData.add(clonedRow);
		}

		return clonedData;
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		areNotesPopulated();
	}

	private void areNotesPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) noteTable.getModel();
		notesPopulated = false;

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String noteName = (String) model.getValueAt(i, 0);
			Double frequency = (Double) model.getValueAt(i, 1);
			if (noteName != null && noteName.length() > 0 && frequency != null)
			{
				notesPopulated = true;
				break;
			}
		}

		fireDataStateChanged();
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{
	}

	@Override
	public void keyReleased(KeyEvent arg0)
	{
		isNamePopulated();
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
	}

	private void isNamePopulated()
	{
		String name = nameWidget.getText();

		namePopulated = (name != null && name.length() > 0);
		fireDataStateChanged();
	}

	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		if (populatedListeners == null)
		{
			populatedListeners = new ArrayList<DataPopulatedListener>();
		}
		populatedListeners.add(listener);
	}

	private void fireDataStateChanged()
	{
		if (populatedListeners == null)
		{
			return;
		}

		List<DataPopulatedEvent> events = new ArrayList<DataPopulatedEvent>();
		DataPopulatedEvent event = new DataPopulatedEvent(this, namePopulated
				&& notesPopulated);
		events.add(event);
		event = new DataPopulatedEvent(this, LOAD_PAGE_ID, notesPopulated);
		events.add(event);
		for (DataPopulatedEvent thisEvent : events)
		{
			for (DataPopulatedListener listener : populatedListeners)
			{
				listener.dataStateChanged(thisEvent);
			}
		}
	}

	private void configureDragAndDrop()
	{
		noteTable.setDragEnabled(true);
		noteTable.setTransferHandler(new TableSourceRowTransferHandler());
		nameWidget.setTransferHandler(new NoOpTransferHandler());
		descriptionWidget.setTransferHandler(new NoOpTransferHandler());
	}

	public void setTableCellSelectionEnabled(boolean editable)
	{
		noteTable.setCellSelectionEnabled(editable);
		noteTable.setRowSelectionAllowed(true);
	}

}
