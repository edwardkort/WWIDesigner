package com.wwidesigner.note.wizard;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.AbstractWizardPage;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;
import com.wwidesigner.gui.util.MultiLineButton;
import com.wwidesigner.gui.util.XmlFileChooser;
import com.wwidesigner.note.FingeringPattern;
import com.wwidesigner.note.Scale;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.view.FingeringPatternPanel;
import com.wwidesigner.note.view.ScalePanel;
import com.wwidesigner.note.view.TuningPanel;

public class TuningPage extends AbstractWizardPage implements
		DataPopulatedProvider, DataPopulatedListener
{
	private JPanel contentPanel;
	private TuningPanel tuningPanel;
	private ScalePanel scalePanel;
	private FingeringPatternPanel fingeringPanel;
	private TuningWizardDialog parent;
	private boolean isInitialized;
	private JButton updateFromScalePageButton;
	private JButton updateFromFingeringPageButton;
	private JButton newButton;
	private JButton saveButton;

	public TuningPage(TuningWizardDialog parent)
	{
		super("Scale with Frequencies and Fingerings (Tuning)",
				"Drag and drop selected notes and fingerings to create a tuning.");
		this.parent = parent;
		createWizardContent();
	}

	@Override
	public JComponent createWizardContent()
	{
		if (!isInitialized)
		{
			contentPanel = new JPanel();
			contentPanel.setLayout(new GridBagLayout());

			setScalePanel();
			setTuningPanel();
			setFingeringPanel();
			isInitialized = true;
		}

		return contentPanel;
	}

	private void setTuningPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label = new JLabel("Tuning");
		label.setFont(getFont().deriveFont(Font.BOLD));
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipady = 15;
		panel.add(label, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(createTuningButtons(), gbc);

		tuningPanel = new TuningPanel();
		addDataPopulatedListener(this);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		panel.add(tuningPanel, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));
		JButton button = new JButton("Add row above selection");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				tuningPanel.insertFingeringAboveSelection();
			}

		});
		buttonPanel.add(button);

		button = new JButton("Add row below selection");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				tuningPanel.insertFingeringBelowSelection();
			}

		});
		buttonPanel.add(button);

		button = new JButton("Delete selected rows");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				tuningPanel.deleteSelectedFingerings();
			}

		});
		buttonPanel.add(button);

		gbc.gridy = 3;
		// gbc.insets = new Insets(5, 0, 5, 0);
		gbc.ipady = 0;
		panel.add(buttonPanel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		contentPanel.add(panel, gbc);
	}

	private Component createTuningButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		newButton = new JButton("New");
		newButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				tuningPanel.setName(null);
				tuningPanel.setDescription(null);
				int numHoles = tuningPanel.getNumberOfHoles();
				tuningPanel.resetTableData(numHoles + 1, numHoles);
			}
		});
		newButton.setEnabled(false);
		panel.add(newButton);

		panel.add(createLoadButton());

		panel.add(createSaveButton());

		return panel;
	}

	private JButton createLoadButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load tuning",
				"from", "XML file" }, 1);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Tuning tuning = null;
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					tuning = tuningPanel.loadFingeringPattern(file);
					if (tuning != null)
					{
						tuningPanel.populateWidgets(tuning);
					}
				}
			}

		});

		return button;
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

					tuningPanel.saveFingeringPattern(file);
				}
			}

		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	private void setScalePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Scale");
		label.setFont(getFont().deriveFont(Font.BOLD));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipady = 20;
		panel.add(label, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.ipady = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		panel.add(addScaleButtons(), gbc);

		scalePanel = new ScalePanel();
		scalePanel.setTableCellSelectionEnabled(false);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(scalePanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 10);
		contentPanel.add(panel, gbc);
	}

	private JComponent addScaleButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		updateFromScalePageButton = new MultiLineButton(new String[] {
				"Load scale", "from Scale with", "Frequencies Page" }, 0);
		updateFromScalePageButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				Scale scale = (Scale) parent.getPageData(4);
				if (scale != null)
				{
					scalePanel.populateWidgets(scale);
				}
			}
		});
		updateFromScalePageButton.setEnabled(false);
		panel.add(updateFromScalePageButton);

		JButton button = new MultiLineButton(new String[] { "Load scale",
				"from", "XML file" }, 1);
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
		panel.add(button);

		return panel;
	}

	private void setFingeringPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Fingerings");
		label.setFont(getFont().deriveFont(Font.BOLD));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipady = 20;
		panel.add(label, gbc);

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.ipady = 0;
		gbc.insets = new Insets(0, 0, 10, 0);
		panel.add(addFingeringButtons(), gbc);

		fingeringPanel = new FingeringPatternPanel();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(fingeringPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 10, 0, 0);
		contentPanel.add(panel, gbc);
	}

	private Component addFingeringButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		updateFromFingeringPageButton = new MultiLineButton(new String[] {
				"Load fingerings", "from Fingering", "Pattern Page" }, 0);
		updateFromFingeringPageButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				FingeringPattern fingeringPattern = (FingeringPattern) parent
						.getPageData(5);
				if (fingeringPattern != null)
				{
					fingeringPanel.populateWidgets(new FingeringPattern(
							fingeringPattern));
				}
			}
		});
		updateFromFingeringPageButton.setEnabled(false);
		panel.add(updateFromFingeringPageButton);

		JButton button = new MultiLineButton(new String[] { "Load fingerings",
				"from", "XML file" }, 1);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				FingeringPattern temp = null;
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					temp = fingeringPanel.loadFingeringPattern(file);
					if (temp != null)
					{
						fingeringPanel.populateWidgets(temp);
					}
				}
			}

		});
		panel.add(button);

		return panel;
	}

	@Override
	public void setupWizardButtons()
	{
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
		fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
		fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.CANCEL);
	}

	@Override
	public int getLeftPaneItems()
	{
		return LEFTPANE_STEPS;
	}

	@Override
	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		tuningPanel.addDataPopulatedListener(listener);
	}

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		Object source = event.getSource();
		if (source instanceof ScalePanel)
		{
			if (!source.equals(scalePanel))
			{
				Boolean isPopulated = event
						.isPopulated(ScalePanel.LOAD_PAGE_ID);
				boolean isPop = isPopulated == null ? false : isPopulated;
				updateFromScalePageButton.setEnabled(isPop);
			}
		}
		else if (source instanceof FingeringPatternPanel)
		{
			if (!source.equals(fingeringPanel) && !source.equals(tuningPanel))
			{
				updateFromFingeringPageButton.setEnabled(event.isPopulated());
			}
			else if (source.equals(tuningPanel))
			{
				Boolean dataPopulated = null;
				dataPopulated = event
						.isPopulated(FingeringPatternPanel.NEW_EVENT_ID);
				if (dataPopulated != null)
				{
					newButton.setEnabled(dataPopulated);
				}
				dataPopulated = event
						.isPopulated(FingeringPatternPanel.SAVE_EVENT_ID);
				if (dataPopulated != null)
				{
					saveButton.setEnabled(dataPopulated);
				}
			}
		}
	}

}
