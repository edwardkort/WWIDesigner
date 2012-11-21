package com.wwidesigner.note.wizard;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.AbstractWizardPage;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;
import com.wwidesigner.gui.util.MultiLineButton;
import com.wwidesigner.gui.util.XmlFileChooser;
import com.wwidesigner.note.ScaleSymbolList;
import com.wwidesigner.note.Temperament;
import com.wwidesigner.note.view.ScaleIntervalPanel;
import com.wwidesigner.note.view.ScaleSymbolListPanel;
import com.wwidesigner.note.view.TemperamentPanel;

public class ScaleIntervalPage extends AbstractWizardPage implements
		DataProvider, DataPopulatedProvider, DataPopulatedListener
{
	private JPanel contentPanel;
	private ScaleIntervalPanel scaleIntervalPanel;
	private ScaleSymbolListPanel symbolsPanel;
	private TemperamentPanel temperamentPanel;
	private TuningWizardDialog parent;
	private boolean isInitialized;
	private JButton updateFromSymbolPageButton;
	private JButton updateFromTemperamentPageButton;

	public ScaleIntervalPage(TuningWizardDialog parent)
	{
		super(
				"Scale with Intervals",
				"Drag and drop selected scale symbols and temperament intervals to create a scale with interval definitions.");
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

			setSymbolListPanel();
			setScaleIntervalPanel();
			setIntervalPanel();
			isInitialized = true;
		}

		return contentPanel;
	}

	private void setScaleIntervalPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;

		scaleIntervalPanel = new ScaleIntervalPanel();
		gbc.gridx = 0;
		gbc.gridy = 0;
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
		gbc.gridy = 1;
		panel.add(button, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		contentPanel.add(panel, gbc);
	}

	private void setSymbolListPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Scale Symbols");
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
		panel.add(addSymbolsButtons(), gbc);

		symbolsPanel = new ScaleSymbolListPanel();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(symbolsPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 10);
		contentPanel.add(panel, gbc);
	}

	private JComponent addSymbolsButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		updateFromSymbolPageButton = new MultiLineButton(new String[] {
				"Load symbols", "from Symbols", "Page" }, 0);
		updateFromSymbolPageButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				ScaleSymbolList symbols = (ScaleSymbolList) parent
						.getPageData(1);
				if (symbols != null)
				{
					symbolsPanel.populateWidgets(symbols);
				}
			}
		});
		updateFromSymbolPageButton.setEnabled(false);
		panel.add(updateFromSymbolPageButton);

		JButton button = new MultiLineButton(new String[] { "Load symbols",
				"from", "XML file" }, 1);
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
					symbols = symbolsPanel.loadSymbolList(file);
					if (symbols != null)
					{
						symbolsPanel.populateWidgets(symbols);
					}
				}
			}

		});
		panel.add(button);

		return panel;
	}

	private void setIntervalPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Intervals");
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
		panel.add(addIntervalButtons(), gbc);

		temperamentPanel = new TemperamentPanel();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(temperamentPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 10, 0, 0);
		contentPanel.add(panel, gbc);
	}

	private Component addIntervalButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		updateFromTemperamentPageButton = new MultiLineButton(new String[] {
				"Load intervals", "from Temperament", "Page" }, 0);
		updateFromTemperamentPageButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				Temperament temperament = (Temperament) parent.getPageData(2);
				if (temperament != null)
				{
					temperamentPanel.populateWidgets(temperament);
				}
			}
		});
		updateFromTemperamentPageButton.setEnabled(false);
		panel.add(updateFromTemperamentPageButton);

		JButton button = new MultiLineButton(new String[] { "Load intervals",
				"from", "XML file" }, 1);
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				Temperament temp = null;
				JFileChooser chooser = new XmlFileChooser();
				int state = chooser.showOpenDialog(getParent());
				if (state == JFileChooser.APPROVE_OPTION)
				{
					File file = chooser.getSelectedFile();
					temp = temperamentPanel.loadTemperament(file);
					if (temp != null)
					{
						temperamentPanel.populateWidgets(temp);
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
		fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
	}

	@Override
	public int getLeftPaneItems()
	{
		return LEFTPANE_STEPS;
	}

	@Override
	public Object getPageData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		Object source = event.getSource();
		if (source instanceof ScaleSymbolListPanel)
		{
			updateFromSymbolPageButton.setEnabled(event.isPopulated());
		}
		else if (source instanceof TemperamentPanel)
		{
			updateFromTemperamentPageButton.setEnabled(event.isPopulated());
		}
	}

}
