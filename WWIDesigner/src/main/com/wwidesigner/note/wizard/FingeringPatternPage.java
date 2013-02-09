package com.wwidesigner.note.wizard;

import java.awt.Component;
import java.awt.FlowLayout;
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
import com.wwidesigner.note.view.FingeringPatternPanel;

public class FingeringPatternPage extends AbstractWizardPage implements
		DataPopulatedListener, DataProvider, DataPopulatedProvider
{
	private JPanel contentPanel;
	private FingeringPatternPanel fingeringPanel;
	private JButton newButton;
	private JButton saveButton;
	private boolean isInitialized;

	public FingeringPatternPage()
	{
		super(
				"Fingering Pattern",
				"Select or create the fingerings used in the scale. "
						+ "Set the number of holes before creating a New fingering pattern. "
						+ "Click on a fingering to initial editing (red outline); "
						+ "Hit Esc to cancel editing, Enter to accept edits.");
		createWizardContent();
	}

	@Override
	public JComponent createWizardContent()
	{
		if (!isInitialized)
		{
			contentPanel = new JPanel();
			contentPanel.setLayout(new GridBagLayout());
			loadListButtons();
			loadFingeringPanel();
			loadFingeringButtons();
			isInitialized = true;
		}

		return contentPanel;
	}

	@Override
	public void setupWizardButtons()
	{
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.BACK);
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
		fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.CANCEL);
	}

	@Override
	public int getLeftPaneItems()
	{
		return LEFTPANE_STEPS;
	}

	public Object getPageData()
	{
		if (fingeringPanel != null)
		{
			return fingeringPanel.getFingeringPattern();
		}

		return null;
	}

	private void loadListButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		newButton = new JButton("New");
		newButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				fingeringPanel.setName(null);
				fingeringPanel.setDescription(null);
				int numHoles = fingeringPanel.getNumberOfHoles();
				fingeringPanel.resetTableData(numHoles + 1, numHoles);
			}
		});
		newButton.setEnabled(false);
		panel.add(newButton);

		panel.add(createLoadButton());

		panel.add(createSaveButton());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(20, 20, 20, 20);
		contentPanel.add(panel, gbc);
	}

	private void loadFingeringPanel()
	{
		fingeringPanel = new FingeringPatternPanel();
		addDataPopulatedListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 2;
		contentPanel.add(fingeringPanel, gbc);
	}

	private JButton createLoadButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load fingering",
				"pattern from", "XML file" }, 1);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				FingeringPattern fingerings = null;
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					fingerings = fingeringPanel.loadFingeringPattern(file);
					if (fingerings != null)
					{
						fingeringPanel.populateWidgets(fingerings);
					}
				}
			}

		});

		return button;
	}

	private void loadFingeringButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 0, 10, 10));
		JButton button = new JButton("Delete selected");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				fingeringPanel.deleteSelectedFingerings();
			}

		});
		panel.add(button);

		button = new JButton("Delete unselected");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				fingeringPanel.deleteUnselectedFingerings();
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
				fingeringPanel.insertFingeringAboveSelection();
			}

		});

		button = new JButton("Insert row below");
		panel.add(button);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				fingeringPanel.insertFingeringBelowSelection();
			}

		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.gridy = 2;
		contentPanel.add(panel, gbc);
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

					fingeringPanel.saveFingeringPattern(file);
				}
			}

		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		if (event.getSource() instanceof FingeringPatternPanel)
		{
			Boolean dataPopulated = null;
			dataPopulated = event
					.isPopulated(FingeringPatternPanel.NEW_EVENT_ID);
			if (dataPopulated != null)
			{
				newButton.setEnabled(dataPopulated);
			}
			else
			{
				dataPopulated = event
						.isPopulated(FingeringPatternPanel.SAVE_EVENT_ID);
				if (dataPopulated != null)
				{
					saveButton.setEnabled(dataPopulated);
				}
			}
		}
	}

	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		fingeringPanel.addDataPopulatedListener(listener);
	}

}
