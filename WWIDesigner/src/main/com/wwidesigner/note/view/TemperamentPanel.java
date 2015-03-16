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
import com.wwidesigner.gui.util.TableSourceTransferHandler;
import com.wwidesigner.note.Temperament;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

public class TemperamentPanel extends JPanel implements KeyListener,
		TableModelListener
{
	private JTextField nameWidget;
	private JTextPane descriptionWidget;
	private JTable ratioList;
	private boolean namePopulated;
	private boolean ratiosPopulated;
	private List<DataPopulatedListener> populatedListeners;

	public static final String LOAD_PAGE_ID = "loadData";
	public static final String SAVE_ID = "saveData";

	public TemperamentPanel()
	{
		this.setLayout(new GridBagLayout());
		setNameWidget();
		setDescriptionWidget();
		setRatioListWidget();
		configureDragAndDrop();
	}

	public Temperament loadTemperament(File file)
	{
		Temperament temp = null;

		if (file != null)
		{
			BindFactory bindery = NoteBindFactory.getInstance();
			try
			{
				temp = (Temperament) bindery.unmarshalXml(file, true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid Temperament file.");
			}
		}

		return temp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateWidgets(Temperament temp)
	{
		if (temp != null)
		{
			nameWidget.setText(temp.getName());
			isNamePopulated();

			descriptionWidget.setText(temp.getComment());

			Vector rows = new Vector();
			for (Double ratio : temp.getRatio())
			{
				Vector row = new Vector();
				row.add(ratio);
				rows.add(row);
			}

			Vector columns = new Vector();
			columns.add("Ratio");
			DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
			model.setDataVector(rows, columns);
			ratioList.getColumn("Ratio").setCellRenderer(
					new DoubleCellRenderer(5));
			areRatiosPopulated();
		}
	}

	public void saveTemperament(File file)
	{
		Temperament temp = getTemperament();
		temp.deleteNulls();

		BindFactory bindery = NoteBindFactory.getInstance();
		try
		{
			bindery.marshalToXml(temp, file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
		}
	}

	public Temperament getTemperament()
	{
		Temperament temp = new Temperament();
		temp.setName(nameWidget.getText());
		temp.setComment(descriptionWidget.getText());
		temp.setRatio(getTableData());

		return temp;
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
		nameWidget.setPreferredSize(new Dimension(150, 25));
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
		descriptionWidget.setPreferredSize(new Dimension(150, 75));
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

	private void setRatioListWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Ratio List: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new DoubleTableModel();
		model.addTableModelListener(this);
		ratioList = new JideTable(model);
		resetTableData();
		ratioList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(ratioList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(150, 200));
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 10, 0);
		add(panel, gbc);
	}

	class DoubleTableModel extends DefaultTableModel
	{
		@Override
		public void setValueAt(Object value, int row, int column)
		{
			Double dblValue = null;

			try
			{
				if (value instanceof String)
				{
					dblValue = Double.valueOf((String) value);
				}
				else
				{
					dblValue = (Double) value;
				}
			}
			catch (Exception e)
			{
			}

			super.setValueAt(dblValue, row, column);
		}

	}

	public void resetTableData()
	{
		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		model.setDataVector(new Double[50][1], new String[] { "Ratio" });
		ratioList.getColumn("Ratio").setCellRenderer(new DoubleCellRenderer(5));
	}

	private List<Double> getTableData()
	{
		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		ArrayList<Double> data = new ArrayList<Double>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Double value = (Double) model.getValueAt(i, 0);
			data.add(value);
		}

		return data;
	}

	public void deleteSelectedRatios()
	{
		int[] selectedRows = ratioList.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	public void deleteUnselectedRatios()
	{
		int[] selectedRows = ratioList.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		int numRows = model.getRowCount();
		for (int row = numRows - 1; row >= 0; row--)
		{
			if (Arrays.binarySearch(selectedRows, row) < 0)
			{
				model.removeRow(row);
			}
		}
	}

	public void insertRatioAboveSelection()
	{
		int[] selectedRows = ratioList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		model.insertRow(topIndex, (Object[]) null);

		ListSelectionModel selModel = ratioList.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.setSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	public void insertRatioBelowSelection()
	{
		int[] selectedRows = ratioList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int bottomIndex = selectedRows[selectedRows.length - 1];

		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		model.insertRow(bottomIndex + 1, (Object[]) null);

	}

	public void addOctaveBelow()
	{
		int[] selectedRows = ratioList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		int lastRow = getLastPopulatedRow();

		for (int selectedRow : selectedRows)
		{
			Double value = (Double) model.getValueAt(selectedRow, 0);
			if (value != null)
			{
				value *= 2.;
			}
			model.insertRow(++lastRow, new Object[] { value });
		}
	}

	private int getLastPopulatedRow()
	{
		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		int row = model.getRowCount();

		for (--row; row >= 0; row++)
		{
			Object value = model.getValueAt(row, 0);
			if (value != null)
			{
				return row;
			}
		}

		return 0;
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		areRatiosPopulated();
	}

	private void areRatiosPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) ratioList.getModel();
		ratiosPopulated = false;

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Object value = model.getValueAt(i, 0);
			if (value != null)
			{
				ratiosPopulated = true;
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

		namePopulated = (name != null && name.trim().length() > 0);
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
		DataPopulatedEvent event = new DataPopulatedEvent(this, SAVE_ID,
				namePopulated && ratiosPopulated);
		events.add(event);
		event = new DataPopulatedEvent(this, LOAD_PAGE_ID, ratiosPopulated);
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
		ratioList.setDragEnabled(true);
		ratioList.setTransferHandler(new TableSourceTransferHandler());
		nameWidget.setTransferHandler(new NoOpTransferHandler());
		descriptionWidget.setTransferHandler(new NoOpTransferHandler());
	}

}
