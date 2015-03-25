package com.wwidesigner.geometry.view;

import java.awt.Dimension;

import javax.swing.JToggleButton;

public class HoleGroupSeparator extends JToggleButton
{
	public HoleGroupSeparator()
	{
		setFocusPainted(false);
		setRolloverEnabled(false);
		setFocusable(false);
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(10, 22);
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
	public Dimension getSize()
	{
		return getPreferredSize();
	}

}
