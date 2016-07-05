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
import java.awt.Insets;

import javax.swing.JLabel;

import com.wwidesigner.geometry.Instrument;

// Requires JRE 8u60 or higher; otherwise, attempting to TAB out of the
// mouthpiece type buttons causes Swing to throw an IllegalArgumentException.

public class ReedInstrumentPanel extends InstrumentPanel
{
	@Override
	public void loadData(Instrument instrument)
	{
		if (instrument != null && instrument.getMouthpiece() != null
				&& instrument.getMouthpiece().getSingleReed() == null
				&& instrument.getMouthpiece().getDoubleReed() == null)
		{
			// Use default mouthpiece if this is not a cane reed mouthpiece.
			super.layoutMouthpieceComponents();
		}
		super.loadData(instrument);
	}

	@Override
	protected void layoutMouthpieceComponents()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label;
		mouthpiecePanel.removeAll();
		mouthpieceTypeGroup.remove(fippleButton);
		mouthpieceTypeGroup.remove(embouchureHoleButton);
		mouthpieceTypeGroup.remove(lipReedButton);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;

		label = new JLabel("Mouthpiece Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		mouthpiecePanel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(10, 0, 0, 0);

		label = new JLabel("Beta Factor: ");
		gbc.gridx = 2;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 3;
		mouthpiecePanel.add(beta, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		mouthpiecePanel.add(singleReedButton, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(doubleReedButton, gbc);

		gbc.gridwidth = 1;
		++gbc.gridy;
		label = new JLabel("Alpha: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(alpha, gbc);

		++gbc.gridy;
		label = new JLabel("Crow Freq: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(crowFreq, gbc);
	}

}
