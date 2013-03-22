package com.wwidesigner.optimization.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.wwidesigner.optimization.Constraint;
import com.wwidesigner.optimization.Constraints;

public class ConstraintsPanel extends JPanel
{
	private Constraints constraints;
	private GridBagConstraints gbc = new GridBagConstraints();
	private int gridy = 0;

	public ConstraintsPanel(Constraints constraints)
	{
		this.constraints = constraints;
		setLayout(new GridBagLayout());
		setMetadataValues();
		setConstraintsValues();
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
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(900, 150));
			panel.add(scrollPane, BorderLayout.CENTER);
			gbc.gridy = ++gridy;
			gbc.gridwidth = 2;
			add(panel, gbc);
		}
	}

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

	class ConstraintTableModel extends AbstractTableModel
	{
		List<Constraint> constraintValues;

		ConstraintTableModel(List<Constraint> constraintValues)
		{
			this.constraintValues = constraintValues;
		}

		@Override
		public int getColumnCount()
		{
			return 4;
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
					return constraint.getType().toString();
				case 2:
					return constraint.getLowerBound();
				case 3:
					return constraint.getUpperBound();
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

	}
}
