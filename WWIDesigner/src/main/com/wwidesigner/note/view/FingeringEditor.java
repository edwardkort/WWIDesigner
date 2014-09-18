/**
 * TableCellEditor to edit fingering patterns in a table.
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
package com.wwidesigner.note.view;

import java.awt.Component;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.wwidesigner.note.Fingering;

/**
 * @author Edward Kort
 *
 */
public class FingeringEditor extends AbstractCellEditor implements
		TableCellEditor
{
	Fingering fingering = new Fingering();
	FingeringRenderer renderer = new FingeringRenderer();

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		Fingering newFingering = (Fingering) value;
		// Take a copy of the fingering being edited.
		fingering.setNote(newFingering.getNote());
		fingering.setOpenHoles(getOpenHoles(newFingering.getOpenHole()));
		renderer.startCellEditing(fingering, isSelected);
		return renderer;
	}

	@Override
	public boolean stopCellEditing()
	{
		fingering.setOpenHoles(renderer.getOpenHoles());
		renderer.stopCellEditing();
		return super.stopCellEditing();
	}

	@Override
	public void cancelCellEditing()
	{
		renderer.stopCellEditing();
		super.cancelCellEditing();
	}

	@Override
	public Object getCellEditorValue()
	{
		// Return a copy of the fingering we use to edit.
		Fingering newFingering = new Fingering();
		newFingering.setNote(fingering.getNote());
		newFingering.setOpenHoles(getOpenHoles(fingering.getOpenHole()));
		return newFingering;
	}

	/**
	 * Create a native boolean copy of an open hole list.
	 * @param openHoles - list of Boolean objects
	 * @return Array of native boolean values
	 */
	static protected boolean[] getOpenHoles(List<Boolean> openHoles)
	{
		boolean[] isOpen = new boolean[openHoles.size()];
		for (int i = 0; i < openHoles.size(); i++)
		{
			if (openHoles.get(i) != null && openHoles.get(i))
			{
				isOpen[i] = true;
			}
			else
			{
				isOpen[i] = false;
			}
		}
		return isOpen;
	}

}
