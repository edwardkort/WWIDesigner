package com.wwidesigner.note.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DoubleCellRenderer;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.gui.util.TableTransferHandler;

public class ScaleIntervalPanel extends JPanel implements TableModelListener
{
	private JTable intervalTable;
	private List<DataPopulatedListener> populatedListeners;
	private boolean intervalsPopulated;

	public ScaleIntervalPanel()
	{
		setLayout(new GridBagLayout());
		setIntervalTableWidget();
		configureDragAndDrop();
	}

	private void setIntervalTableWidget()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		DefaultTableModel model = new NumericTableModel();
		model.addTableModelListener(this);
		intervalTable = new JideTable(model);
		intervalTable.setCellSelectionEnabled(true);
		resetTableData();
		intervalTable.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(intervalTable);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(150, 200));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(scrollPane, gbc);

		gbc.gridy = 0;
		add(panel, gbc);
	}

	private void resetTableData()
	{
		DefaultTableModel model = (DefaultTableModel) intervalTable.getModel();
		model.setDataVector(new Object[60][2], new String[] { "Symbol",
				"Interval" });
		intervalTable.getColumn("Interval").setCellRenderer(
				new DoubleCellRenderer(5));

	}

	@SuppressWarnings({ "rawtypes" })
	public void setTableData(Vector data)
	{
		DefaultTableModel model = (DefaultTableModel) intervalTable.getModel();
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("Symbol");
		columnNames.add("Interval");
		model.setDataVector(data, columnNames);
		intervalTable.getColumn("Interval").setCellRenderer(
				new DoubleCellRenderer(6));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getTableData()
	{
		DefaultTableModel model = (DefaultTableModel) intervalTable.getModel();
		Vector data = model.getDataVector();
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
				clonedRow.add(interval);
			}
			clonedData.add(clonedRow);
		}

		return clonedData;
	}

	public void clearSelection()
	{
		DefaultTableModel model = (DefaultTableModel) intervalTable.getModel();
		int rowIndexStart = intervalTable.getSelectedRow();
		int rowIndexEnd = intervalTable.getSelectionModel()
				.getMaxSelectionIndex();
		int colIndexStart = intervalTable.getSelectedColumn();
		int colIndexEnd = intervalTable.getColumnModel().getSelectionModel()
				.getMaxSelectionIndex();
		for (int i = rowIndexStart; i <= rowIndexEnd; i++)
		{
			for (int j = colIndexStart; j <= colIndexEnd; j++)
			{
				if (intervalTable.isCellSelected(i, j))
				{
					model.setValueAt(null, i, j);

				}
			}
		}
		fireDataStateChanged();
	}

	public void deleteSelection()
	{
		DefaultTableModel model = (DefaultTableModel) intervalTable.getModel();
		int rowIndexStart = intervalTable.getSelectedRow();
		int rowIndexEnd = intervalTable.getSelectionModel()
				.getMaxSelectionIndex();
		int colIndexStart = intervalTable.getSelectedColumn();
		int colIndexEnd = intervalTable.getColumnModel().getSelectionModel()
				.getMaxSelectionIndex();
		for (int i = rowIndexEnd; i >= rowIndexStart; i--)
		{
			for (int j = colIndexEnd; j >= colIndexStart; j--)
			{
				if (intervalTable.isCellSelected(i, j))
				{
					for (int k = i + 1; k < model.getRowCount(); k++)
					{
						Object lowerValue = model.getValueAt(k, j);
						model.setValueAt(lowerValue, k - 1, j);
					}
					model.setValueAt(null, model.getRowCount() - 1, j);
				}
			}
		}
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

		DataPopulatedEvent event = new DataPopulatedEvent(this,
				intervalsPopulated);
		for (DataPopulatedListener listener : populatedListeners)
		{
			listener.dataStateChanged(event);
		}
	}

	@Override
	public void tableChanged(TableModelEvent arg0)
	{
		areIntervalsPopulated();
	}

	private void areIntervalsPopulated()
	{
		DefaultTableModel model = (DefaultTableModel) intervalTable.getModel();
		intervalsPopulated = false;

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String value = (String) model.getValueAt(i, 0);
			if (value != null && value.length() > 0)
			{
				Double dblValue = (Double) model.getValueAt(i, 1);
				if (dblValue != null)
				{
					intervalsPopulated = true;
					break;
				}
			}
		}

		fireDataStateChanged();
	}

	private void configureDragAndDrop()
	{
		intervalTable.setTransferHandler(new TableTransferHandler());
	}

}
