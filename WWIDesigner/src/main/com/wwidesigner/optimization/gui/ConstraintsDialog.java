package com.wwidesigner.optimization.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.view.ConstraintsPanel;

public class ConstraintsDialog extends StandardDialog
{
	Constraints constraints;

	public ConstraintsDialog(Constraints constraints)
	{
		this.constraints = constraints;
		this.setSize(new Dimension(1000, 1000));
	}

	@Override
	public JComponent createBannerPanel()
	{

		return null;
	}

	@Override
	public JComponent createContentPanel()
	{
		ConstraintsPanel panel = new ConstraintsPanel(constraints);
		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
