/**
 * Contained View to display and edit Tuning objects.
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
package com.wwidesigner.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.note.view.FingeringPatternPanel;
import com.wwidesigner.note.view.TuningPanel;
import com.wwidesigner.note.view.WhistleTuningPanel;
import com.wwidesigner.util.BindFactory;

public class ContainedTuningView extends ContainedXmlView implements DataPopulatedListener
{
	protected TuningPanel tuningPanel;	// For raw tuning data.
	protected JPanel myPanel;			// For tuningPanel and editing buttons.

	public ContainedTuningView(DataViewPane parent)
	{
		super(parent);

		tuningPanel = new WhistleTuningPanel( 480 );
		tuningPanel.addDataPopulatedListener(this);
		myPanel = new JPanel();
		myPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		myPanel.add(tuningPanel, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(4, 1));
		JButton button;

		button = new JButton("Add row above selection");
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

		gbc.anchor = GridBagConstraints.SOUTHWEST;
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		myPanel.add(buttonPanel, gbc);
	}

	@Override
	protected void setDataDirty()
	{
	}

	@Override
	public String getText()
	{
		BindFactory binder = NoteBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		try
		{
			Tuning tuning = tuningPanel.getData();
			if (tuning != null)
			{
				binder.marshalToXml(tuning, writer);
				return writer.toString();
			}
		}
		catch (Exception e)
		{
			System.out.println("Tuning update failed: " + e.getMessage());
		}
		return null;
	}

	@Override
	public void setText(String text)
	{
		BindFactory binder = NoteBindFactory.getInstance();
		try
		{
			if (text != null && ! text.isEmpty())
			{
				Tuning tuning = (Tuning) binder.unmarshalXml(text, true);
				tuningPanel.loadData(tuning, true);
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(parent, 
					"XML input does not define a valid Tuning.");
		}
	}

	@Override
	public Component getViewComponent()
	{
		return myPanel;
	}

	@Override
	public void dataStateChanged(DataPopulatedEvent event)
	{
		Object source = event.getSource();
		if (source.equals(tuningPanel))
		{
			Boolean dataPopulated = event
						.isPopulated(FingeringPatternPanel.SAVE_EVENT_ID);
			if (dataPopulated != null)
			{
				// Data has changed.  Enable saving if data is valid.
				parent.makeDirty(dataPopulated);
			}
		}
	}
}
