package com.wwidesigner.note.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.wwidesigner.gui.util.DoubleCellRenderer;
import com.wwidesigner.gui.util.TableTransferHandler;

public class ScaleIntervalPanel extends JPanel implements TableModelListener
{
	private JTable intervalTable;

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
		JLabel label = new JLabel("Scale with Intervals");
		label.setFont(getFont().deriveFont(Font.BOLD));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipady = 20;
		panel.add(label, gbc);

		DefaultTableModel model = new StringDoubleTableModel();
		model.addTableModelListener(this);
		intervalTable = new JTable(model);
		intervalTable.setCellSelectionEnabled(true);
		resetTableData();
		intervalTable.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(intervalTable);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(150, 200));
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 1;
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
				new DoubleCellRenderer(6));

	}

	class StringDoubleTableModel extends DefaultTableModel
	{
		@Override
		public void setValueAt(Object value, int row, int column)
		{
			if (column == 1)
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
			else
			{
				super.setValueAt(value, row, column);
			}
		}
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
	}

	@Override
	public void tableChanged(TableModelEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	private void configureDragAndDrop()
	{
		intervalTable.setTransferHandler(new TableTransferHandler());
	}

}
