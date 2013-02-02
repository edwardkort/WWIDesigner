package com.wwidesigner.note.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.AbstractWizardPage;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;
import com.wwidesigner.gui.util.MultiLineButton;
import com.wwidesigner.gui.util.XmlFileChooser;
import com.wwidesigner.note.NoteInterval;
import com.wwidesigner.note.Scale;
import com.wwidesigner.note.view.ScaleIntervalPanel;
import com.wwidesigner.note.view.ScalePanel;

public class ScalePage extends AbstractWizardPage implements DataProvider,
		DataPopulatedProvider, DataPopulatedListener, KeyListener
{
	private JPanel contentPanel;
	private ScaleIntervalPanel scaleIntervalPanel;
	private TuningWizardDialog parent;
	private boolean isInitialized;
	private JButton updateFromIntervalsPageButton;
	@SuppressWarnings("rawtypes")
	private JComboBox noteSelector;
	private JTextField frequencyField;
	private ScalePanel scalePanel;
	private JButton createScaleButton;
	private JButton saveButton;
	private boolean isRefNoteSelected;
	private boolean isRefFrequencySet;

	public ScalePage(TuningWizardDialog parent)
	{
		super(
				"Scale with Frequencies",
				"Convert a scale with intervals to one with frequencies by setting a reference note.");
		this.parent = parent;
		createWizardContent();
	}

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		Object source = event.getSource();
		if (source instanceof ScaleIntervalPanel)
		{
			if (source.equals(scaleIntervalPanel))
			{
				ScaleIntervalPanel intervalPanel = (ScaleIntervalPanel) source;
				populateNoteSelector(intervalPanel.getTableData());
			}
			else
			{
				updateFromIntervalsPageButton.setEnabled(event.isPopulated());
			}
		}
		else if (source.equals(scalePanel))
		{
			saveButton.setEnabled(event.isPopulated());
		}

	}

	@Override
	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		scalePanel.addDataPopulatedListener(listener);
	}

	@Override
	public Object getPageData()
	{
		return scalePanel.getScale();
	}

	@Override
	public JComponent createWizardContent()
	{
		if (!isInitialized)
		{
			contentPanel = new JPanel();
			contentPanel.setLayout(new GridBagLayout());

			setScaleIntervalPanel();
			setReferencePanel();
			setScalePanel();
			isInitialized = true;
		}

		return contentPanel;
	}

	private JPanel createListButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		JButton button = new JButton("New");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				scalePanel.setName(null);
				scalePanel.setDescription(null);
				scalePanel.resetTableData();
			}
		});
		panel.add(button);

		panel.add(createLoadButton());

		panel.add(createSaveButton());

		return panel;
	}

	private Component createSaveButton()
	{
		saveButton = new JButton("Save...");
		saveButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showSaveDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					if (!file.getName().toLowerCase().endsWith(".xml"))
					{
						file = new File(file.getAbsolutePath() + ".xml");
					}
					if (file.exists())
					{
						int retVal = JOptionPane.showConfirmDialog(getParent(),
								"File " + file.getName()
										+ " already exists.\n\nOverwrite?",
								"File already exists",
								JOptionPane.YES_NO_OPTION);
						if (retVal != JOptionPane.OK_OPTION)
						{
							return;
						}
					}

					scalePanel.saveScale(file);
				}
			}

		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	private JButton createLoadButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load scale",
				"from XML file" }, 0);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Scale scale = null;
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					scale = scalePanel.loadScale(file);
					if (scale != null)
					{
						scalePanel.populateWidgets(scale);
					}
				}
			}

		});

		return button;
	}

	private void setScaleIntervalPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JLabel label = new JLabel("Scale with Intervals");
		label.setFont(getFont().deriveFont(Font.BOLD));
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		panel.add(label, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		updateFromIntervalsPageButton = new MultiLineButton(new String[] {
				"Load intervals", "from Scale Intervals", "Page" }, 0);
		updateFromIntervalsPageButton.addActionListener(new ActionListener()
		{
			@SuppressWarnings("rawtypes")
			public void actionPerformed(ActionEvent event)
			{
				Vector intervalData = (Vector) parent.getPageData(3);
				if (intervalData != null)
				{
					scaleIntervalPanel.setTableData(intervalData);
				}
			}
		});
		updateFromIntervalsPageButton.setEnabled(false);
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(updateFromIntervalsPageButton, gbc);

		scaleIntervalPanel = new ScaleIntervalPanel();
		scaleIntervalPanel.addDataPopulatedListener(this);
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(scaleIntervalPanel, gbc);

		JButton button = new JButton("Clear selected cells");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				scaleIntervalPanel.clearSelection();
			}

		});
		gbc.gridy = 3;
		gbc.insets = new Insets(5, 0, 5, 0);
		panel.add(button, gbc);

		button = new JButton("Delete selected cells");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				scaleIntervalPanel.deleteSelection();
			}

		});
		gbc.gridy = 4;
		panel.add(button, gbc);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		contentPanel.add(panel, gbc);
	}

	@SuppressWarnings("rawtypes")
	private void setReferencePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;

		// MultiLineLabel label = new MultiLineLabel(new String[] {
		// "Select a reference", "note, enter a", "reference frequency,",
		// "and convert the", "interval scale." });
		// gbc.gridx = 0;
		// gbc.gridy = 0;
		// gbc.insets = new Insets(0, 0, 10, 0);
		// panel.add(label, gbc);

		JLabel label2 = new JLabel("Reference note:");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(label2, gbc);

		noteSelector = new JComboBox();
		noteSelector.addItemListener(new ItemListener()
		{

			@Override
			public void itemStateChanged(ItemEvent event)
			{
				int state = event.getStateChange();
				Object selectedObject = event.getItem();

				isRefNoteSelected = state == ItemEvent.SELECTED
						&& selectedObject != null;
				updateCreateButton();
			}

		});
		noteSelector.setPreferredSize(new Dimension(90, 20));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(noteSelector, gbc);

		label2 = new JLabel("Reference frequency:");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(10, 0, 0, 0);
		panel.add(label2, gbc);

		frequencyField = new JTextField(11);
		frequencyField.addKeyListener(this);
		frequencyField.setDocument(new DoubleDocument());
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(frequencyField, gbc);

		createScaleButton = new MultiLineButton(new String[] { "Create scale",
				"with frequencies" }, 0);
		createScaleButton.setEnabled(false);
		createScaleButton.addActionListener(new ActionListener()
		{

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent event)
			{
				double refFreq = Double.parseDouble(frequencyField.getText());
				NoteInterval note = (NoteInterval) noteSelector.getModel()
						.getSelectedItem();
				double refInterval = note.getInterval();
				double freqMultiplier = refFreq / refInterval;

				Vector rows = new Vector();
				for (int i = 0; i < noteSelector.getItemCount(); i++)
				{
					NoteInterval thisNote = (NoteInterval) noteSelector
							.getItemAt(i);
					Vector row = new Vector();
					row.add(thisNote.getName());
					row.add(thisNote.getInterval() * freqMultiplier);
					rows.add(row);
				}

				scalePanel.setTableData(rows);
			}

		});
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.insets = new Insets(10, 0, 0, 0);
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(createScaleButton, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 20, 0, 10);
		contentPanel.add(panel, gbc);
	}

	private void setScalePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;

		JLabel label = new JLabel("Scale with Frequencies");
		label.setFont(getFont().deriveFont(Font.BOLD));
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 10, 0);
		panel.add(label, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		// gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 5, 5, 5);
		panel.add(createListButtons(), gbc);

		scalePanel = new ScalePanel();
		scalePanel.addDataPopulatedListener(this);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(scalePanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 10, 0, 0);
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.NONE;
		panel.add(createScaleButtons(), gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		contentPanel.add(panel, gbc);
	}

	protected void updateCreateButton()
	{
		createScaleButton.setEnabled(isRefNoteSelected && isRefFrequencySet);
	}

	private JPanel createScaleButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 0, 10, 10));
		JButton button = new MultiLineButton(new String[] { "Delete selected",
				"notes" }, 0);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				scalePanel.deleteSelectedNotes();
			}

		});
		panel.add(button);

		button = new MultiLineButton(new String[] { "Delete unselected",
				"notes" }, 0);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				scalePanel.deleteUnselectedNotes();
			}

		});
		panel.add(button);

		button = new JButton("Insert row above");
		panel.add(button);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				scalePanel.insertNoteAboveSelection();
			}

		});

		button = new JButton("Insert row below");
		panel.add(button);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				scalePanel.insertNoteBelowSelection();
			}

		});

		return panel;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateNoteSelector(Object tableData)
	{
		DefaultComboBoxModel model = (DefaultComboBoxModel) noteSelector
				.getModel();
		model.removeAllElements();

		Vector data = (Vector) tableData;
		if (data == null || data.size() == 0)
		{
			return;
		}

		for (Object row : data)
		{
			Vector rowData = (Vector) row;
			if (rowData != null && rowData.size() == 2)
			{
				String name = (String) rowData.get(0);
				Double interval = (Double) rowData.get(1);
				if (name != null && name.length() > 0 && interval != null)
				{
					NoteInterval note = new NoteInterval(name, interval);
					model.addElement(note);
				}
			}
		}

	}

	@Override
	public void setupWizardButtons()
	{
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
	}

	@Override
	public int getLeftPaneItems()
	{
		return LEFTPANE_STEPS;
	}

	class DoubleDocument extends PlainDocument
	{
		@Override
		public void insertString(int offset, String s, AttributeSet attributeSet)
				throws BadLocationException
		{
			try
			{
				String text = getText(0, getLength()) + s;
				if (!(text.length() == 1 && text.equals(".")))
				{
					Double.parseDouble(text);
				}
			}
			catch (Exception ex)
			{
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			super.insertString(offset, s, attributeSet);
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0)
	{
	}

	@Override
	public void keyReleased(KeyEvent event)
	{
		if (event.getSource().equals(frequencyField))
		{
			String frequency = frequencyField.getText();
			try
			{
				Double.parseDouble(frequency);
				isRefFrequencySet = true;
			}
			catch (Exception ex)
			{
				isRefFrequencySet = false;
			}

			updateCreateButton();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
	}

}
