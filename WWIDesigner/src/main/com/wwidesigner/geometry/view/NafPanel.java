package com.wwidesigner.geometry.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.wwidesigner.geometry.Mouthpiece;

public class NafPanel extends InstrumentPanel
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
		gbc.gridy = gridy + 1;
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
			JOptionPane.showMessageDialog(this,
					"Splitting-edge position is required.");
			mouthpiecePosition.requestFocusInWindow();
			return null;
		}
		mouthpiece.setPosition(value);
		Mouthpiece.Fipple fipple = new Mouthpiece.Fipple();
		value = (Double) windowLength.getValue();
		if (value == null || value <= 0.0)
		{
			JOptionPane.showMessageDialog(this, "TSH length must be positive.");
			windowLength.requestFocusInWindow();
			return null;
		}
		fipple.setWindowLength(value);
		value = (Double) windowWidth.getValue();
		if (value == null || value <= 0.0)
		{
			JOptionPane.showMessageDialog(this, "TSH width must be positive.");
			windowWidth.requestFocusInWindow();
			return null;
		}
		fipple.setWindowWidth(value);
		value = (Double) windowHeight.getValue();
		fipple.setWindowHeight(value);
		value = (Double) windwayLength.getValue();
		fipple.setWindwayLength(value);
		value = (Double) windwayHeight.getValue();
		if (value == null || value <= 0.0)
		{
			JOptionPane
					.showMessageDialog(this, "Flue height must be positive.");
			windwayHeight.requestFocusInWindow();
			return null;
		}
		fipple.setWindwayHeight(value);
		value = (Double) fippleFactor.getValue();
		if (value == null || value <= 0.0)
		{
			JOptionPane.showMessageDialog(this,
					"Fipple factor must be positive.");
			fippleFactor.requestFocusInWindow();
			return null;
		}
		fipple.setFippleFactor(value);
		mouthpiece.setFipple(fipple);
		mouthpiece.setEmbouchureHole(null);
		value = (Double) beta.getValue();
		mouthpiece.setBeta(value);

		return mouthpiece;
	}

}
