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
import java.io.StringWriter;

import javax.swing.JOptionPane;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.view.InstrumentPanel;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.note.view.FingeringPatternPanel;
import com.wwidesigner.util.BindFactory;

public class ContainedInstrumentView extends ContainedXmlView implements DataPopulatedListener
{
	protected InstrumentPanel instrumentPanel;

	public ContainedInstrumentView(DataViewPane parent)
	{
		super(parent);

		instrumentPanel = new InstrumentPanel();
		instrumentPanel.addDataPopulatedListener(this);
	}

	@Override
	protected void setDataDirty()
	{
	}

	@Override
	public String getText()
	{
		BindFactory binder = GeometryBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		try
		{
			Instrument instrument = instrumentPanel.getData();
			if (instrument != null)
			{
				binder.marshalToXml(instrument, writer);
				return writer.toString();
			}
		}
		catch (Exception e)
		{
			System.out.println("Instrument update failed: " + e.getMessage());
		}
		return null;
	}

	@Override
	public void setText(String text)
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		try
		{
			if (text != null && ! text.isEmpty())
			{
				Instrument instrument = (Instrument) geometryBindFactory.unmarshalXml(text, true);
				instrumentPanel.loadData(instrument, true);
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
		return instrumentPanel;
	}

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
				// Data has changed.  Enable saving if data is valid.
				parent.makeDirty(dataPopulated);
			}
		}
	}
}
