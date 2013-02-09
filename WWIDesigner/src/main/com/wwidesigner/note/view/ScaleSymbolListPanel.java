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

import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.NoOpTransferHandler;
import com.wwidesigner.gui.util.TableSourceTransferHandler;
import com.wwidesigner.note.ScaleSymbolList;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

public class ScaleSymbolListPanel extends JPanel implements KeyListener,
		TableModelListener
{

	private JTextField nameWidget;
	private JTextPane descriptionWidget;
	private JTable symbolList;
	private boolean namePopulated;
	private boolean symbolsPopulated;
	private List<DataPopulatedListener> populatedListeners;

	public static final String LOAD_PAGE_ID = "loadData";
	public static final String SAVE_ID = "saveData";

	public ScaleSymbolListPanel()
	{
		setLayout(new GridBagLayout());
		setNameWidget();
		setDescriptionWidget();
		setSymbolListWidget();
		configureDragAndDrop();
	}

	public ScaleSymbolList loadSymbolList(File file)
	{
		ScaleSymbolList symbols = null;

		if (file != null)
		{
			BindFactory bindery = NoteBindFactory.getInstance();
			try
			{
				symbols = (ScaleSymbolList) bindery.unmarshalXml(file, true);
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid ScaleSymbolList file.");
			}
		}

		return symbols;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void populateWidgets(ScaleSymbolList symbols)
	{
		if (symbols != null)
		{
			nameWidget.setText(symbols.getName());
			isNamePopulated();

			descriptionWidget.setText(symbols.getComment());

			Vector rows = new Vector();
			for (String symbol : symbols.getScaleSymbol())
			{
				Vector row = new Vector();
				row.add(symbol);
				rows.add(row);
			}

			Vector columns = new Vector();
			columns.add("Symbol");
			DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
			model.setDataVector(rows, columns);
			areSymbolsPopulated();
		}
	}

	public void deleteSelectedSymbols()
	{
		int[] selectedRows = symbolList.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	public void deleteUnselectedSymbols()
	{
		int[] selectedRows = symbolList.getSelectedRows();
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		int numRows = model.getRowCount();
		for (int row = numRows - 1; row >= 0; row--)
		{
			if (Arrays.binarySearch(selectedRows, row) < 0)
			{
				model.removeRow(row);
			}
		}
	}

	public void insertSymbolAboveSelection()
	{
		int[] selectedRows = symbolList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		model.insertRow(topIndex, (Object[]) null);

		ListSelectionModel selModel = symbolList.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.setSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	public void insertSymbolBelowSelection()
	{
		int[] selectedRows = symbolList.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int bottomIndex = selectedRows[selectedRows.length - 1];

		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		model.insertRow(bottomIndex + 1, (Object[]) null);
	}

	public void saveSymbolList(File file)
	{
		ScaleSymbolList symbols = getScaleSymbolList();
		symbols.removeNulls();

		BindFactory bindery = NoteBindFactory.getInstance();
		try
		{
			bindery.marshalToXml(symbols, file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
		}
	}

	public ScaleSymbolList getScaleSymbolList()
	{
		ScaleSymbolList symbols = new ScaleSymbolList();
		symbols.setName(nameWidget.getText());
		symbols.setComment(descriptionWidget.getText());
		symbols.setScaleSymbol(getTableData());

		return symbols;
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

	private void setSymbolListWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Symbol List: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new DefaultTableModel();
		model.addTableModelListener(this);
		symbolList = new JTable(model);
		resetTableData();
		symbolList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(symbolList);
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

	public void resetTableData()
	{
		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		model.setDataVector(new String[50][1], new String[] { "Symbol" });
	}

	private List<String> getTableData()
	{
		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		ArrayList<String> data = new ArrayList<String>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String value = (String) model.getValueAt(i, 0);
			// if (value != null && value.length() > 0)
			// {
			data.add(value);
			// }
		}
		return data;
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		areSymbolsPopulated();
	}

	private void areSymbolsPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) symbolList.getModel();
		symbolsPopulated = false;

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String value = (String) model.getValueAt(i, 0);
			if (value != null && value.trim().length() > 0)
			{
				symbolsPopulated = true;
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
				namePopulated && symbolsPopulated);
		events.add(event);
		event = new DataPopulatedEvent(this, LOAD_PAGE_ID, symbolsPopulated);
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
		symbolList.setDragEnabled(true);
		symbolList.setTransferHandler(new TableSourceTransferHandler());
		nameWidget.setTransferHandler(new NoOpTransferHandler());
		descriptionWidget.setTransferHandler(new NoOpTransferHandler());
	}

}
