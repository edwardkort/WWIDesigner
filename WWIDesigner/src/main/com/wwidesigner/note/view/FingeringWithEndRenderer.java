/**
 * JPanel used as a TableCellRenderer to render fingering patterns with closable end in a table.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JRadioButton;
import javax.swing.JTable;

import com.wwidesigner.note.Fingering;

/**
 * @author Burton Patkau
 *
 */
public class FingeringWithEndRenderer extends FingeringRenderer
{
	protected JRadioButton mEnd;

	public FingeringWithEndRenderer(int numHoles)
	{
		super(numHoles);
	}

	public FingeringWithEndRenderer()
	{
		this(0);
	}

	@Override
	public void createHoles(int numHoles)
	{
		super.createHoles(numHoles);
		mEnd = new JRadioButton();
		mEnd.setToolTipText("End of bore is open/closed");
		mEnd.setBackground(Color.WHITE);
		mEnd.setPreferredSize(new Dimension(mHoleLength, mHoleHeight));
		mEnd.setMinimumSize(new Dimension(mHoleLength, mHoleHeight));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 0;
		if (numHoles > 0)
		{
			gbc.insets = new Insets(0, 8, 0, 0);
		}
		else
		{
			gbc.insets = new Insets(0, 40, 0, 0);
		}
		gbc.weightx = 1.0;
		gbc.gridx = numHoles;
		add(mEnd, gbc);
	}
	
	public boolean getOpenEnd()
	{
		return ! mEnd.isSelected();
	}
	
	public void setOpenEnd(Boolean openEnd)
	{
		if (openEnd != null && ! openEnd)
		{
			mEnd.setSelected(true);
		}
		else
		{
			mEnd.setSelected(false);
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension size = super.getPreferredSize();
		size.setSize(size.getWidth() + mHoleLength, size.getHeight());
		return size;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (value != null && value instanceof Fingering)
		{
			Fingering fingering = (Fingering) value;
			setOpenEnd(fingering.getOpenEnd());
			return this;
		}

		return null;
	}

	@Override
	public void startCellEditing(Fingering fingering, boolean isSelected)
	{
		super.startCellEditing(fingering, isSelected);
		setOpenEnd(fingering.getOpenEnd());
	}

	@Override
	public void setEnableDataChanges(boolean enableChanges)
	{
		super.setEnableDataChanges(enableChanges);
		if (!enableDataChanges)
		{
			mEnd.removeChangeListener(this);
		}
		else
		{
			mEnd.addChangeListener(this);
		}
	}

}
