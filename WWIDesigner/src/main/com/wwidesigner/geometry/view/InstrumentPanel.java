/**
 * JPanel to display and edit an instrument definition.
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
package com.wwidesigner.geometry.view;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.Termination;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.gui.util.DataChangedProvider;
import com.wwidesigner.gui.util.NumberFormatTableCellRenderer;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.DoubleFormatter;

public class InstrumentPanel extends JPanel implements FocusListener,
		TableModelListener, ActionListener, DataChangedProvider
{
	public static final int HOLE_TABLE_WIDTH = 310;
	public static final int BORE_TABLE_WIDTH = 175;

	// Instrument data fields.

	protected JTextField nameField;
	protected JTextPane descriptionField;
	protected JTextField lengthTypeField;
	
	protected JPanel mouthpiecePanel;
	protected JFormattedTextField mouthpiecePosition;
	protected ButtonGroup mouthpieceTypeGroup;
	protected JRadioButton embouchureHoleButton;
	protected JRadioButton fippleButton;
	protected JRadioButton singleReedButton;
	protected JRadioButton doubleReedButton;
	protected JRadioButton lipReedButton;
	protected JFormattedTextField embHoleLength;
	protected JFormattedTextField embHoleWidth;
	protected JFormattedTextField embHoleHeight;
	protected JFormattedTextField airstreamLength;
	protected JFormattedTextField windowLength;
	protected JFormattedTextField windowWidth;
	protected JFormattedTextField windowHeight;
	protected JFormattedTextField windwayLength;
	protected JFormattedTextField windwayHeight;
	protected JFormattedTextField fippleFactor;
	protected JFormattedTextField alpha;
	protected JFormattedTextField crowFreq;
	protected JFormattedTextField beta;

	protected JFormattedTextField terminationFlange;
	protected JideTable holeList;
	protected JideTable boreList;
	
	protected int dimensionalDecimalPrecision;
	protected int dimensionlessDecimalPrecision = 5;
	protected FormatterFactory formatterFactory = new FormatterFactory();

	// State fields for this component.
	// IsPopulated flags are true when required fields contain something,
	// but do not test whether data is valid.

	protected String priorValue; // Value a field had when it gained focus.
	protected List<DataChangedListener> changeListeners;

	/**
	 * Create a panel to display and edit an instrument definition.
	 */
	public InstrumentPanel()
	{
		this.priorValue = "";
		layoutWidgets();
		setFocusTraversalKeys();
	}

	protected void layoutWidgets()
	{
		setLayout(new GridBagLayout());
		setNameWidget(0, 0, 1);
		setDescriptionWidget(0, 1, 1);
		setLengthTypeWidget(0, 2, 1);
		setMouthpieceWidget(1, 0, 3);
		setTerminationWidget(1, 2, 1);
		setHoleTableWidget(0, 3, GridBagConstraints.REMAINDER);
		setBoreTableWidget(1, 3, 1);
	}

	/**
	 * Load this panel with the instrument definition from an instrument XML
	 * file.
	 * 
	 * @param file
	 *            - contains XML for an instrument
	 * @return true if the load was successful
	 */
	public boolean loadFromFile(File file)
	{
		Instrument instrument = null;

		if (file != null)
		{
			BindFactory bindery = GeometryBindFactory.getInstance();
			try
			{
				instrument = (Instrument) bindery.unmarshalXml(file, true);
				if (instrument != null)
				{
					loadData(instrument);
					return true;
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid Instrument file.");
			}
		}

		return false;
	}

	/**
	 * Return an empty row suitable for the hole or bore table.
	 */
	protected static Object[] emptyRow(int numCols)
	{
		if (numCols == 2)
		{
			return (new Object[] { null, null });
		}
		return (new Object[] { null, null, null });
	}

	/**
	 * Load an instrument into this panel.
	 * 
	 * @param instrument
	 *            - instrument definition to load.
	 */
	public void loadData(Instrument instrument)
	{
		if (instrument != null)
		{
			dimensionalDecimalPrecision = instrument.getLengthType()
					.getDecimalPrecision();
			nameField.setText(instrument.getName());
			descriptionField.setText(instrument.getDescription());
			Mouthpiece mouthpiece = instrument.getMouthpiece();
			windowLength.setValue(null);
			windowWidth.setValue(null);
			windwayLength.setValue(null);
			windwayHeight.setValue(null);
			fippleFactor.setValue(null);
			embHoleLength.setValue(null);
			embHoleWidth.setValue(null);
			embHoleHeight.setValue(null);
			airstreamLength.setValue(null);
			alpha.setValue(null);
			crowFreq.setValue(null);
			if (mouthpiece != null)
			{
				mouthpiecePosition.setValue(mouthpiece.getPosition());
				beta.setValue(mouthpiece.getBeta());
				fippleButton.setSelected(false);
				embouchureHoleButton.setSelected(false);
				singleReedButton.setSelected(false);
				doubleReedButton.setSelected(false);
				lipReedButton.setSelected(false);
				if (mouthpiece.getEmbouchureHole() != null)
				{
					embouchureHoleButton.setSelected(true);
					embHoleLength.setValue(mouthpiece.getEmbouchureHole()
							.getLength());
					embHoleWidth.setValue(mouthpiece.getEmbouchureHole()
							.getWidth());
					embHoleHeight.setValue(mouthpiece.getEmbouchureHole()
							.getHeight());
					airstreamLength.setValue(mouthpiece.getEmbouchureHole()
							.getAirstreamLength());
					windwayHeight.setValue(mouthpiece.getEmbouchureHole()
							.getAirstreamHeight());
				}
				else if (mouthpiece.getFipple() != null)
				{
					fippleButton.setSelected(true);
					windowLength.setValue(mouthpiece.getFipple()
							.getWindowLength());
					windowWidth.setValue(mouthpiece.getFipple()
							.getWindowWidth());
					windowHeight.setValue(mouthpiece.getFipple()
							.getWindowHeight());
					windwayLength.setValue(mouthpiece.getFipple()
							.getWindwayLength());
					windwayHeight.setValue(mouthpiece.getFipple()
							.getWindwayHeight());
					fippleFactor.setValue(mouthpiece.getFipple()
							.getFippleFactor());
				}
				else if (mouthpiece.getSingleReed() != null)
				{
					singleReedButton.setSelected(true);
					alpha.setValue(mouthpiece.getSingleReed()
							.getAlpha());
				}
				else if (mouthpiece.getDoubleReed() != null)
				{
					doubleReedButton.setSelected(true);
					alpha.setValue(mouthpiece.getDoubleReed()
							.getAlpha());
					crowFreq.setValue(mouthpiece.getDoubleReed()
							.getCrowFreq());
				}
				else if (mouthpiece.getLipReed() != null)
				{
					lipReedButton.setSelected(true);
					alpha.setValue(mouthpiece.getLipReed()
							.getAlpha());
				}
				else
				{
					// Default to fipple mouthpiece.
					fippleButton.setSelected(true);
				}
			}
			else
			{
				// Default to fipple mouthpiece.
				fippleButton.setSelected(true);
				embouchureHoleButton.setSelected(false);
				singleReedButton.setSelected(false);
				doubleReedButton.setSelected(false);
				lipReedButton.setSelected(false);
			}
			enableMouthpieceFields();

			stopTableEditing(holeList);
			stopTableEditing(boreList);
			holeList.getModel().removeTableModelListener(this);
			boreList.getModel().removeTableModelListener(this);
			resetTableData(holeList, 0, 5);
			resetTableData(boreList, 0, 2);
			DefaultTableModel model = (DefaultTableModel) holeList.getModel();
			boolean firstHole = true;
			Double priorHolePosition = 0.;
			for (Hole hole : instrument.getHole())
			{
				Double spacing = null;
				double holePosition = hole.getBorePosition();
				if (!firstHole)
				{
					spacing = holePosition - priorHolePosition;
				}
				else
				{
					firstHole = false;
				}
				model.addRow(new Object[] { hole.getName(),
						hole.getBorePosition(), spacing, hole.getDiameter(),
						hole.getHeight() });
				priorHolePosition = holePosition;
			}
			model = (DefaultTableModel) boreList.getModel();
			for (BorePoint point : instrument.getBorePoint())
			{
				model.addRow(new Double[] { point.getBorePosition(),
						point.getBoreDiameter() });
			}

			holeList.getModel().addTableModelListener(this);
			boreList.getModel().addTableModelListener(this);
			if (instrument.getTermination() != null)
			{
				terminationFlange.setValue(instrument.getTermination()
						.getFlangeDiameter());
			}
			else
			{
				terminationFlange.setValue(null);
			}
			lengthTypeField.setText(instrument.getLengthType().name());
		}
	}

	static protected void stopTableEditing(JideTable table)
	{
		TableCellEditor editor = table.getCellEditor();
		if (editor != null)
		{
			editor.stopCellEditing();
		}
	}

	static protected void deleteSelectedRows(JideTable table)
	{
		stopTableEditing(table);
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	static protected void deleteUnselectedRows(JideTable table)
	{
		stopTableEditing(table);
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows.length == 0)
		{
			// If there are no selected rows, delete nothing
			// rather than deleting everything.
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int numRows = model.getRowCount();
		for (int row = numRows - 1; row >= 0; row--)
		{
			if (Arrays.binarySearch(selectedRows, row) < 0)
			{
				model.removeRow(row);
			}
		}
	}

	static protected void insertRowAboveSelection(JideTable table)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		if (model.getRowCount() <= 0)
		{
			// If table is empty, we can't select anything.
			// Insert at the top, and leave nothing selected.
			model.insertRow(0, emptyRow(model.getColumnCount()));
			return;
		}
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		model.insertRow(topIndex, emptyRow(model.getColumnCount()));

		// Re-select the original rows.
		ListSelectionModel selModel = table.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.addSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	static public void insertRowBelowSelection(JideTable table)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int bottomIndex = 0; // If table is empty, insert at the top.
		if (model.getRowCount() > 0)
		{
			int[] selectedRows = table.getSelectedRows();
			if (selectedRows.length == 0)
			{
				return;
			}
			Arrays.sort(selectedRows);
			bottomIndex = selectedRows[selectedRows.length - 1] + 1;
		}

		model.insertRow(bottomIndex, emptyRow(model.getColumnCount()));
	}

	public void saveInstrument(File file) throws Exception
	{
		Instrument instrument = getData();

		BindFactory bindery = GeometryBindFactory.getInstance();
		instrument.checkValidity();
		bindery.marshalToXml(instrument, file);
	}

	public void stopTextEditing()
	{
		Component focusedComponent = KeyboardFocusManager
				.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusedComponent != null
				& focusedComponent instanceof JFormattedTextField)
		{
			JFormattedTextField focusedField = (JFormattedTextField) focusedComponent;
			try
			{
				focusedField.commitEdit();
			}
			catch (Exception e)
			{
			}
		}
	}

	/**
	 * Build an Instrument from the data entered on the panel. Does not check
	 * the instrument for validity.
	 * 
	 * @return an Instrument, or null
	 */
	public Instrument getData()
	{
		stopTableEditing(holeList);
		stopTableEditing(boreList);
		stopTextEditing();
		Instrument instrument = new Instrument();
		instrument.setName(nameField.getText());
		instrument.setDescription(descriptionField.getText());
		// Something really strange happening in the call to the static
		// LengthType: depending on the order the Constraints, Instrument, and
		// Tuning are loaded, running an optimization generates an
		// enum-not-found exception which is irrelevant. This exception is not
		// thrown in the debugger. The implemented try/catch block "cures" the
		// problem. Without spending hours fighting the JDAF activity thread
		// code, this band-aid will have to do.
		try
		{
			String lengthTypeName = lengthTypeField.getText();
			instrument.setLengthType(LengthType.valueOf(lengthTypeName));
		}
		catch (Exception e)
		{
			instrument.setLengthType(LengthType.M);
		}
		Mouthpiece mouthpiece = getMouthpiece();
		if (mouthpiece == null)
		{
			return null;
		}
		instrument.setMouthpiece(mouthpiece);
		List<Hole> holes = getHoleTableData();
		if (holes == null)
		{
			return null;
		}
		instrument.setHole(holes);
		List<BorePoint> borePoints = getBoreTableData();
		if (borePoints == null)
		{
			return null;
		}
		instrument.setBorePoint(borePoints);
		Termination termination = getTermination();
		if (termination == null)
		{
			return null;
		}
		instrument.setTermination(termination);

		return instrument;
	}

	protected Mouthpiece getMouthpiece()
	{
		Mouthpiece mouthpiece = new Mouthpiece();
		Double value;
		value = (Double) mouthpiecePosition.getValue();
		if (value == null)
		{
			value = Double.NaN;
		}
		mouthpiece.setPosition(value);
		if (fippleButton.isSelected())
		{
			Mouthpiece.Fipple fipple = new Mouthpiece.Fipple();
			value = (Double) windowLength.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			fipple.setWindowLength(value);
			value = (Double) windowWidth.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			fipple.setWindowWidth(value);
			value = (Double) windowHeight.getValue();
			fipple.setWindowHeight(value);
			value = (Double) windwayLength.getValue();
			fipple.setWindwayLength(value);
			value = (Double) windwayHeight.getValue();
			fipple.setWindwayHeight(value);
			value = (Double) fippleFactor.getValue();
			fipple.setFippleFactor(value);
			mouthpiece.setFipple(fipple);
		}
		else if (embouchureHoleButton.isSelected())
		{
			Mouthpiece.EmbouchureHole hole = new Mouthpiece.EmbouchureHole();
			value = (Double) embHoleLength.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			hole.setLength(value);
			value = (Double) embHoleWidth.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			hole.setWidth(value);
			value = (Double) embHoleHeight.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			hole.setHeight(value);
			value = (Double) airstreamLength.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			hole.setAirstreamLength(value);
			value = (Double) windwayHeight.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			hole.setAirstreamHeight(value);
			mouthpiece.setEmbouchureHole(hole);
		}
		else if (singleReedButton.isSelected())
		{
			Mouthpiece.SingleReed reed = new Mouthpiece.SingleReed();
			value = (Double) alpha.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			reed.setAlpha(value);
			mouthpiece.setSingleReed(reed);
		}
		else if (doubleReedButton.isSelected())
		{
			Mouthpiece.DoubleReed reed = new Mouthpiece.DoubleReed();
			value = (Double) alpha.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			reed.setAlpha(value);
			value = (Double) crowFreq.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			reed.setCrowFreq(value);
			mouthpiece.setDoubleReed(reed);
		}
		else if (lipReedButton.isSelected())
		{
			Mouthpiece.LipReed reed = new Mouthpiece.LipReed();
			value = (Double) alpha.getValue();
			if (value == null)
			{
				value = Double.NaN;
			}
			reed.setAlpha(value);
			mouthpiece.setLipReed(reed);
		}
		else
		{
			// Should not occur.
			return null;
		}
		value = (Double) beta.getValue();
		mouthpiece.setBeta(value);
		return mouthpiece;
	}

	protected Termination getTermination()
	{
		Termination termination = new Termination();
		Double value;
		value = (Double) terminationFlange.getValue();
		if (value == null)
		{
			value = Double.NaN;
		}
		termination.setFlangeDiameter(value);
		return termination;
	}

	protected void setNameWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Name: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		nameField = new JTextField();
		nameField.addFocusListener(this);
		int height = (int) Math.ceil(nameField.getPreferredSize().getHeight());
		nameField.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, height));
		nameField.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH - 30, height));
		nameField.setMargin(new Insets(2, 4, 2, 4));
		nameField.setText("");
		nameField.getKeymap().removeKeyStrokeBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		nameField.getDocument().addDocumentListener(new DocumentListener()
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

				if (docLength == 0)
				{
					nameField.setBackground(Color.PINK);
				}
				else
				{
					nameField.setBackground(Color.WHITE);
				}

				fireDataChanged();
			}

		});
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(nameField, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	protected void setDescriptionWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Description: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		descriptionField = new JTextPane();
		descriptionField.addFocusListener(this);
		descriptionField.setMargin(new Insets(2, 4, 2, 4));
		descriptionField.setBorder(new LineBorder(Color.BLUE));
		descriptionField.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, 65));
		descriptionField
				.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH - 30, 20));
		descriptionField.setText("");
		descriptionField.getKeymap().removeKeyStrokeBinding(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		descriptionField.getDocument().addDocumentListener(
				new TextFieldChangeListener());
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(descriptionField, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	protected void setLengthTypeWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Dimensions are in: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		lengthTypeField = new JTextField();
		lengthTypeField.setEnabled(false);
		int height = (int) Math.ceil(lengthTypeField.getPreferredSize()
				.getHeight());
		lengthTypeField.setPreferredSize(new Dimension(30, height));
		lengthTypeField.setMinimumSize(new Dimension(30, height));
		lengthTypeField.setMargin(new Insets(2, 4, 2, 4));
		lengthTypeField.setText("");
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(lengthTypeField, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	class FormatterFactory extends AbstractFormatterFactory
	{
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField tf)
		{
			if (tf.equals(mouthpiecePosition) || tf.equals(windowLength)
					|| tf.equals(embHoleWidth) || tf.equals(windowWidth)
					|| tf.equals(embHoleLength) || tf.equals(embHoleHeight)
					|| tf.equals(airstreamLength))
			{
				DoubleFormatter requiredDouble = new DoubleFormatter(false);
				requiredDouble.setDecimalPrecision(dimensionalDecimalPrecision);
				return requiredDouble;
			}
			if (tf.equals(windowHeight) || tf.equals(windwayLength)
					|| tf.equals(windwayHeight) || tf.equals(terminationFlange))
			{
				DoubleFormatter optionalDouble = new DoubleFormatter(true);
				optionalDouble.setDecimalPrecision(dimensionalDecimalPrecision);
				return optionalDouble;
			}
			if (tf.equals(alpha) || tf.equals(crowFreq))
			{
				DoubleFormatter requiredDouble = new DoubleFormatter(false);
				requiredDouble.setDecimalPrecision(dimensionlessDecimalPrecision);
				return requiredDouble;
			}
			if (tf.equals(beta) || tf.equals(fippleFactor))
			{
				DoubleFormatter optionalDouble = new DoubleFormatter(true);
				optionalDouble.setDecimalPrecision(dimensionlessDecimalPrecision);
				return optionalDouble;
			}
			return new DefaultFormatter();
		}
	}

	protected void setMouthpieceWidget(int gridx, int gridy, int gridheight)
	{
		createMouthpieceComponents();
		GridBagConstraints gbc = new GridBagConstraints();
		mouthpiecePanel = new JPanel();
		mouthpiecePanel.setLayout(new GridBagLayout());
		layoutMouthpieceComponents();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(mouthpiecePanel, gbc);
	}

	protected JFormattedTextField setTextField(int numColumns,
			Object initialValue)
	{
		JFormattedTextField field = new JFormattedTextField(formatterFactory);
		field.setColumns(numColumns);
		field.setValue(initialValue);
		field.getDocument().addDocumentListener(new TextFieldChangeListener());

		return field;
	}

	protected void createMouthpieceComponents()
	{
		mouthpiecePanel = null;		// Created in layoutMouthpieceComponents().

		mouthpiecePosition = setTextField(5, 0.0);

		beta = setTextField(5, null);

		fippleButton = new JRadioButton("Fipple Mouthpiece");
		embouchureHoleButton = new JRadioButton("Embouchure Hole");
		singleReedButton = new JRadioButton("Single Reed");
		doubleReedButton = new JRadioButton("Double Reed");
		lipReedButton = new JRadioButton("Brass");
		fippleButton.setSelected(true);
		mouthpieceTypeGroup = new ButtonGroup();
		mouthpieceTypeGroup.add(fippleButton);
		mouthpieceTypeGroup.add(embouchureHoleButton);
		mouthpieceTypeGroup.add(singleReedButton);
		mouthpieceTypeGroup.add(doubleReedButton);
		mouthpieceTypeGroup.add(lipReedButton);
		fippleButton.addActionListener(this);
		embouchureHoleButton.addActionListener(this);
		singleReedButton.addActionListener(this);
		doubleReedButton.addActionListener(this);
		lipReedButton.addActionListener(this);

		windowLength = setTextField(5, null);
		windowWidth = setTextField(5, null);
		windowHeight = setTextField(5, null);
		embHoleLength = setTextField(5, null);
		embHoleWidth = setTextField(5, null);
		embHoleHeight = setTextField(5, null);
		airstreamLength = setTextField(5, null);
		windwayLength = setTextField(5, null);
		windwayHeight = setTextField(5, null);
		fippleFactor = setTextField(5, null);
		alpha = setTextField(5, null);
		crowFreq = setTextField(5, null);

		embHoleLength.setEnabled(false);
		embHoleWidth.setEnabled(false);
		embHoleHeight.setEnabled(false);
		airstreamLength.setEnabled(false);
		alpha.setEnabled(false);
		crowFreq.setEnabled(false);

		mouthpiecePosition.addFocusListener(this);
		embouchureHoleButton.addFocusListener(this);
		fippleButton.addFocusListener(this);
		singleReedButton.addFocusListener(this);
		doubleReedButton.addFocusListener(this);
		lipReedButton.addFocusListener(this);
		embHoleLength.addFocusListener(this);
		embHoleWidth.addFocusListener(this);
		embHoleHeight.addFocusListener(this);
		airstreamLength.addFocusListener(this);
		windowLength.addFocusListener(this);
		windowWidth.addFocusListener(this);
		windowHeight.addFocusListener(this);
		windwayLength.addFocusListener(this);
		windwayHeight.addFocusListener(this);
		fippleFactor.addFocusListener(this);
		beta.addFocusListener(this);
		alpha.addFocusListener(this);
		crowFreq.addFocusListener(this);
	}

	protected void layoutMouthpieceComponents()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label;
		mouthpiecePanel.removeAll();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;

		label = new JLabel("Mouthpiece Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		mouthpiecePanel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(10, 0, 0, 0);

		label = new JLabel("Beta Factor: ");
		gbc.gridx = 2;
		mouthpiecePanel.add(label, gbc);
		gbc.insets = new Insets(10, 0, 0, 10);
		gbc.gridx = 3;
		mouthpiecePanel.add(beta, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		mouthpiecePanel.add(fippleButton, gbc);
		gbc.gridx = 2;
		mouthpiecePanel.add(embouchureHoleButton, gbc);
		gbc.gridx = 4;
		mouthpiecePanel.add(singleReedButton, gbc);

		gbc.gridwidth = 1;
		++gbc.gridy;
		label = new JLabel("Window Length: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windowLength, gbc);

		label = new JLabel("Emb Hole Length: ");
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 3;
		mouthpiecePanel.add(embHoleLength, gbc);

		gbc.gridx = 4;
		gbc.gridwidth = 2;
		mouthpiecePanel.add(doubleReedButton, gbc);
		gbc.gridwidth = 1;

		++gbc.gridy;
		label = new JLabel("Window Width: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windowWidth, gbc);

		label = new JLabel("Emb Hole Width: ");
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 3;
		mouthpiecePanel.add(embHoleWidth, gbc);

		gbc.gridx = 4;
		gbc.gridwidth = 2;
		mouthpiecePanel.add(lipReedButton, gbc);
		gbc.gridwidth = 1;

		++gbc.gridy;
		label = new JLabel("Window Height: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windowHeight, gbc);

		label = new JLabel("Emb Hole Height: ");
		gbc.gridx = 2;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 3;
		mouthpiecePanel.add(embHoleHeight, gbc);

		label = new JLabel("Alpha (unused): ");
		gbc.gridx = 4;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 5;
		mouthpiecePanel.add(alpha, gbc);

		++gbc.gridy;
		label = new JLabel("Windway Length: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windwayLength, gbc);

		label = new JLabel("Airstream Length: ");
		gbc.gridx = 2;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 3;
		mouthpiecePanel.add(airstreamLength, gbc);

		label = new JLabel("Crow Freq: ");
		gbc.gridx = 4;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 5;
		mouthpiecePanel.add(crowFreq, gbc);

		++gbc.gridy;
		label = new JLabel("Windway Height: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windwayHeight, gbc);

		++gbc.gridy;
		label = new JLabel("Fipple Factor: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(fippleFactor, gbc);
	}

	protected void setTerminationWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel label = new JLabel("Termination Flange Diameter: ");
		panel.add(label);

		terminationFlange = setTextField(5, null);
		terminationFlange.addFocusListener(this);
		panel.add(terminationFlange);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(10, 0, 10, 10);
		add(panel, gbc);
	}

	class NumberFormatCellRenderer extends NumberFormatTableCellRenderer
	{
		@Override
		public int getDecimalPrecision(JTable table, int row, int col)
		{
			return dimensionalDecimalPrecision;
		}
	}

	protected void setHoleTableWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Holes: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new DefaultTableModel()
		{
			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				if (columnIndex == 0)
				{
					return String.class;
				}
				else
				{
					return Double.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column)
			{
				if (column == 2)
				{
					return false;
				}
				else
				{
					return true;
				}
			}
		};
		holeList = new JideTable(model);
		resetTableData(holeList, 0, 5);
		holeList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(holeList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, 160));
		scrollPane.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH, 120));
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));
		JButton button;

		button = new JButton("Add row above selection");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowAboveSelection(holeList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Add row below selection");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowBelowSelection(holeList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Delete selected rows");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				deleteSelectedRows(holeList);
			}

		});
		buttonPanel.add(button);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(buttonPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(panel, gbc);
		model.addTableModelListener(this);
	}

	protected void setBoreTableWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Bore Points: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new NumericTableModel(Double.class,
				Double.class);
		boreList = new JideTable(model);
		resetTableData(boreList, 2, 2);
		boreList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(boreList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(BORE_TABLE_WIDTH, 160));
		scrollPane.setMinimumSize(new Dimension(BORE_TABLE_WIDTH, 120));
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		JPanel buttonPanel = createBoreButtons();

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(buttonPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(panel, gbc);
		model.addTableModelListener(this);
	}

	protected JPanel createBoreButtons()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));
		JButton button;

		button = new JButton("Add row above selection");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowAboveSelection(boreList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Add row below selection");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowBelowSelection(boreList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Delete selected rows");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				deleteSelectedRows(boreList);
			}

		});
		buttonPanel.add(button);
		return buttonPanel;
	}

	public void setName(String name)
	{
		String oldValue = nameField.getText();
		nameField.setText(name);
		if (!oldValue.equals(name))
		{
			fireDataChanged();
		}
	}

	public void setDescription(String description)
	{
		String oldValue = descriptionField.getText();
		descriptionField.setText(description);
		if (!oldValue.equals(description))
		{
			fireDataChanged();
		}
	}

	protected void enableMouthpieceFields()
	{
		windowLength.setEnabled(fippleButton.isSelected());
		windowWidth.setEnabled(fippleButton.isSelected());
		windowHeight.setEnabled(fippleButton.isSelected());
		windwayLength.setEnabled(fippleButton.isSelected());
		windwayHeight.setEnabled(fippleButton.isSelected() || embouchureHoleButton.isSelected());
		fippleFactor.setEnabled(fippleButton.isSelected());
		embHoleLength.setEnabled(embouchureHoleButton.isSelected());
		embHoleWidth.setEnabled(embouchureHoleButton.isSelected());
		embHoleHeight.setEnabled(embouchureHoleButton.isSelected());
		airstreamLength.setEnabled(embouchureHoleButton.isSelected());
		alpha.setEnabled(singleReedButton.isSelected() 
				|| doubleReedButton.isSelected() 
				|| lipReedButton.isSelected());
		crowFreq.setEnabled(doubleReedButton.isSelected());
	}

	protected void resetTableData(JideTable table, int numRows, int numCols)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		TableCellRenderer renderer = new NumberFormatCellRenderer();
		int firstDoubleCol;
		int lastDoubleCol;
		if (numCols == 2)
		{
			model.setDataVector(new Object[0][2], new String[] { "Position",
					"Diameter" });
			firstDoubleCol = 0;
			lastDoubleCol = 2;
		}
		else
		{
			model.setDataVector(new Object[0][4], new String[] { "Name",
					"Position", "Spacing", "Diameter", "Height" });
			firstDoubleCol = 1;
			lastDoubleCol = 5;
		}
		for (int i = firstDoubleCol; i < lastDoubleCol; i++)
		{
			TableColumn col = table.getColumn(model.getColumnName(i));
			col.setCellRenderer(renderer);
		}
		table.setFillsGrids(false);
		table.setAutoResizeMode(JideTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setFillsRight(true);
		table.setCellSelectionEnabled(true);
		if (numRows > 0)
		{
			for (int i = 0; i < numRows; i++)
			{
				model.addRow(emptyRow(model.getColumnCount()));
			}
		}
	}

	protected List<Hole> getHoleTableData()
	{
		stopTableEditing(holeList);
		DefaultTableModel model = (DefaultTableModel) holeList.getModel();
		ArrayList<Hole> data = new ArrayList<Hole>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String holeName = (String) model.getValueAt(i, 0);
			Double position = (Double) model.getValueAt(i, 1);
			Double diameter = (Double) model.getValueAt(i, 3);
			Double height = (Double) model.getValueAt(i, 4);
			if (position == null)
			{
				position = Double.NaN;
			}
			if (diameter == null)
			{
				diameter = Double.NaN;
			}
			if (height == null)
			{
				height = Double.NaN;
			}
			Hole hole = new Hole(position, diameter, height);
			hole.setName(holeName);
			data.add(hole);
		}
		return data;
	}

	protected List<BorePoint> getBoreTableData()
	{
		stopTableEditing(boreList);
		DefaultTableModel model = (DefaultTableModel) boreList.getModel();
		ArrayList<BorePoint> data = new ArrayList<BorePoint>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Double position = (Double) model.getValueAt(i, 0);
			Double diameter = (Double) model.getValueAt(i, 1);
			if (position == null)
			{
				position = Double.NaN;
			}
			if (diameter == null)
			{
				diameter = Double.NaN;
			}
			data.add(new BorePoint(position, diameter));
		}
		return data;
	}

	@Override
	public void focusGained(FocusEvent event)
	{
		if (event.getSource() instanceof JTextComponent)
		{
			JTextComponent field = (JTextComponent) event.getSource();
			priorValue = new String(field.getText());
		}
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		boolean isDataChanged = false;

		if (event.getSource() instanceof JTextComponent)
		{
			JTextComponent field = (JTextComponent) event.getSource();
			if (!priorValue.equals(field.getText()))
			{
				isDataChanged = true;
			}
		}
		if (isDataChanged)
		{
			fireDataChanged();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource().equals(fippleButton)
			|| event.getSource().equals(embouchureHoleButton)
			|| event.getSource().equals(singleReedButton)
			|| event.getSource().equals(doubleReedButton)
			|| event.getSource().equals(lipReedButton))
		{
			enableMouthpieceFields();
		}
		fireDataChanged();
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		updateHoleSpacing(event.getSource());
		fireDataChanged();
	}

	private void updateHoleSpacing(Object source)
	{
		if (source instanceof DefaultTableModel)
		{
			DefaultTableModel model = (DefaultTableModel) source;
			if (model.getColumnCount() == 5)
			{
				model.removeTableModelListener(this);
				boolean firstHole = true;
				Double priorHolePosition = 0.;
				int rowCount = model.getRowCount();
				for (int row = 0; row < rowCount; row++)
				{
					Double spacing = null;
					Double holePosition = (Double) model.getValueAt(row, 1);
					// Allow for a newly created row without a hole position.
					if (holePosition == null)
					{
						continue;
					}
					if (!firstHole)
					{
						spacing = holePosition - priorHolePosition;
						model.setValueAt(spacing, row, 2);
					}
					else
					{
						firstHole = false;
					}
					priorHolePosition = holePosition;
				}
				model.addTableModelListener(this);
			}
		}
	}

	@Override
	public void addDataChangedListener(DataChangedListener listener)
	{
		if (changeListeners == null)
		{
			changeListeners = new ArrayList<DataChangedListener>();
		}
		changeListeners.add(listener);
	}

	@Override
	public void removeDataChangedListener(DataChangedListener listener)
	{
		if (changeListeners != null)
		{
			changeListeners.remove(listener);
		}
	}

	protected void fireDataChanged()
	{
		if (changeListeners == null)
		{
			return;
		}

		for (DataChangedListener listener : changeListeners)
		{
			listener.dataChanged(new DataChangedEvent(this));
		}
	}

	protected void setFocusTraversalKeys()
	{
		KeyboardFocusManager kfManager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>();
		forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
		forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
				InputEvent.CTRL_MASK));
		// Do not include Enter key: conflicts with default button action
		forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.CTRL_MASK));
		forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0));
		forwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN,
				InputEvent.CTRL_MASK));
		kfManager.setDefaultFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
		Set<AWTKeyStroke> backKeys = new HashSet<AWTKeyStroke>();
		backKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
				InputEvent.SHIFT_MASK));
		backKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		backKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.SHIFT_MASK));
		backKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_ENTER,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		backKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0));
		backKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP,
				InputEvent.CTRL_MASK));
		kfManager.setDefaultFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backKeys);
	}

	class TextFieldChangeListener implements DocumentListener
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
			fireDataChanged();
		}

	}
}
