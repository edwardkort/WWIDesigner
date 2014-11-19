/**
 * Data View Pane to display and edit Instrument objects.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.geometry.view.InstrumentPanel;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.note.view.FingeringPatternPanel;
import com.wwidesigner.util.BindFactory;

/**
 * @author Burton Patkau
 * 
 */
public class InstrumentViewPane extends DataViewPane implements DataPopulatedListener
{
	protected Instrument instrument;
	protected InstrumentPanel instrumentPanel;
	protected boolean isInitialized;

	@Override
	protected void initializeComponents()
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		instrumentPanel = new InstrumentPanel();
		instrumentPanel.addDataPopulatedListener(this);
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		add(instrumentPanel, gbc);
	}

	@Override
	public void updateView(DataModel dataModel)
	{
		if ( dataModel instanceof BasicDataModel )
		{
			BasicDataModel model = (BasicDataModel) dataModel;
			BindFactory geometryBindFactory = GeometryBindFactory.getInstance();
			try
			{
				String xmlString = model.getData().toString();
				if (xmlString != null && ! xmlString.isEmpty())
				{
					instrument = (Instrument) geometryBindFactory.unmarshalXml(xmlString, true);
					instrumentPanel.loadData(instrument, true);
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "File " + model.getName()
						+ " does not contain a valid Instrument.");
			}
		}
	}

	@Override
	public void updateModel(DataModel dataModel)
	{
		if ( dataModel instanceof BasicDataModel )
		{
			BasicDataModel model = (BasicDataModel) dataModel;
			BindFactory binder = GeometryBindFactory.getInstance();
			StringWriter writer = new StringWriter();
			try
			{
				instrument = instrumentPanel.getData();
				if (instrument != null)
				{
					binder.marshalToXml(instrument, writer);
					model.setData(writer.toString());
				}
			}
			catch (Exception e)
			{
				System.out.println("Instrument update failed: " + e.getMessage());
			}
		}
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
				makeDirty(dataPopulated);
			}
		}
	}

}
