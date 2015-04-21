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

import javax.swing.JScrollPane;

import com.jidesoft.app.framework.DataModelException;
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

		setInstrumentPanel();
		scrollPane = new JScrollPane(instrumentPanel);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);

		setDataDirty();
	}

	protected void setInstrumentPanel()
	{
		instrumentPanel = new InstrumentPanel();
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
						// In JDAF, a "new" tab will be deleted, when opening a
						// file-based tab, if it is not dirty. Currently, we
						// don't know when a tab is "new", but it is set dirty
						// on creation. Therefore, for now, do not make a tab
						// not-dirty, if it is dirty.
						if (!parent.isDirty())
						{
							// Data has changed. Enable saving if data is valid.
							parent.makeDirty(dataPopulated);
						}
					}
				}
			}
		});
	}

	@Override
	public String getText() throws DataModelException
	{
		String xmlText = null;
		Instrument instrument = instrumentPanel.getData();
		if (instrument == null)
		{
			throw new DataModelException("Missing or invalid data on Instrument panel");
		}
		try
		{
			instrument.checkValidity();
			xmlText = StudyModel.marshal(instrument);
		}
		catch (Exception ex)
		{
			throw new DataModelException(null, ex);
		}

		return xmlText;
	}

	@Override
	public void setText(String text) throws DataModelException
	{
		BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
		try
		{
			if (text != null && !text.isEmpty())
			{
				Instrument instrument = (Instrument) geometryBindFactory
						.unmarshalXml(text, true);
				LengthType dimensionType = getApplicationLengthType();
				boolean changed = instrument.convertToLengthType(dimensionType);
				instrumentPanel.loadData(instrument, !changed);
			}
		}
		catch (Exception e)
		{
			throw new DataModelException(null, e);
		}
	}

	@Override
	public Component getViewComponent()
	{
		return scrollPane;
	}

}
