/**
 * JPanel to display and edit the fingering patterns of a tuning.
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.gui.util.DataChangedProvider;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;
import com.wwidesigner.gui.util.IntegerDocument;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.TableSourceRowTransferHandler;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.FingeringPattern;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

public class FingeringPatternPanel extends JPanel implements FocusListener,
		TableModelListener, DataPopulatedProvider, DataChangedProvider,
		DataChangedListener
{
	public static final String NEW_EVENT_ID = "newData";
	public static final String SAVE_EVENT_ID = "saveData";
	public static final int DEFAULT_WIDTH = 220;

	protected JTextField nameWidget;
	protected JTextPane descriptionWidget;
	protected JTextField numberOfHolesWidget;
	protected JideTable fingeringList;
	protected FingeringRenderer renderer;
	protected int componentWidth;
	protected int numberOfHoles;
	protected int numberOfColumns;
	protected String name;
	protected String description;
	protected boolean namePopulated;
	protected boolean fingeringsPopulated;
	protected List<DataPopulatedListener> populatedListeners;
	protected List<DataChangedListener> dataChangedListeners;

	/**
	 * Create a panel with components of a specified preferred width.
	 * 
	 * @param componentWidth
	 *            - preferred width of display/edit components.
	 */
	public FingeringPatternPanel(int componentWidth)
	{
		this.componentWidth = componentWidth;
		this.numberOfHoles = 0;
		this.numberOfColumns = 1;
		this.name = "";
		this.description = "";
		this.namePopulated = false;
		this.fingeringsPopulated = false;
		setLayout(new GridBagLayout());
		setNameWidget();
		setDescriptionWidget();
		setNumberWidget();
		setFingeringListWidget();
		configureDragAndDrop();
	}

	/**
	 * Create a panel with components of a default preferred width.
	 */
	public FingeringPatternPanel()
	{
		this(DEFAULT_WIDTH);
	}

	/**
	 * Load a fingering pattern from a fingering-pattern XML file.
	 * 
	 * @param file
	 *            - contains XML for a fingering pattern
	 * @return true if the load was successful
	 */
	public boolean loadFromFile(File file)
	{
		FingeringPattern fingerings = null;

		if (file != null)
		{
			BindFactory bindery = NoteBindFactory.getInstance();
			try
			{
				fingerings = (FingeringPattern) bindery
						.unmarshalXml(file, true);
				if (fingerings != null)
				{
					loadData(fingerings, false);
					return true;
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid FingeringPattern file.");
			}
		}

		return false;
	}

	protected String[] columnNames()
	{
		return (new String[] { "Fingering" });
	}

	/**
	 * Return an empty row suitable for the fingering table.
	 */
	protected Object[] emptyRow()
	{
		return (new Fingering[] { new Fingering(numberOfHoles) });
	}

	protected Object[] rowData(Fingering fingering)
	{
		return (new Fingering[] { fingering });
	}

	protected void setEditableNumberOfHoles(boolean isEditable)
	{
		numberOfHolesWidget.setEnabled(isEditable);
	}

	/**
	 * Load a fingering pattern into this panel.
	 * 
	 * @param fingerings
	 *            - fingering pattern to load.
	 * @param suppressChangeEvent
	 *            - if true, don't fire the DataPopulated event.
	 */
	public void loadData(FingeringPattern fingerings,
			boolean suppressChangeEvent)
	{
		if (fingerings != null)
		{
			name = fingerings.getName();
			nameWidget.setText(name);
			description = fingerings.getComment();
			descriptionWidget.setText(description);
			numberOfHoles = (Integer) fingerings.getNumberOfHoles();
			numberOfHolesWidget.setText(Integer.toString(numberOfHoles));

			stopTableEditing();
			fingeringList.getModel().removeTableModelListener(this);
			renderer.setEnableDataChanges(false);
			resetTableData(0);
			DefaultTableModel model = (DefaultTableModel) fingeringList
					.getModel();
			for (Fingering fingering : fingerings.getFingering())
			{
				model.addRow(rowData(fingering));
			}

			fingeringList.getModel().addTableModelListener(this);
			renderer.setEnableDataChanges(true);
			isNamePopulated();
			areFingeringsPopulated();
			if (!suppressChangeEvent)
			{
				fireDataStateChanged();
			}
		}
	}

	@Override
	public void focusGained(FocusEvent event)
	{
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		boolean isDataChanged = false;
		if (event.getSource().equals(numberOfHolesWidget))
		{
			isDataChanged = validateNumberOfHoles();
		}
		else if (event.getSource().equals(nameWidget))
		{
			isDataChanged = isNamePopulated();
		}
		else if (event.getSource().equals(descriptionWidget))
		{
			isDataChanged = isDescriptionChanged();
		}
		if (isDataChanged)
		{
			fireDataStateChanged();
		}
	}

	/**
	 * Verify that there is a name in the nameWidget, and set namePopulated
	 * accordingly.
	 * 
	 * @return true if the name has changed.
	 */
	protected boolean isNamePopulated()
	{
		String newName = nameWidget.getText();

		namePopulated = (newName != null && newName.trim().length() > 0);
		if (newName != null && !newName.equals(name))
		{
			name = newName;
			return true;
		}
		return false;
	}

	/**
	 * Test whether the description in descriptionWidget has changed.
	 * 
	 * @return true if the description has changed.
	 */
	protected boolean isDescriptionChanged()
	{
		String newDescription = descriptionWidget.getText();

		if (newDescription != null && !newDescription.equals(description))
		{
			description = newDescription;
			return true;
		}
		return false;
	}

	/**
	 * Validate the data in numberOfHolesWidget. If the value is invalid,
	 * restore the original value in the widget.
	 * 
	 * @return true if the number of holes has changed.
	 */
	protected boolean validateNumberOfHoles()
	{
		String number = numberOfHolesWidget.getText();

		if (number == null || number.trim().isEmpty())
		{
			JOptionPane.showMessageDialog(this,
					"Number of holes must be a valid number.");
			numberOfHolesWidget.setText(Integer.toString(numberOfHoles));
			return false;
		}

		Integer newNumberOfHoles = Integer.parseInt(number);
		if (newNumberOfHoles < 0)
		{
			JOptionPane.showMessageDialog(this,
					"Number of holes must be non-negative.");
			numberOfHolesWidget.setText(Integer.toString(numberOfHoles));
			return false;
		}
		if (newNumberOfHoles.equals(numberOfHoles))
		{
			return false;
		}
		numberOfHoles = newNumberOfHoles;
		resetTableData(1);
		return true;
	}
	
	protected boolean isFingeringPopulated(Fingering fingering)
	{
		return fingering != null;
	}

	/**
	 * Test whether the fingeringList table contains fingerings, and all
	 * fingerings it contains are valid, and set fingeringsPopulated
	 * accordingly.
	 */
	protected void areFingeringsPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		if (model.getRowCount() == 0)
		{
			// No fingerings.
			fingeringsPopulated = false;
			return;
		}

		fingeringsPopulated = true;
		for (int i = 0; i < model.getRowCount(); i++)
		{
			Fingering value = getRowData(model, i);
			if (! isFingeringPopulated(value))
			{
				fingeringsPopulated = false;
				return;
			}
		}
	}

	protected void stopTableEditing()
	{
		TableCellEditor editor = fingeringList.getCellEditor();
		if (editor != null)
		{
			editor.stopCellEditing();
		}
	}

	public void deleteSelectedFingerings()
	{
		stopTableEditing();
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	public void deleteUnselectedFingerings()
	{
		stopTableEditing();
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			// If there are no selected rows, delete nothing
			// rather than deleting everything.
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		int numRows = model.getRowCount();
		for (int row = numRows - 1; row >= 0; row--)
		{
			if (Arrays.binarySearch(selectedRows, row) < 0)
			{
				model.removeRow(row);
			}
		}
	}

	public void insertFingeringAboveSelection()
	{
		stopTableEditing();
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		if (model.getRowCount() <= 0)
		{
			// If table is empty, we can't select anything.
			// Insert at the top, and leave nothing selected.
			model.insertRow(0, emptyRow());
			return;
		}
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		model.insertRow(topIndex, emptyRow());

		// Re-select the original rows.
		ListSelectionModel selModel = fingeringList.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.addSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	public void insertFingeringBelowSelection()
	{
		stopTableEditing();
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		int bottomIndex = 0; // If table is empty, insert at the top.
		if (model.getRowCount() > 0)
		{
			int[] selectedRows = fingeringList.getSelectedRows();
			if (selectedRows.length == 0)
			{
				return;
			}
			Arrays.sort(selectedRows);
			bottomIndex = selectedRows[selectedRows.length - 1] + 1;
		}

		model.insertRow(bottomIndex, emptyRow());
	}

	public void saveFingeringPattern(File file)
	{
		FingeringPattern fingerings = getData();

		BindFactory bindery = NoteBindFactory.getInstance();
		try
		{
			bindery.marshalToXml(fingerings, file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
		}
	}

	public FingeringPattern getData()
	{
		stopTableEditing();
		if (!namePopulated || !fingeringsPopulated)
		{
			return null;
		}
		FingeringPattern fingerings = new FingeringPattern();
		fingerings.setName(nameWidget.getText());
		fingerings.setComment(descriptionWidget.getText());
		fingerings.setNumberOfHoles(Integer.parseInt(numberOfHolesWidget
				.getText()));
		fingerings.setFingering(getTableData());

		return fingerings;
	}

	protected void setNameWidget()
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
		nameWidget.addFocusListener(this);
		nameWidget.setPreferredSize(new Dimension(componentWidth, 20));
		nameWidget.setMinimumSize(new Dimension(200, 20));
		nameWidget.setMargin(new Insets(2, 4, 2, 4));
		nameWidget.setText(name);
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(nameWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	protected void setDescriptionWidget()
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
		descriptionWidget.addFocusListener(this);
		descriptionWidget.setMargin(new Insets(2, 4, 2, 4));
		descriptionWidget.setBorder(new LineBorder(Color.BLUE));
		descriptionWidget.setPreferredSize(new Dimension(componentWidth, 65));
		descriptionWidget.setMinimumSize(new Dimension(200, 20));
		descriptionWidget.setText(description);
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(descriptionWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	protected void setNumberWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Number of Holes: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		numberOfHolesWidget = new JTextField(3);
		numberOfHolesWidget.addFocusListener(this);
		numberOfHolesWidget.setDocument(new IntegerDocument());
		numberOfHolesWidget.setHorizontalAlignment(JTextField.RIGHT);
		numberOfHolesWidget.setMargin(new Insets(2, 4, 2, 4));
		numberOfHolesWidget.setText("0");
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(numberOfHolesWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

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

		DefaultTableModel model = new DefaultTableModel();
		fingeringList = new JideTable(model);
		resetTableData(0);
		fingeringList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(fingeringList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(componentWidth, 200));
		scrollPane.setMinimumSize(new Dimension(200, 200));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
		model.addTableModelListener(this);
	}

	public int getNumberOfHoles()
	{
		return numberOfHoles;
	}

	public void setName(String name)
	{
		nameWidget.setText(name);
		if (isNamePopulated())
		{
			fireDataStateChanged();
		}
	}

	public void setDescription(String description)
	{
		descriptionWidget.setText(description);
		if (isDescriptionChanged())
		{
			fireDataStateChanged();
		}
	}

	public void setNumberOfHoles(Integer number)
	{
		if (number != null)
		{
			numberOfHolesWidget.setText(number.toString());
			if (validateNumberOfHoles())
			{
				fireDataStateChanged();
			}
		}
	}

	public void resetTableData(int numRows)
	{
		stopTableEditing();
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.setDataVector(new Object[0][numberOfColumns], columnNames());
		fingeringList.setFillsGrids(false);
		renderer = new FingeringRenderer(numberOfHoles);
		renderer.addDataChangedListener(this);
		TableColumn column = fingeringList.getColumn("Fingering");
		column.setCellRenderer(renderer);
		column.setCellEditor(new FingeringEditor());
		column.setPreferredWidth(renderer.getPreferredSize().width);
		column.setMinWidth(renderer.getMinimumSize().width);
		fingeringList.setRowHeight(renderer.getPreferredSize().height);
		fingeringList.setAutoResizeMode(JideTable.AUTO_RESIZE_FILL);
		fingeringList.setFillsRight(true);
		fingeringList.setCellSelectionEnabled(true);
		if (numRows > 0)
		{
			for (int i = 0; i < numRows; i++)
			{
				model.addRow(emptyRow());
			}
		}

		areFingeringsPopulated();
	}

	protected Fingering getRowData(DefaultTableModel model, int row)
	{
		return (Fingering) model.getValueAt(row, 0);
	}

	protected List<Fingering> getTableData()
	{
		stopTableEditing();
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		ArrayList<Fingering> data = new ArrayList<Fingering>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Fingering value = getRowData(model, i);
			if (value != null)
			{
				data.add(value);
			}
		}
		return data;
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		areFingeringsPopulated();
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

	@Override
	public void addDataChangedListener(DataChangedListener listener)
	{
		if (dataChangedListeners == null)
		{
			dataChangedListeners = new ArrayList<DataChangedListener>();
		}
		dataChangedListeners.add(listener);
	}

	@Override
	public void removeDataChangedListener(DataChangedListener listener)
	{
		if (dataChangedListeners != null)
		{
			dataChangedListeners.remove(listener);
		}
	}

	protected void fireDataStateChanged()
	{
		if (populatedListeners != null)
		{
			List<DataPopulatedEvent> events = new ArrayList<DataPopulatedEvent>();
			DataPopulatedEvent event = new DataPopulatedEvent(this,
					SAVE_EVENT_ID, namePopulated && fingeringsPopulated);
			events.add(event);
			for (DataPopulatedEvent thisEvent : events)
			{
				for (DataPopulatedListener listener : populatedListeners)
				{
					listener.dataStateChanged(thisEvent);
				}
			}
		}

		if (dataChangedListeners != null)
		{
			for (DataChangedListener listener : dataChangedListeners)
			{
				listener.dataChanged(new DataChangedEvent(this));
			}
		}
	}

	protected void configureDragAndDrop()
	{
		fingeringList.setDragEnabled(true);
		fingeringList.setTransferHandler(new TableSourceRowTransferHandler());
		nameWidget.setTransferHandler(new NoOpTransferHandler());
		descriptionWidget.setTransferHandler(new NoOpTransferHandler());
	}

	@Override
	public void dataChanged(DataChangedEvent event)
	{
		if (event.getSource() instanceof FingeringRenderer)
		{
			fireDataStateChanged();
		}
	}

}
