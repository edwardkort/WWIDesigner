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
import java.util.Vector;

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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.IntegerDocument;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.TableSourceRowTransferHandler;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.FingeringPattern;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

public class FingeringPatternPanel extends JPanel implements FocusListener,
		TableModelListener
{
	public static final String NEW_EVENT_ID = "newData";
	public static final String SAVE_EVENT_ID = "saveData";
	public static final int DEFAULT_WIDTH = 220;

	protected JTextField nameWidget;
	protected JTextPane descriptionWidget;
	protected JTextField numberOfHolesWidget;
	protected JideTable fingeringList;
	protected int componentWidth;
	protected Integer numberOfHoles;
	protected String name;
	protected String description;
	protected boolean namePopulated;
	protected boolean fingeringsPopulated;
	protected List<DataPopulatedListener> populatedListeners;

	/**
	 * Create a panel with components of a specified preferred width.
	 * @param componentWidth - preferred width of display/edit components.
	 */
	public FingeringPatternPanel(int componentWidth)
	{
		this.componentWidth = componentWidth;
		this.numberOfHoles = 0;
		this.name = "New";
		this.description = "";
		this.namePopulated = true;
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

	public boolean loadFingeringPattern(File file)
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
					populateWidgets(fingerings, true);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
			TableColumn column = fingeringList.getColumn("Fingering");
			column.setPreferredWidth(new FingeringComponent(fingerings
					.getNumberOfHoles()).getPreferredSize().width);
			for (Fingering fingering : fingerings.getFingering())
			{
				Vector row = new Vector();
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
	 * Verify that there is a name in the nameWidget, and set namePopulated accordingly.
	 * @return true if the name has changed.
	 */
	protected boolean isNamePopulated()
	{
		String newName = nameWidget.getText();

		namePopulated = (newName != null && newName.trim().length() > 0);
		if (newName != null && ! newName.equals(name))
		{
			name = newName;
			return true;
		}
		return false;
	}

	/**
	 * Test whether the description in descriptionWidget has changed.
	 * @return true if the description has changed.
	 */
	protected boolean isDescriptionChanged()
	{
		String newDescription = descriptionWidget.getText();

		if (newDescription != null && ! newDescription.equals(description))
		{
			description = newDescription;
			return true;
		}
		return false;
	}

	/**
	 * Validate the data in numberOfHolesWidget.
	 * @return true if the number of holes has changed.
	 */
	protected boolean validateNumberOfHoles()
	{
		String number = numberOfHolesWidget.getText();

		if (number == null || number.trim().isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Number of holes must be a valid number.");
			numberOfHolesWidget.setText(numberOfHoles.toString());
			return false;
		}

		Integer newNumberOfHoles = Integer.parseInt(number);
		if (newNumberOfHoles < 0)
		{
			JOptionPane.showMessageDialog(this, "Number of holes must be non-negative.");
			numberOfHolesWidget.setText(numberOfHoles.toString());
			return false;
		}
		if (newNumberOfHoles.equals(numberOfHoles))
		{
			return false;
		}
		resetTableData(1, newNumberOfHoles);
		numberOfHoles = newNumberOfHoles;
		return true;
	}

	/**
	 * Test whether the fingeringList table contains valid fingerings,
	 * and set fingeringsPopulated accordingly.
	 */
	protected void areFingeringsPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		fingeringsPopulated = false;

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Fingering value = (Fingering) model.getValueAt(i, 0);
			if (value != null)
			{
				fingeringsPopulated = true;
				break;
			}
		}
	}

	public void deleteSelectedFingerings()
	{
		int[] selectedRows = fingeringList.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	public void deleteUnselectedFingerings()
	{
		int[] selectedRows = fingeringList.getSelectedRows();
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
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0 || numberOfHoles == null)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.insertRow(topIndex,
				new Fingering[] { new Fingering(numberOfHoles) });

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
		int[] selectedRows = fingeringList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int bottomIndex = selectedRows[selectedRows.length - 1];

		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.insertRow(bottomIndex + 1, new Fingering[] { new Fingering(
				numberOfHoles) });
	}

	public void saveFingeringPattern(File file)
	{
		FingeringPattern fingerings = getFingeringPattern();

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

	public FingeringPattern getFingeringPattern()
	{
		if (! namePopulated || ! fingeringsPopulated)
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
		model.addTableModelListener(this);
		fingeringList = new JideTable(model);
		resetTableData(0, numberOfHoles);
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

	public void resetTableData(int numRows, int numHoles)
	{
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		model.setDataVector(new Fingering[0][1], new String[] { "Fingering" });
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
				model.addRow(new Fingering[] { new Fingering(numHoles) });
			}
		}
		fingeringList.setRowHeight(((FingeringRenderer) renderer)
				.getPreferredSize().height);

		areFingeringsPopulated();
	}

	protected List<Fingering> getTableData()
	{
		DefaultTableModel model = (DefaultTableModel) fingeringList.getModel();
		ArrayList<Fingering> data = new ArrayList<Fingering>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Fingering value = (Fingering) model.getValueAt(i, 0);
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

	protected void fireDataStateChanged()
	{
		if (populatedListeners == null)
		{
			return;
		}

		List<DataPopulatedEvent> events = new ArrayList<DataPopulatedEvent>();
		DataPopulatedEvent event = new DataPopulatedEvent(this, SAVE_EVENT_ID,
				namePopulated && fingeringsPopulated);
		events.add(event);
		for (DataPopulatedEvent thisEvent : events)
		{
			for (DataPopulatedListener listener : populatedListeners)
			{
				listener.dataStateChanged(thisEvent);
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

}
