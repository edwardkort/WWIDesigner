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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.DoubleFormatter;
import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.Termination;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.util.BindFactory;

public class InstrumentPanel extends JPanel implements FocusListener,
		TableModelListener, ActionListener
{
	public static final String NEW_EVENT_ID = "newData";
	public static final String SAVE_EVENT_ID = "saveData";
	public static final int HOLE_TABLE_WIDTH = 230;
	public static final int BORE_TABLE_WIDTH = 160;

	// Instrument data fields.

	protected JTextField nameField;
	protected JTextPane descriptionField;
	protected ButtonGroup lengthTypeGroup;
	protected JRadioButton inLengthButton;
	protected JRadioButton mmLengthButton;
	protected JFormattedTextField mouthpiecePosition;
	protected ButtonGroup mouthpieceTypeGroup;
	protected JRadioButton embouchureHoleButton;
	protected JRadioButton fippleButton;
	protected JFormattedTextField innerDiameter;
	protected JFormattedTextField outerDiameter;
	protected JFormattedTextField embHoleHeight;
	protected JFormattedTextField windowLength;
	protected JFormattedTextField windowWidth;
	protected JFormattedTextField windowHeight;
	protected JFormattedTextField windwayLength;
	protected JFormattedTextField windwayHeight;
	protected JFormattedTextField fippleFactor;
	protected JFormattedTextField beta;
    protected JFormattedTextField terminationFlange;
	protected JideTable holeList;
	protected JideTable boreList;

	// State fields for this component.
	// IsPopulated flags are true when required fields contain something,
	// but do not test whether data is valid.

	protected String priorValue;	// Value a field had when it gained focus.
	protected boolean nameIsPopulated;
	protected boolean mouthpieceIsPopulated;
	protected boolean holesArePopulated;
	protected boolean boreIsPopulated;
	protected boolean terminationIsPopulated;
	protected List<DataPopulatedListener> populatedListeners;

	/**
	 * Create a panel to display and edit an instrument definition.
	 */
	public InstrumentPanel()
	{
		this.nameIsPopulated = false;
		this.holesArePopulated = true;
		this.boreIsPopulated = false;
		this.mouthpieceIsPopulated = false;
		this.terminationIsPopulated = false;
		this.priorValue = "";
		setLayout(new GridBagLayout());
		setNameWidget(0,0,1);
		setDescriptionWidget(0,1,1);
		setLengthTypeWidget(0,2,1);
		setMouthpieceWidget(1,0,3);
		setTerminationWidget(1,3,1);
		setHoleTableWidget(0,3,GridBagConstraints.REMAINDER);
		setBoreTableWidget(1,4,1);
	}

	/**
	 * Load this panel with the instrument definition from an instrument XML file.
	 * @param file - contains XML for an instrument
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
					loadData(instrument, false);
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
			return (new Object[] {null, null});
		}
		return (new Object[] {null, null, null});
	}

	/**
	 * Load an instrument into this panel.
	 * @param instrument - instrument definition to load.
	 * @param suppressChangeEvent - if true, don't fire the DataPopulated event.
	 */
	public void loadData(Instrument instrument, boolean suppressChangeEvent)
	{
		if (instrument != null)
		{
			nameField.setText(instrument.getName());
			descriptionField.setText(instrument.getDescription());
			Mouthpiece mouthpiece = instrument.getMouthpiece();
			windowLength.setValue(null);
			windowWidth.setValue(null);
			windwayLength.setValue(null);
			windwayHeight.setValue(null);
			fippleFactor.setValue(null);
			outerDiameter.setValue(null);
			innerDiameter.setValue(null);
			if (mouthpiece != null)
			{
				mouthpiecePosition.setValue(mouthpiece.getPosition());
				beta.setValue(mouthpiece.getBeta());
				if (mouthpiece.getEmbouchureHole() != null)
				{
					fippleButton.setSelected(false);
					embouchureHoleButton.setSelected(true);
					outerDiameter.setValue(mouthpiece.getEmbouchureHole().getOuterDiameter());
					innerDiameter.setValue(mouthpiece.getEmbouchureHole().getInnerDiameter());
					embHoleHeight.setValue(mouthpiece.getEmbouchureHole().getHeight());
				}
				else
				{
					fippleButton.setSelected(true);
					embouchureHoleButton.setSelected(false);
					windowLength.setValue(mouthpiece.getFipple().getWindowLength());
					windowWidth.setValue(mouthpiece.getFipple().getWindowWidth());
					windowHeight.setValue(mouthpiece.getFipple().getWindowHeight());
					windwayLength.setValue(mouthpiece.getFipple().getWindwayLength());
					windwayHeight.setValue(mouthpiece.getFipple().getWindwayHeight());
					fippleFactor.setValue(mouthpiece.getFipple().getFippleFactor());
				}
			}
			else
			{
				// Default to fipple mouthpiece.
				fippleButton.setSelected(true);
				embouchureHoleButton.setSelected(false);
			}
			enableMouthpieceFields();

			stopTableEditing(holeList);
			stopTableEditing(boreList);
			holeList.getModel().removeTableModelListener(this);
			boreList.getModel().removeTableModelListener(this);
			resetTableData(holeList, 0, 3);
			resetTableData(boreList, 0, 2);
			DefaultTableModel model = (DefaultTableModel) holeList.getModel();
			for (Hole hole : instrument.getHole())
			{
				model.addRow( new Double[] { hole.getBorePosition(),
						hole.getDiameter(), hole.getHeight() } );
			}
			model = (DefaultTableModel) boreList.getModel();
			for (BorePoint point : instrument.getBorePoint())
			{
				model.addRow( new Double[] { point.getBorePosition(),
						point.getBoreDiameter() } );
			}

			holeList.getModel().addTableModelListener(this);
			boreList.getModel().addTableModelListener(this);
			if (instrument.getTermination() != null)
			{
				terminationFlange.setValue(instrument.getTermination().getFlangeDiameter());
			}
			else
			{
				terminationFlange.setValue(null);
			}
			isNamePopulated();
			isMouthpiecePopulated();
			holesArePopulated = isTablePopulated(holeList, 0);
			boreIsPopulated = isTablePopulated(boreList, 2);
			isTerminationPopulated();
			if (! suppressChangeEvent)
			{
				fireDataStateChanged();
			}
		}
	}

	/**
	 * Test whether there is a name in the name field,
	 * and set nameIsPopulated accordingly.
	 */
	protected void isNamePopulated()
	{
		String newName = nameField.getText();
		nameIsPopulated = (newName != null && newName.trim().length() > 0);
	}
	
	/**
	 * Test whether the required mouthpiece fields are populated,
	 * and set mouthpieceIsPopulated accordingly.
	 */
	protected void isMouthpiecePopulated()
	{
		mouthpieceIsPopulated = false;
		if (mouthpiecePosition.getValue() == null)
		{
			// Not populated.
			return;
		}
		if (fippleButton.isSelected())
		{
			if (windowLength.getValue() != null
				&& ((Double) windowLength.getValue()) > 0.0
				&& windowWidth.getValue() != null
				&& ((Double) windowWidth.getValue()) > 0.0)
			{
				mouthpieceIsPopulated = true;
			}
		}
		else if (embouchureHoleButton.isSelected())
		{
			if (outerDiameter.isEditValid()
				&& innerDiameter.isEditValid()
				&& embHoleHeight.isEditValid())
			{
				mouthpieceIsPopulated = true;
			}
		}
	}
	
	/**
	 * Test whether the required termination field is populated,
	 * and set terminationIsPopulated accordingly.
	 */
	protected void isTerminationPopulated()
	{
		terminationIsPopulated = (terminationFlange.getValue() != null);
	}

	/**
	 * Test whether all entries in the hole or bore table contain valid data,
	 * and the table contains the minimum number of rows.
	 */
	static protected boolean isTablePopulated(JideTable table, int minimumRows)
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		if (model == null || model.getRowCount() < minimumRows)
		{
			return false;
		}

		for (int i = 0; i < model.getRowCount(); i++)
		{
			for (int j = 0; j < model.getColumnCount(); j++)
			{
				if (model.getValueAt(i, j) == null)
				{
					return false;
				}
			}
		}
		return true;
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
			model.insertRow(0, emptyRow(model.getColumnCount()) );
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
		int bottomIndex = 0;		// If table is empty, insert at the top.
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

	public void saveInstrument(File file)
	{
		Instrument instrument = getData();

		BindFactory bindery = GeometryBindFactory.getInstance();
		try
		{
			bindery.marshalToXml(instrument, file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
		}
	}

	public Instrument getData()
	{
		stopTableEditing(holeList);
		stopTableEditing(boreList);
		if (! nameIsPopulated)
		{
			System.out.println("Name field is required.");
			return null;
		}
		Instrument instrument = new Instrument();
		instrument.setName(nameField.getText());
		instrument.setDescription(descriptionField.getText());
		if (mmLengthButton.isSelected())
		{
			instrument.setLengthType(LengthType.MM);
		}
		else
		{
			instrument.setLengthType(LengthType.IN);
		}
		Mouthpiece mouthpiece = getMouthpiece();
		if (mouthpiece == null)
		{
			return null;
		}
		instrument.setMouthpiece(mouthpiece);
		List <Hole> holes = getHoleTableData();
		if (holes == null)
		{
			return null;
		}
		instrument.setHole(holes);
		List <BorePoint> borePoints = getBoreTableData();
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
			System.out.println("Mouthpiece position is required.");
			return null;
		}
		mouthpiece.setPosition(value);
		if (fippleButton.isSelected())
		{
			Mouthpiece.Fipple fipple = new Mouthpiece.Fipple();
			value = (Double) windowLength.getValue();
			if (value == null || value <= 0.0)
			{
				System.out.println("Window length must be positive.");
				return null;
			}
			fipple.setWindowLength(value);
			value = (Double) windowWidth.getValue();
			if (value == null || value <= 0.0)
			{
				System.out.println("Window width must be positive.");
				return null;
			}
			fipple.setWindowWidth(value);
			value = (Double) windowHeight.getValue();
			if (value != null && value <= 0.0)
			{
				System.out.println("Window height, if specified, must be positive.");
				return null;
			}
			fipple.setWindowHeight(value);
			value = (Double) windwayLength.getValue();
			if (value != null && value <= 0.0)
			{
				System.out.println("Windway length, if specified, must be positive.");
				return null;
			}
			fipple.setWindwayLength(value);
			value = (Double) windwayHeight.getValue();
			if (value != null && value <= 0.0)
			{
				System.out.println("Windway height, if specified, must be positive.");
				return null;
			}
			fipple.setWindwayHeight(value);
			value = (Double) fippleFactor.getValue();
			if (value != null && value <= 0.0)
			{
				System.out.println("Fipple factor, if specified, must be positive.");
				return null;
			}
			fipple.setFippleFactor(value);
			mouthpiece.setFipple(fipple);
			mouthpiece.setEmbouchureHole(null);
		}
		else if (embouchureHoleButton.isSelected())
		{
			Mouthpiece.EmbouchureHole hole = new Mouthpiece.EmbouchureHole();
			value = (Double) outerDiameter.getValue();
			if (value == null || value <= 0.0)
			{
				System.out.println("Outer diameter must be positive.");
				return null;
			}
			hole.setOuterDiameter(value);
			value = (Double) innerDiameter.getValue();
			if (value == null || value <= 0.0)
			{
				System.out.println("Inner diameter must be positive.");
				return null;
			}
			hole.setInnerDiameter(value);
			value = (Double) embHoleHeight.getValue();
			if (value == null || value <= 0.0)
			{
				System.out.println("Embouchure hole height must be positive.");
				return null;
			}
			hole.setHeight(value);
			mouthpiece.setEmbouchureHole(hole);
			mouthpiece.setFipple(null);
		}
		else
		{
			// Should not occur.
			return null;
		}
		value = (Double) beta.getValue();
		if (value != null && (value <= 0.0 || value >= 1.0))
		{
			System.out.println("Beta, if specified, must be positive and less than 1.0.");
			return null;
		}
		mouthpiece.setBeta(value);
		return mouthpiece;
	}
	
	protected Termination getTermination()
	{
		Termination termination = new Termination();
		Double value;
		value = (Double) terminationFlange.getValue();
		if (value == null || value <= 0.0)
		{
			System.out.println("Termination flange diameter must be positive.");
			return null;
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
		nameField.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, 20));
		nameField.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH-30, 20));
		nameField.setMargin(new Insets(2, 4, 2, 4));
		nameField.setText("");
		nameIsPopulated = false;
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
		descriptionField.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH-30, 20));
		descriptionField.setText("");
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

	private void setLengthTypeWidget(int gridx, int gridy, int gridheight)
	{
		JPanel lengthTypePanel = new JPanel();
		lengthTypePanel.setLayout(new BoxLayout(lengthTypePanel,BoxLayout.X_AXIS));
		mmLengthButton = new JRadioButton("Length in mm");
		inLengthButton = new JRadioButton("Length in Inches");
		mmLengthButton.setSelected(true);
		lengthTypeGroup = new ButtonGroup();
		lengthTypeGroup.add(mmLengthButton);
		lengthTypeGroup.add(inLengthButton);
		lengthTypePanel.add(mmLengthButton);
		lengthTypePanel.add(inLengthButton);
		mmLengthButton.addActionListener(this);
		inLengthButton.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(lengthTypePanel, gbc);
	}

	private void setMouthpieceWidget(int gridx, int gridy, int gridheight)
	{
		DoubleFormatter requiredDouble = new DoubleFormatter(false);
		DoubleFormatter optionalDouble = new DoubleFormatter(true);
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label;
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;

		label = new JLabel("Mouthpiece Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		panel.add(label, gbc);
        mouthpiecePosition = new JFormattedTextField(requiredDouble.clone());
        mouthpiecePosition.setColumns(5);
        mouthpiecePosition.setValue(0.0);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		panel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(10, 0, 0, 0);

		label = new JLabel("Beta Factor: ");
		gbc.gridx = 2;
		panel.add(label, gbc);
        beta = new JFormattedTextField(optionalDouble.clone());
        beta.setColumns(5);
		gbc.gridx = 3;
		panel.add(beta, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		++ gbc.gridy;
		fippleButton = new JRadioButton("Fipple Mouthpiece");
		embouchureHoleButton = new JRadioButton("Embouchure Hole");
		fippleButton.setSelected(true);
		mouthpieceTypeGroup = new ButtonGroup();
		mouthpieceTypeGroup.add(fippleButton);
		mouthpieceTypeGroup.add(embouchureHoleButton);
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		panel.add(fippleButton, gbc);
		gbc.gridx = 2;
		panel.add(embouchureHoleButton, gbc);
		fippleButton.addActionListener(this);
		embouchureHoleButton.addActionListener(this);

		gbc.gridwidth = 1;
		++ gbc.gridy;
		label = new JLabel("Window Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
        windowLength = new JFormattedTextField(requiredDouble.clone());
        windowLength.setColumns(5);
		gbc.gridx = 1;
		panel.add(windowLength, gbc);

		label = new JLabel("Outer Diameter: ");
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		panel.add(label, gbc);
        outerDiameter = new JFormattedTextField(requiredDouble.clone());
        outerDiameter.setColumns(5);
		gbc.gridx = 3;
		panel.add(outerDiameter, gbc);

		++ gbc.gridy;
		label = new JLabel("Window Width: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
        windowWidth = new JFormattedTextField(requiredDouble.clone());
        windowWidth.setColumns(5);
		gbc.gridx = 1;
		panel.add(windowWidth, gbc);

		label = new JLabel("Inner Diameter: ");
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		panel.add(label, gbc);
        innerDiameter = new JFormattedTextField(requiredDouble.clone());
        innerDiameter.setColumns(5);
		gbc.gridx = 3;
		panel.add(innerDiameter, gbc);

		++ gbc.gridy;
		label = new JLabel("Window Height: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
        windowHeight = new JFormattedTextField(optionalDouble.clone());
        windowHeight.setColumns(5);
		gbc.gridx = 1;
		panel.add(windowHeight, gbc);

		label = new JLabel("Emb Hole Height: ");
		gbc.gridx = 2;
		panel.add(label, gbc);
        embHoleHeight = new JFormattedTextField(requiredDouble.clone());
        embHoleHeight.setColumns(5);
		gbc.gridx = 3;
		panel.add(embHoleHeight, gbc);

		++ gbc.gridy;
		label = new JLabel("Windway Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
        windwayLength = new JFormattedTextField(optionalDouble.clone());
        windwayLength.setColumns(5);
		gbc.gridx = 1;
		panel.add(windwayLength, gbc);

		++ gbc.gridy;
		label = new JLabel("Windway Height: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
        windwayHeight = new JFormattedTextField(optionalDouble.clone());
        windwayHeight.setColumns(5);
		gbc.gridx = 1;
		panel.add(windwayHeight, gbc);

		++ gbc.gridy;
		label = new JLabel("Fipple Factor: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
        fippleFactor = new JFormattedTextField(optionalDouble.clone());
        fippleFactor.setColumns(5);
		gbc.gridx = 1;
		panel.add(fippleFactor, gbc);

		outerDiameter.setEnabled(false);
		innerDiameter.setEnabled(false);

		mouthpiecePosition.addFocusListener(this);
		embouchureHoleButton.addFocusListener(this);
		fippleButton.addFocusListener(this);
		innerDiameter.addFocusListener(this);
		outerDiameter.addFocusListener(this);
		embHoleHeight.addFocusListener(this);
		windowLength.addFocusListener(this);
		windowWidth.addFocusListener(this);
		windowHeight.addFocusListener(this);
		windwayLength.addFocusListener(this);
		windwayHeight.addFocusListener(this);
		fippleFactor.addFocusListener(this);
		beta.addFocusListener(this);
		
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	private void setTerminationWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));

		JLabel label = new JLabel("Termination Flange Diameter: ");
		panel.add(label);

		NumberFormat floatFormat = NumberFormat.getNumberInstance();
        floatFormat.setMinimumFractionDigits(1);
        terminationFlange = new JFormattedTextField(floatFormat);
        terminationFlange.setColumns(5);
		terminationFlange.addFocusListener(this);
		panel.add(terminationFlange);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
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

		DefaultTableModel model = new NumericTableModel(Double.class,Double.class);
		holeList = new JideTable(model);
		resetTableData(holeList, 0, 3);
		holesArePopulated = true;		// No holes is acceptable.
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

		DefaultTableModel model = new NumericTableModel(Double.class,Double.class);
		boreList = new JideTable(model);
		resetTableData(boreList, 2, 2);
		boreIsPopulated = false;		// Bore points not entered.
		boreList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(boreList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(BORE_TABLE_WIDTH, 140));
		scrollPane.setMinimumSize(new Dimension(BORE_TABLE_WIDTH, 100));
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

	public void setName(String name)
	{
		String oldValue = nameField.getText();
		nameField.setText(name);
		isNamePopulated();
		if (! oldValue.equals(name))
		{
			fireDataStateChanged();
		}
	}

	public void setDescription(String description)
	{
		String oldValue = descriptionField.getText();
		descriptionField.setText(description);
		if (! oldValue.equals(description))
		{
			fireDataStateChanged();
		}
	}
	
	protected void enableMouthpieceFields()
	{
		windowLength.setEnabled(fippleButton.isSelected());
		windowWidth.setEnabled(fippleButton.isSelected());
		windowHeight.setEnabled(fippleButton.isSelected());
		windwayLength.setEnabled(fippleButton.isSelected());
		windwayHeight.setEnabled(fippleButton.isSelected());
		fippleFactor.setEnabled(fippleButton.isSelected());
		outerDiameter.setEnabled(embouchureHoleButton.isSelected());
		innerDiameter.setEnabled(embouchureHoleButton.isSelected());
		embHoleHeight.setEnabled(embouchureHoleButton.isSelected());
	}

	static protected void resetTableData(JideTable table, int numRows, int numCols)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		if (numCols == 2)
		{
			model.setDataVector(new Object[0][2],
					new String[] { "Position", "Diameter" });
		}
		else
		{
			model.setDataVector(new Object[0][3],
					new String[] { "Position", "Diameter", "Height" });
		}
		table.setFillsGrids(false);
		table.setAutoResizeMode(JideTable.AUTO_RESIZE_FILL);
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
			Double position = (Double) model.getValueAt(i, 0);
			Double diameter = (Double) model.getValueAt(i, 1);
			Double height   = (Double) model.getValueAt(i, 2);
			if (position == null)
			{
				System.out.println("Missing hole position.");
				return null;
			}
			if (diameter == null || diameter <= 0.0)
			{
				System.out.println("All hole diameters must be positive.");
				return null;
			}
			if (height == null || height <= 0.0)
			{
				System.out.println("All hole heights must be positive.");
				return null;
			}
			data.add(new Hole(position, diameter, height));
		}
		return data;
	}

	protected List<BorePoint> getBoreTableData()
	{
		stopTableEditing(boreList);
		DefaultTableModel model = (DefaultTableModel) boreList.getModel();
		ArrayList<BorePoint> data = new ArrayList<BorePoint>();
		if (model.getRowCount() < 2)
		{
			System.out.println("Must specify at least two bore points.");
			return null;
		}

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Double position = (Double) model.getValueAt(i, 0);
			Double diameter = (Double) model.getValueAt(i, 1);
			if (position == null)
			{
				System.out.println("Missing bore point position.");
				return null;
			}
			if (diameter == null || diameter <= 0.0)
			{
				System.out.println("All bore diameters must be positive.");
				return null;
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
			if(! priorValue.equals(field.getText()))
			{
				isDataChanged = true;
			}
			if (event.getSource().equals(nameField))
			{
				isNamePopulated();
			}
			else if (event.getSource().equals(terminationFlange))
			{
				isTerminationPopulated();
			}
			else
			{
				isMouthpiecePopulated();
			}
		} 
		if (isDataChanged)
		{
			fireDataStateChanged();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource().equals(fippleButton)
			|| event.getSource().equals(embouchureHoleButton))
		{
			enableMouthpieceFields();
			isMouthpiecePopulated();
		}
		fireDataStateChanged();
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		holesArePopulated = isTablePopulated(holeList, 0);
		boreIsPopulated = isTablePopulated(boreList, 2);
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
				nameIsPopulated && mouthpieceIsPopulated 
				&& holesArePopulated && boreIsPopulated && terminationIsPopulated );
		events.add(event);
		for (DataPopulatedEvent thisEvent : events)
		{
			for (DataPopulatedListener listener : populatedListeners)
			{
				listener.dataStateChanged(thisEvent);
			}
		}
	}
}
