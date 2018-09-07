package com.wwidesigner.geometry.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.plaf.UIDefaultsLookup;

public class HoleGroupSpacingDialog extends StandardDialog
{
	protected HoleGroupSpacingComponent spacingWidget;
	protected int numberOfHoles;
	protected int[][] holeGroups = null;

	public HoleGroupSpacingDialog(Frame parentFrame, int aNumberOfHoles)
	{
		super(parentFrame, "Hole-spacing Groups", true);
		setLocationRelativeTo(parentFrame);
		this.numberOfHoles = aNumberOfHoles;
	}

	@Override
	public JComponent createBannerPanel()
	{
		BannerPanel headerPanel = new BannerPanel(
				"Hole-spacing groups definition",
				"A hole-spacing group is a set of contiguous holes "
						+ "with equal inter-hole spacing. Select or deselect "
						+ "the separators between each hole to define the "
						+ "boundaries of the hole-spacing groups.");
		headerPanel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		headerPanel.setPreferredSize(new Dimension(350, 100));
		return headerPanel;
	}

	@Override
	public JComponent createContentPanel()
	{
		spacingWidget = new HoleGroupSpacingComponent(numberOfHoles);

		JPanel panel = new JPanel(new FlowLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(spacingWidget);
		
		return panel;
	}

	@Override
	public ButtonPanel createButtonPanel()
	{
		ButtonPanel buttons = createOKCancelButtonPanel();
		JButton okButton = (JButton) buttons.getButtonByName(OK);
		okButton.setAction(new AbstractAction(UIDefaultsLookup
				.getString("OptionPane.okButtonText"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDialogResult(RESULT_AFFIRMED);
				setVisible(false);
				holeGroups = spacingWidget.getHoleGroups();
				dispose();
			}
		});

        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return buttons;
	}

	public int[][] getHoleSpacingGroups()
	{
		return holeGroups;
	}

}
