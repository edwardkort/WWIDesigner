package com.wwidesigner.note.wizard;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.AbstractWizardPage;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;
import com.wwidesigner.gui.util.MultiLineButton;
import com.wwidesigner.gui.util.XmlFileChooser;
import com.wwidesigner.note.Temperament;
import com.wwidesigner.note.Temperament.StandardTemperament;
import com.wwidesigner.note.view.TemperamentPanel;

public class TemperamentPage extends AbstractWizardPage implements
		DataPopulatedListener, DataProvider, DataPopulatedProvider
{
	private JPanel contentPanel;
	private JScrollPane scrollPane;
	TemperamentPanel tempPanel;
	TuningWizardDialog parent;
	private JButton saveButton;
	private boolean isInitialized;

	public TemperamentPage(TuningWizardDialog aParent)
	{
		super("Musical Temperament",
				"Select or create the note intervals (temperament) used in the scale.");
		this.parent = aParent;
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
			loadTemperamentPanel();
			loadTemperamentButtons();
			isInitialized = true;
			scrollPane = new JScrollPane(contentPanel);
		}

		return scrollPane;
	}

	private void loadListButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		JButton button = new JButton("New");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				tempPanel.setName(null);
				tempPanel.setDescription(null);
				tempPanel.resetTableData();
			}
		});
		panel.add(button);

		panel.add(createStandardsButton());

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

	private JButton createStandardsButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load a standard",
				"temperament" }, 0);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				StandardTemperament temperamentType = (StandardTemperament) JOptionPane
						.showInputDialog(getParent(),
								"Select a standard temperament",
								"Standard Temperaments",
								JOptionPane.OK_CANCEL_OPTION, null,
								StandardTemperament.values(),
								StandardTemperament.TET_12);
				if (temperamentType != null)
				{
					tempPanel.populateWidgets(Temperament
							.makeStandardTemperament(temperamentType));
				}
			}

		});

		return button;
	}

	private JButton createLoadButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load temperament",
				"from XML file" }, 1);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Temperament temp = null;
				JFileChooser chooser = new XmlFileChooser(parent.getCurrentSaveDirectory());
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					temp = tempPanel.loadTemperament(file);
					if (temp != null)
					{
						parent.setCurrentSaveDirectory(file.getParentFile());
						tempPanel.populateWidgets(temp);
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
				JFileChooser chooser = new XmlFileChooser(parent.getCurrentSaveDirectory());
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

					parent.setCurrentSaveDirectory(file.getParentFile());
					tempPanel.saveTemperament(file);
				}
			}

		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	private void loadTemperamentPanel()
	{
		tempPanel = new TemperamentPanel();
		addDataPopulatedListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 2;
		contentPanel.add(tempPanel, gbc);
	}

	private void loadTemperamentButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JButton button = new JButton("Delete selected");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				tempPanel.deleteSelectedRatios();
			}

		});
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(button, gbc);

		button = new JButton("Delete unselected");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				tempPanel.deleteUnselectedRatios();
			}

		});
		gbc.gridx = 1;
		panel.add(button, gbc);

		button = new JButton("Insert row above");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				tempPanel.insertRatioAboveSelection();
			}

		});
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(button, gbc);

		button = new JButton("Insert row below");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				tempPanel.insertRatioBelowSelection();
			}

		});
		gbc.gridx = 1;
		panel.add(button, gbc);

		// button = new JButton("Add octave above");
		// panel.add(button);

		button = new MultiLineButton(new String[] { "Make octave from",
				"selection and", "add below" }, 0);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				tempPanel.addOctaveBelow();
			}

		});
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		panel.add(button, gbc);

		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.insets = new Insets(0, 10, 20, 0);
		gbc.gridx = 1;
		gbc.gridy = 2;
		contentPanel.add(panel, gbc);
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

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		if (event.getSource() instanceof TemperamentPanel)
		{
			Boolean canSave = event.isPopulated(TemperamentPanel.SAVE_ID);
			if (canSave != null)
			{
				saveButton.setEnabled(canSave);
			}
		}
	}

	@Override
	public Object getPageData()
	{
		if (tempPanel == null)
		{
			return null;
		}

		return tempPanel.getTemperament();
	}

	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		tempPanel.addDataPopulatedListener(listener);
	}

}
