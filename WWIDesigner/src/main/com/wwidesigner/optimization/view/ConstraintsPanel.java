package com.wwidesigner.optimization.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Document;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.gui.util.DataChangedProvider;
import com.wwidesigner.gui.util.NumberFormatTableCellRenderer;
import com.wwidesigner.optimization.Constraint;
import com.wwidesigner.optimization.ConstraintType;
import com.wwidesigner.optimization.Constraints;

public class ConstraintsPanel extends JPanel implements DataChangedProvider
{
	Constraints constraints;
	private GridBagConstraints gbc = new GridBagConstraints();
	private int gridy = 0;
	int dimensionalDecimalPrecision;
	int dimensionlessDecimalPrecision = 4;
	private List<DataChangedListener> dataChangedListeners;
	private Dimension panelDimension = new Dimension(780, 150);
	private int[] columnWidth = new int[] { 500, 110, 85, 85 };
	private JTable[] constraintTables;

	public ConstraintsPanel(Constraints aConstraints)
	{
		setConstraintValues(aConstraints);
	}

	public ConstraintsPanel()
	{

	}

	public void setConstraintValues(Constraints aConstraints)
	{
		this.constraints = aConstraints;
		dimensionalDecimalPrecision = aConstraints.getDimensionType()
				.getDecimalPrecision();
		setLayout(new GridBagLayout());
		setMetadataValues();
		setConstraintsValues();
	}

	public Constraints getConstraintValues()
	{
		stopEditing();
		return constraints;
	}

	protected void stopEditing()
	{
		if (constraintTables != null)
		{
			for (JTable table : constraintTables)
			{
				if (table != null)
				{
					TableCellEditor editor = table.getCellEditor();
					if (editor != null)
					{
						editor.stopCellEditing();
					}
				}
			}
		}
	}

	public void setPanelDimension(int width, int height)
	{
		panelDimension.setSize(width, height);
	}

	public void setColumnWidths(int[] widths)
	{
		int numColumns = Math.min(widths.length, columnWidth.length);
		for (int i = 0; i < numColumns; i++)
		{
			columnWidth[i] = widths[i];
		}
	}

	private void setConstraintsValues()
	{
		Set<String> categories = constraints.getCategories();
		gbc.gridx = 0;
		constraintTables = new JTable[categories.size()];
		int catIndex = 0;
		for (String category : categories)
		{
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			JLabel label = new JLabel(category);
			panel.add(label, BorderLayout.NORTH);

			List<Constraint> constraintValues = constraints
					.getConstraints(category);
			JTable table = new JideTable(
					new ConstraintTableModel(constraintValues));
			configureTable(table);
			constraintTables[catIndex++] = table;
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(panelDimension);
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
		// Remove ENTER_KEY action, handled by document listener
		constraintsNameField.getKeymap().removeKeyStrokeBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
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
		nameCol.setPreferredWidth(columnWidth[0]);
		TableColumn typeCol = table.getColumn(tableModel.getColumnName(1));
		typeCol.setPreferredWidth(columnWidth[1]);
		TableColumn lbCol = table.getColumn(tableModel.getColumnName(2));
		lbCol.setPreferredWidth(columnWidth[2]);
		TableColumn ubCol = table.getColumn(tableModel.getColumnName(3));
		ubCol.setPreferredWidth(columnWidth[3]);

		// Set number format
		TableCellRenderer renderer = new NumberFormatCellRenderer();
		lbCol.setCellRenderer(renderer);
		ubCol.setCellRenderer(renderer);

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

		ConstraintTableModel(List<Constraint> aConstraintValues)
		{
			this.constraintValues = aConstraintValues;
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

	class NumberFormatCellRenderer extends NumberFormatTableCellRenderer
	{
		@Override
		public int getDecimalPrecision(JTable table, int row, int col)
		{
			ConstraintTableModel model = (ConstraintTableModel) table
					.getModel();
			Object dimensionType = model.getValueAt(row, 1);
			if (ConstraintType.DIMENSIONLESS.toString().equals(dimensionType))
			{
				return dimensionlessDecimalPrecision;
			}

			return dimensionalDecimalPrecision;
		}
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

	void fireDataChangedEvent()
	{
		DataChangedEvent event = new DataChangedEvent(this);
		for (DataChangedListener listener : dataChangedListeners)
		{
			listener.dataChanged(event);
		}
	}
}
