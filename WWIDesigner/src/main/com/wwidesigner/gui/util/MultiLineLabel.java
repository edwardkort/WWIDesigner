package com.wwidesigner.gui.util;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/** A multi-line label, text is centered. */
public class MultiLineLabel extends JPanel
{

	/**
	 * The only constructor.
	 * 
	 * @param line
	 *            Array of lines. This class makes no attempt to even out the
	 *            lines - the arrangement is explicit in the array.
	 */
	public MultiLineLabel(String[] line)
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		int column = 0;
		for (String thisLine : line)
		{
			JLabel label = new JLabel();
			label.setText(thisLine);
			label.setFont(getFont().deriveFont(Font.BOLD));
			gbc.gridy = column++;
			add(label, gbc);
		}
	}
}
