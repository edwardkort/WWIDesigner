/**
 * InstrumentPanel customized to display an instrument for whistle study model.
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
package com.wwidesigner.geometry.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.wwidesigner.geometry.Mouthpiece;

public class WhistleInstrumentPanel extends InstrumentPanel
{
	@Override
	protected void layoutMouthpieceComponents(int gridx, int gridy,
			int gridheight)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label;
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;

		label = new JLabel("Mouthpiece Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		panel.add(label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		panel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(10, 0, 0, 0);

		label = new JLabel("Beta Factor: ");
		gbc.gridx = 2;
		panel.add(label, gbc);
		gbc.gridx = 3;
		panel.add(beta, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		gbc.gridwidth = 1;
		++gbc.gridy;
		label = new JLabel("Window Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowLength, gbc);

		++gbc.gridy;
		label = new JLabel("Window Width: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowWidth, gbc);

		++gbc.gridy;
		label = new JLabel("Window Height: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowHeight, gbc);

		++gbc.gridy;
		label = new JLabel("Windway Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windwayLength, gbc);

		++gbc.gridy;
		label = new JLabel("Windway Height: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windwayHeight, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	@Override
	protected Mouthpiece getMouthpiece()
	{
		Mouthpiece mouthpiece = new Mouthpiece();
		Double value;
		value = (Double) mouthpiecePosition.getValue();
		if (value == null)
		{
			// Position is required. Assume zero.
			value = 0.0;
		}
		mouthpiece.setPosition(value);

		Mouthpiece.Fipple fipple = new Mouthpiece.Fipple();
		value = (Double) windowLength.getValue();
		if (value == null)
		{
			// Value is required. Assume zero, which fails validity checking.
			value = 0.0;
		}
		fipple.setWindowLength(value);
		value = (Double) windowWidth.getValue();
		if (value == null)
		{
			// Value is required. Assume zero, which fails validity checking.
			value = 0.0;
		}
		fipple.setWindowWidth(value);
		value = (Double) windowHeight.getValue();
		fipple.setWindowHeight(value);
		value = (Double) windwayLength.getValue();
		fipple.setWindwayLength(value);
		value = (Double) windwayHeight.getValue();
		fipple.setWindwayHeight(value);
		mouthpiece.setFipple(fipple);
		mouthpiece.setEmbouchureHole(null);
		value = (Double) beta.getValue();
		mouthpiece.setBeta(value);
		return mouthpiece;
	}

}
