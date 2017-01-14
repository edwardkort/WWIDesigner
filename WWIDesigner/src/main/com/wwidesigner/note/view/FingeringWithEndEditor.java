/**
 * TableCellEditor to edit fingering patterns with closable end in a table.
 * 
 * Copyright (C) 2017, Edward Kort, Antoine Lefebvre, Burton Patkau.
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
package com.wwidesigner.note.view;

import java.awt.Component;

import javax.swing.JTable;

import com.wwidesigner.note.Fingering;

/**
 * @author Burton Patkau
 *
 */
public class FingeringWithEndEditor extends FingeringEditor
{
	public FingeringWithEndEditor()
	{
		super();
		renderer = new FingeringWithEndRenderer();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		Fingering newFingering = (Fingering) value;
		// Take a copy of the fingering being edited.
		fingering.setNote(newFingering.getNote());
		fingering.setOpenHoles(getOpenHoles(newFingering.getOpenHole()));
		fingering.setOpenEnd(newFingering.getOpenEnd());
		renderer.startCellEditing(fingering, isSelected);
		return renderer;
	}

	@Override
	public boolean stopCellEditing()
	{
		fingering.setOpenEnd(((FingeringWithEndRenderer) renderer).getOpenEnd());
		return super.stopCellEditing();
	}

	@Override
	public Object getCellEditorValue()
	{
		Fingering newFingering = (Fingering) super.getCellEditorValue();
		newFingering.setOpenEnd(fingering.getOpenEnd());
		return newFingering;
	}

}
