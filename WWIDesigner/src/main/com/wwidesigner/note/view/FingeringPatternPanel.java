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

public class FingeringPatternPanel extends JPanel implements KeyListener,
		TableModelListener
{
	public static final String NEW_EVENT_ID = "newData";
	public static final String SAVE_EVENT_ID = "saveData";

	protected JTextField nameWidget;
	protected JTextPane descriptionWidget;
	protected JTextField numberOfHolesWidget;
	protected JideTable fingeringList;
	protected Integer numberOfHoles;
	protected boolean namePopulated;
	protected boolean numberOfHolesPopulated;
	protected boolean fingeringsPopulated;
	protected List<DataPopulatedListener> populatedListeners;

	public FingeringPatternPanel()
	{
		setLayout(new GridBagLayout());
		setNameWidget();
		setDescriptionWidget();
		setNumberWidget();
		setFingeringListWidget();
		configureDragAndDrop();
	}

	public FingeringPattern loadFingeringPattern(File file)
	{
		FingeringPattern fingerings = null;

		if (file != null)
		{
			BindFactory bindery = NoteBindFactory.getInstance();
			try
			{
				fingerings = (FingeringPattern) bindery
						.unmarshalXml(file, true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid FingeringPattern file.");
			}
		}

		return fingerings;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateWidgets(FingeringPattern fingerings)
	{
		if (fingerings != null)
		{
			nameWidget.setText(fingerings.getName());
			isNamePopulated();

			descriptionWidget.setText(fingerings.getComment());

			numberOfHolesWidget.setText(((Integer) fingerings
					.getNumberOfHoles()).toString());
			isNumberOfHolesPopulated();

			resetTableData(0, 0);
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

			areFingeringsPopulated();
		}
	}

	protected void isNumberOfHolesPopulated()
	{
		String number = numberOfHolesWidget.getText();

		numberOfHolesPopulated = (number != null && number.length() > 0);
		if (numberOfHolesPopulated)
		{
			Integer newNumberOfHoles = Integer.parseInt(number);
			if (newNumberOfHoles != numberOfHoles)
			{
				resetTableData(0, 0);
			}
			numberOfHoles = newNumberOfHoles;
		}
		else
		{
			if (numberOfHoles != null)
			{
				resetTableData(0, 0);
			}
			numberOfHoles = null;
		}

		fireDataStateChanged();
	}

	public int getNumberOfHoles()
	{
		return numberOfHoles;
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
		nameWidget.addKeyListener(this);
		nameWidget.setBorder(new LineBorder(Color.BLACK));
		nameWidget.setPreferredSize(new Dimension(250, 25));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(nameWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	public void setName(String name)
	{
		nameWidget.setText(name);
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
		descriptionWidget.setBorder(new LineBorder(Color.BLACK));
		descriptionWidget.setPreferredSize(new Dimension(250, 75));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(descriptionWidget, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	public void setDescription(String description)
	{
		descriptionWidget.setText(description);
	}

	public void setNumberOfHoles(Integer number)
	{
		numberOfHolesWidget.setText(number.toString());
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
		numberOfHolesWidget.addKeyListener(this);
		numberOfHolesWidget.setDocument(new IntegerDocument());
		numberOfHolesWidget.setBorder(new LineBorder(Color.BLACK));
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
		resetTableData(0, 0);
		fingeringList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(fingeringList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(250, 200));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
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
	}

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

		fireDataStateChanged();
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
		if (event.getSource().equals(numberOfHolesWidget))
		{
			isNumberOfHolesPopulated();
		}
		else if (event.getSource().equals(nameWidget))
		{
			isNamePopulated();
		}
	}

	@Override
	public void keyTyped(KeyEvent event)
	{
	}

	protected void isNamePopulated()
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

	protected void fireDataStateChanged()
	{
		if (populatedListeners == null)
		{
			return;
		}

		List<DataPopulatedEvent> events = new ArrayList<DataPopulatedEvent>();
		DataPopulatedEvent event = new DataPopulatedEvent(this, SAVE_EVENT_ID,
				namePopulated && numberOfHolesPopulated && fingeringsPopulated);
		events.add(event);
		event = new DataPopulatedEvent(this, NEW_EVENT_ID,
				numberOfHolesPopulated);
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
