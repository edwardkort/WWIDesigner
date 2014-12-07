package com.wwidesigner.optimization.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Document;

import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.gui.util.DataChangedProvider;
import com.wwidesigner.optimization.Constraint;
import com.wwidesigner.optimization.Constraints;

public class ConstraintsPanel extends JPanel implements DataChangedProvider
{
	private Constraints constraints;
	private GridBagConstraints gbc = new GridBagConstraints();
	private int gridy = 0;
	private int decimalPrecision = 5;
	private List<DataChangedListener> dataChangedListeners;

	public ConstraintsPanel(Constraints constraints)
	{
		setConstraintValues(constraints);
	}

	public ConstraintsPanel()
	{

	}

	public void setConstraintValues(Constraints constraints)
	{
		this.constraints = constraints;
		setLayout(new GridBagLayout());
		setMetadataValues();
		setConstraintsValues();
	}

	public Constraints getConstraintValues()
	{
		return constraints;
	}

	private void setConstraintsValues()
	{
		Set<String> categories = constraints.getCategories();
		gbc.gridx = 0;
		for (String category : categories)
		{
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			JLabel label = new JLabel(category);
			panel.add(label, BorderLayout.NORTH);

			List<Constraint> constraintValues = constraints
					.getConstraints(category);
			JTable table = new JTable(
					new ConstraintTableModel(constraintValues));
			configureTable(table);
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(900, 150));
			panel.add(scrollPane, BorderLayout.CENTER);
			gbc.gridy = ++gridy;
			gbc.gridwidth = 2;
			add(panel, gbc);
		}
	}

	/**
	 * Only the Constraints name is editable, and it cannot be blank.
	 */
	private void setMetadataValues()
	{
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		JLabel label = new JLabel();
		label.setText("Optimizer name:");
		gbc.gridx = 0;
		gbc.gridy = gridy;
		add(label, gbc);
		label = new JLabel();
		label.setText(constraints.getObjectiveDisplayName());
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 1;
		add(label, gbc);

		label = new JLabel();
		label.setText("Constraints name:");
		gbc.gridx = 0;
		gbc.gridy = ++gridy;
		add(label, gbc);
		// Require a non-blank name field
		final JTextField constraintsNameField = new JTextField(50);
		constraintsNameField.setText(constraints.getConstraintsName());
		constraintsNameField.getDocument().addDocumentListener(
				new DocumentListener()
				{

					@Override
					public void insertUpdate(DocumentEvent e)
					{
						processDocumentChange(e);
					}

					@Override
					public void removeUpdate(DocumentEvent e)
					{
						processDocumentChange(e);
					}

					@Override
					public void changedUpdate(DocumentEvent e)
					{
						processDocumentChange(e);
					}

					private void processDocumentChange(DocumentEvent docEvent)
					{
						Document doc = docEvent.getDocument();
						int docLength = doc.getLength();
						String text = new String();
						try
						{
							text = doc.getText(0, docLength);
						}
						catch (Exception ex)
						{
						}

						constraints.setConstraintsName(text);
						fireDataChangedEvent();
						
						if (docLength == 0)
						{
							constraintsNameField.setBackground(Color.PINK);
						}
						else
						{
							constraintsNameField.setBackground(Color.WHITE);
						}
					}

				});

		gbc.gridx = 1;
		add(constraintsNameField, gbc);

		label = new JLabel();
		label.setText("Number of holes:");
		gbc.gridx = 0;
		gbc.gridy = ++gridy;
		add(label, gbc);
		label = new JLabel();
		label.setText(Integer.toString(constraints.getNumberOfHoles()));
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		gbc.gridx = 1;
		add(label, gbc);
	}

	private void configureTable(JTable table)
	{
		TableModel tableModel = table.getModel();

		// Set column widths
		TableColumn nameCol = table.getColumn(tableModel.getColumnName(0));
		nameCol.setPreferredWidth(425);
		TableColumn typeCol = table.getColumn(tableModel.getColumnName(1));
		typeCol.setPreferredWidth(125);
		TableColumn lbCol = table.getColumn(tableModel.getColumnName(2));
		lbCol.setPreferredWidth(175);
		TableColumn ubCol = table.getColumn(tableModel.getColumnName(3));
		ubCol.setPreferredWidth(175);

		// Set number format
		lbCol.setCellRenderer(new NumberFormatCellRenderer());
		ubCol.setCellRenderer(new NumberFormatCellRenderer());

		// Set single cell selection
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
	}

	/**
	 * TableModel in which the underlying data is List of Constraint values.
	 * 
	 * Only the upper and lower bounds are editable.
	 */
	class ConstraintTableModel extends AbstractTableModel
	{
		List<Constraint> constraintValues;

		ConstraintTableModel(List<Constraint> constraintValues)
		{
			this.constraintValues = constraintValues;
		}

		@Override
		public void setValueAt(Object value, int row, int col)
		{
			Constraint constraint = constraintValues.get(row);
			if (col == 2)
			{
				Double originalValue = constraint.getLowerBound();
				constraint.setLowerBound((Double) value);
				Double alteredValue = constraint.convertBound(true, true);
				constraint.setLowerBound(alteredValue);
				if (!originalValue.equals(alteredValue))
				{
					fireDataChangedEvent();
				}
			}
			else if (col == 3)
			{
				Double originalValue = constraint.getUpperBound();
				constraint.setUpperBound((Double) value);
				Double alteredValue = constraint.convertBound(false, true);
				constraint.setUpperBound(alteredValue);
				if (!originalValue.equals(alteredValue))
				{
					fireDataChangedEvent();
				}
			}

			fireTableCellUpdated(row, col);
		}

		@Override
		public int getRowCount()
		{
			return constraintValues.size();
		}

		@Override
		public Object getValueAt(int row, int column)
		{
			Constraint constraint = constraintValues.get(row);

			switch (column)
			{
				case 0:
					return constraint.getDisplayName();
				case 1:
					return constraint.getConstraintDimension();
				case 2:
					return constraint.convertBound(true, false);
				case 3:
					return constraint.convertBound(false, false);
			}

			return null;
		}

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0:
					return "Constraint name";
				case 1:
					return "Type";
				case 2:
					return "Lower bound";
				case 3:
					return "Upper bound";
			}

			return null;

		}

		@Override
		public boolean isCellEditable(int row, int column)
		{
			switch (column)
			{
				case 0:
					return false;
				case 1:
					return false;
				case 2:
					return true;
				case 3:
					return true;
			}

			return false;

		}

		@Override
		public Class<?> getColumnClass(int column)
		{
			switch (column)
			{
				case 0:
				case 1:
					return String.class;
				case 2:
				case 3:
					return Double.class;
			}

			return Object.class;

		}

		@Override
		public int getColumnCount()
		{
			return 4;
		}

	}

	class NumberFormatCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col)
		{
			JLabel label = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, col);
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMinimumFractionDigits(decimalPrecision);
			label.setText(value == null ? "" : format.format(value));

			return label;
		}
	}

	public void addDataChangedListener(DataChangedListener listener)
	{
		if (dataChangedListeners == null)
		{
			dataChangedListeners = new ArrayList<DataChangedListener>();
		}

		dataChangedListeners.add(listener);
	}

	private void fireDataChangedEvent()
	{
		DataChangedEvent event = new DataChangedEvent(this);
		for (DataChangedListener listener : dataChangedListeners)
		{
			listener.dataChanged(event);
		}
	}
}
