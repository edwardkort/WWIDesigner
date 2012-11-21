package com.wwidesigner.gui.util;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * A JButton containing multiple lines of centered text (the actual display size
 * of the button is under the control of the parent container's layout manager.
 * The action command for the button is set to the first line, so if you intend
 * to create a set of these buttons with a single actionListener, make the first
 * lines unique. No attempt is make to evenly size the lines - each element of
 * the String[] is a line.
 */
public class MultiLineButton extends JButton
{
	/**
	 * The only constructor.
	 * 
	 * @param line
	 *            An array of lines; each element will be a centered line within
	 *            the button. line[0] will be the actionCommand.
	 */
	public MultiLineButton(String[] line, int commandIndex)
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		JLabel label;
		if (line != null)
		{
			// Set actionCommand.
			if (commandIndex >= 0 && commandIndex < line.length
					&& line[commandIndex] != null)
			{
				setActionCommand(line[commandIndex]);
			}
			for (int i = 0; i < line.length; i++)
			{
				label = new JLabel(line[i]);
				label.setForeground(Color.black);
				add(label, gbc);
			}
		}
	}

	/**
	 * Overrides JButton.setEnabled() in order to change text color.
	 */
	public void setEnabled(boolean enabled)
	{
		Color textColor;

		if (enabled)
		{
			textColor = Color.black;
		}
		else
		{
			textColor = Color.gray;
		}

		super.setEnabled(enabled);

		for (int i = 0; i < getComponentCount(); i++)
		{
			getComponent(i).setForeground(textColor);
		}
	}
}
