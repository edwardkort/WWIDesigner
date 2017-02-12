/**
 * JPanel used as a TableCellRenderer to render fingering patterns in a table.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.gui.util.DataChangedProvider;
import com.wwidesigner.note.Fingering;

/**
 * @author Edward Kort
 *
 */
public class FingeringRenderer extends JPanel implements TableCellRenderer,
		ChangeListener, DataChangedProvider
{
	protected static final float NORMAL_STROKE_WIDTH = 1.0f;
	protected static final float BOLD_STROKE_WIDTH = 3.0f;
	protected static final Color NORMAL_COLOR = Color.BLACK;
	protected static final Color EDIT_COLOR = Color.BLUE;

	protected JRadioButton[] mHoles = {};
	protected JPanel mButtonPanel = new JPanel();
	protected Image mImage;
	protected int mHoleLength = 20;
	protected int mHoleHeight = 16;
	protected float strokeWidth = NORMAL_STROKE_WIDTH;
	protected Color strokeColor = NORMAL_COLOR;
	protected List<DataChangedListener> dataChangedListeners;
	protected boolean enableDataChanges = false;

	public FingeringRenderer(int numHoles)
	{
		createHoles(numHoles);
		Border border = new EmptyBorder(2, 2, 2, 2);
		setBorder(border);
	}

	public FingeringRenderer()
	{
		this(0);
	}

	public void createHoles(int numHoles)
	{
		setHoleSize();
		removeAll();
		mHoles = new JRadioButton[numHoles];
		for (int i = 0; i < numHoles; i++)
		{
			JRadioButton button = new JRadioButton();
			mHoles[i] = button;
			mHoles[i].setPreferredSize(new Dimension(mHoleLength, mHoleHeight));
			mHoles[i].setMinimumSize(new Dimension(mHoleLength, mHoleHeight));
		}

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 15, 0, 0); // For first hole only.
		for (int i = 0; i < mHoles.length; i++)
		{
			gbc.gridx = i;
			add(mHoles[i], gbc);
			gbc.insets = new Insets(0, 0, 0, 0);
		}
	}

	protected void setHoleSize()
	{
		JRadioButton button = new JRadioButton();
		Dimension size = button.getPreferredSize();
		mHoleLength = (int) Math.ceil(size.getWidth());
		mHoleHeight = (int) Math.ceil(size.getHeight());
	}

	protected void initializeComponents(int numHoles, boolean isSelected,
			boolean isEditing)
	{
		if (isSelected)
		{
			strokeWidth = BOLD_STROKE_WIDTH;
		}
		else
		{
			strokeWidth = NORMAL_STROKE_WIDTH;
		}
		if (isEditing)
		{
			strokeColor = EDIT_COLOR;
		}
		else
		{
			strokeColor = NORMAL_COLOR;
		}

		if (numHoles != mHoles.length)
		{
			createHoles(numHoles);
		}
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (value != null && value instanceof Fingering)
		{
			Fingering fingering = (Fingering) value;
			initializeComponents(fingering.getNumberOfHoles(), isSelected,
					false);
			setOpenHoles(fingering.getOpenHole());
			return this;
		}

		return null;
	}

	public void startCellEditing(Fingering fingering, boolean isSelected)
	{
		initializeComponents(fingering.getNumberOfHoles(), isSelected, true);
		setOpenHoles(fingering.getOpenHole());
	}

	public void stopCellEditing()
	{
		strokeColor = NORMAL_COLOR;
	}

	public boolean[] getOpenHoles()
	{
		boolean[] openHoles = new boolean[mHoles.length];
		for (int i = 0; i < mHoles.length; i++)
		{
			openHoles[i] = !mHoles[i].isSelected();
		}
		return openHoles;
	}

	public void setOpenHoles(List<Boolean> openHoles)
	{
		boolean isClosed;
		for (int i = 0; i < mHoles.length; i++)
		{
			if (i >= openHoles.size() || openHoles.get(i) == null
					|| !openHoles.get(i))
			{
				isClosed = true;
			}
			else
			{
				isClosed = false;
			}
			mHoles[i].setSelected(isClosed);
		}
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.WHITE);
		g2.fill(new Rectangle(getPreferredSize()));
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setColor(strokeColor);
		g2.draw(new Arc2D.Double(4, 2, 68, mHoleHeight + 7, 90, 180, Arc2D.OPEN));
		int lineLength = mHoles.length * mHoleLength;
		g2.draw(new Line2D.Double(38, 2, 38 + lineLength, 2));
		g2.draw(new Line2D.Double(38, mHoleHeight + 9, 38 + lineLength,
				mHoleHeight + 9));
		g2.draw(new Line2D.Double(38 + lineLength, 2, 38 + lineLength,
				mHoleHeight + 9));
		g2.setStroke(new BasicStroke(NORMAL_STROKE_WIDTH));
		g2.setColor(NORMAL_COLOR);
	}

	@Override
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	@Override
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize()
	{
		// Make minimum width equal to a 4-hole flute.
		int numHoles = mHoles.length;
		if (numHoles < 4)
		{
			numHoles = 4;
		}
		int width = numHoles * mHoleLength + 45;
		int height = mHoleHeight + 14;
		return new Dimension(width, height);
	}

	@Override
	public void stateChanged(ChangeEvent event)
	{
		if (event.getSource() instanceof JRadioButton)
		{
			JRadioButton button = (JRadioButton) event.getSource();
			if (button.getModel().isPressed())
			{
				fireDataChangedEvent();
			}
		}
	}

	public void setEnableDataChanges(boolean enableChanges)
	{
		enableDataChanges = enableChanges;
		for (JRadioButton button : mHoles)
		{
			if (!enableDataChanges)
			{
				button.removeChangeListener(this);
			}
			else
			{
				button.addChangeListener(this);
			}
		}
	}

	private void fireDataChangedEvent()
	{
		if (enableDataChanges)
		{
			if (dataChangedListeners != null)
			{
				for (DataChangedListener listener : dataChangedListeners)
				{
					listener.dataChanged(new DataChangedEvent(this));
				}
			}
		}
	}

	@Override
	public void addDataChangedListener(DataChangedListener listener)
	{
		if (dataChangedListeners == null)
		{
			dataChangedListeners = new ArrayList<DataChangedListener>();
		}

		dataChangedListeners.add(listener);
	}

	@Override
	public void removeDataChangedListener(DataChangedListener listener)
	{
		if (dataChangedListeners != null)
		{
			dataChangedListeners.remove(listener);
		}
	}

}
