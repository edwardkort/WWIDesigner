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
import com.jidesoft.wizard.WizardDialog;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;
import com.wwidesigner.gui.util.MultiLineButton;
import com.wwidesigner.gui.util.XmlFileChooser;
import com.wwidesigner.note.ScaleSymbolList;
import com.wwidesigner.note.ScaleSymbolList.StandardSymbols;
import com.wwidesigner.note.view.ScaleSymbolListPanel;

public class ScaleSymbolPage extends AbstractWizardPage implements
		DataPopulatedListener, DataProvider, DataPopulatedProvider
{
	private JPanel contentPanel;
	private ScaleSymbolListPanel symbolPanel;
	private JButton saveButton;
	private WizardDialog parent;
	private boolean isInitialized;

	public ScaleSymbolPage(WizardDialog parent)
	{
		super("Scale Symbols",
				"Select or create the note symbols used in the scale.");
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
			loadListButtons();
			loadSymbolsPanel();
			loadSymbolButtons();
			isInitialized = true;
		}

		return contentPanel;
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

	public Object getPageData()
	{
		if (symbolPanel != null)
		{
			return symbolPanel.getScaleSymbolList();
		}

		return null;
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
				symbolPanel.setName(null);
				symbolPanel.setDescription(null);
				symbolPanel.resetTableData();
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

	private void loadSymbolsPanel()
	{
		symbolPanel = new ScaleSymbolListPanel();
		addDataPopulatedListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 2;
		contentPanel.add(symbolPanel, gbc);
	}

	private JButton createStandardsButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load a standard",
				"symbol set" }, 0);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				StandardSymbols symbolType = (StandardSymbols) JOptionPane
						.showInputDialog(getParent(),
								"Select a standard symbol list",
								"Standard Symbol Lists",
								JOptionPane.OK_CANCEL_OPTION, null,
								StandardSymbols.values(),
								StandardSymbols.SCIENTIFIC);
				if (symbolType != null)
				{
					symbolPanel.populateWidgets(ScaleSymbolList
							.makeStandardSymbols(symbolType));
				}
			}

		});

		return button;
	}

	private JButton createLoadButton()
	{
		JButton button = new MultiLineButton(new String[] { "Load symbols",
				"from XML file" }, 1);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				ScaleSymbolList symbols = null;
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					symbols = symbolPanel.loadSymbolList(file);
					if (symbols != null)
					{
						symbolPanel.populateWidgets(symbols);
					}
				}
			}

		});

		return button;
	}

	private void loadSymbolButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 0, 10, 10));
		JButton button = new JButton("Delete selected");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				symbolPanel.deleteSelectedSymbols();
			}

		});
		panel.add(button);

		button = new JButton("Delete unselected");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				symbolPanel.deleteUnselectedSymbols();
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
				symbolPanel.insertSymbolAboveSelection();
			}

		});

		button = new JButton("Insert row below");
		panel.add(button);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent event)
			{
				symbolPanel.insertSymbolBelowSelection();
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

					symbolPanel.saveSymbolList(file);
				}
			}

		});
		saveButton.setEnabled(false);
		return saveButton;
	}

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		if (event.getSource() instanceof ScaleSymbolListPanel)
		{
			saveButton.setEnabled(event.isPopulated());
		}
	}

	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		symbolPanel.addDataPopulatedListener(listener);
	}

}
