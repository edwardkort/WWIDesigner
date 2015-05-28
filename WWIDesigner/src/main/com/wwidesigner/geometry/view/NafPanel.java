package com.wwidesigner.geometry.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.wwidesigner.geometry.Mouthpiece;

public class NafPanel extends InstrumentPanel
{
	@Override
	protected void layoutWidgets()
	{
		setLayout(new GridBagLayout());
		setNameWidget(0, 0, 1);
		setDescriptionWidget(0, 1, 1);
		setLengthTypeWidget(0, 2, 1);
		setMouthpieceWidget(1, 0, 3);
		setTerminationWidget(1, 2, 1);
		setHoleTableWidget(0, 3, GridBagConstraints.REMAINDER);
		setBoreTableWidget(1, 3, 1);
	}

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

		label = new JLabel("Splitting-edge Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		panel.add(label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		panel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		gbc.gridwidth = 1;
		++gbc.gridy;
		label = new JLabel("TSH Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowLength, gbc);

		++gbc.gridy;
		label = new JLabel("TSH Width: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowWidth, gbc);

		++gbc.gridy;
		label = new JLabel("Flue Depth: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windwayHeight, gbc);

		++gbc.gridy;
		label = new JLabel("Fipple Factor: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(fippleFactor, gbc);

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
		if (value == null || value <= 0.0)
		{
			// Value is required. Assume zero, which fails validity checking.
			value = 0.0;
		}
		fipple.setWindowLength(value);
		value = (Double) windowWidth.getValue();
		if (value == null || value <= 0.0)
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
		if (value == null || value <= 0.0)
		{
			// For NAF model, a value is required. Assume zero, which fails
			// validity checking.
			value = 0.0;
		}
		fipple.setWindwayHeight(value);
		value = (Double) fippleFactor.getValue();
		if (value == null || value <= 0.0)
		{
			// For NAF model, a value is required. Assume zero, which fails
			// validity checking.
			value = 0.0;
		}
		fipple.setFippleFactor(value);
		mouthpiece.setFipple(fipple);
		mouthpiece.setEmbouchureHole(null);
		value = (Double) beta.getValue();
		mouthpiece.setBeta(value);

		return mouthpiece;
	}

}
