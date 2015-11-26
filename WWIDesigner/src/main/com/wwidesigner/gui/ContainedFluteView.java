/**
 * Contained View to display and edit NAF Instrument objects.
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

import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.geometry.view.FluteInstrumentPanel;

public class ContainedFluteView extends ContainedInstrumentView
{
	public ContainedFluteView(DataViewPane parent)
	{
		super(parent);
	}

	@Override
	protected void setInstrumentPanel()
	{
		instrumentPanel = new FluteInstrumentPanel();
	}

}
