/**
 * Contained View to display and edit Instrument objects.
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

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.view.InstrumentPanel;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.note.view.FingeringPatternPanel;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.LengthType;

public class ContainedInstrumentView extends ContainedXmlView
{
	protected InstrumentPanel instrumentPanel;
	private JScrollPane scrollPane;

	public ContainedInstrumentView(DataViewPane parent)
	{
		super(parent);

		instrumentPanel = new InstrumentPanel();
		scrollPane = new JScrollPane(instrumentPanel);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);

		setDataDirty();
	}

	@Override
	protected void setDataDirty()
	{
		instrumentPanel.addDataPopulatedListener(new DataPopulatedListener()
		{
			@Override
			public void dataStateChanged(DataPopulatedEvent event)
			{
				Object source = event.getSource();
				if (source.equals(instrumentPanel))
				{
					Boolean dataPopulated = event
							.isPopulated(FingeringPatternPanel.SAVE_EVENT_ID);
					if (dataPopulated != null)
					{
						// Data has changed. Enable saving if data is valid.
						parent.makeDirty(dataPopulated);
					}
				}
			}
		});
	}

	@Override
	public String getText()
	{
		String xmlText = null;
		Instrument instrument = instrumentPanel.getData();
		if (instrument != null)
		{
			try
			{
				xmlText = StudyModel.marshal(instrument);
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}

		return xmlText;
	}

	@Override
	public void setText(String text)
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		try
		{
			if (text != null && !text.isEmpty())
			{
				Instrument instrument = (Instrument) geometryBindFactory
						.unmarshalXml(text, true);
				LengthType originalDimensionType = instrument.getLengthType();
				LengthType dimensionType = getApplicationLengthType();
				instrument.convertToMetres();
				instrument.setLengthType(dimensionType);
				instrument.convertToLengthType();
				instrumentPanel.loadData(instrument,
						originalDimensionType.equals(dimensionType));
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(parent,
					"XML input does not define a valid Instrument.");
		}
	}

	@Override
	public Component getViewComponent()
	{
		return scrollPane;
	}

}
