package com.wwidesigner.geometry.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.calculation.HemisphericalBoreHead;
import com.wwidesigner.util.SortedPositionList;

public class NafPanel extends InstrumentPanel
{
	@Override
	public void loadData(Instrument instrument)
	{
		if (instrument != null && instrument.getMouthpiece() != null
				&& instrument.getMouthpiece().getFipple() == null)
		{
			// Use default mouthpiece if this is not a fipple mouthpiece.
			super.layoutMouthpieceComponents();
		}
		super.loadData(instrument);
	}

	@Override
	protected void configureWidgets()
	{
		HOLE_TABLE_WIDTH = 400;
	}

	@Override
	protected void layoutMouthpieceComponents()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label;
		mouthpiecePanel.removeAll();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;

		label = new JLabel("Splitting-edge Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		mouthpiecePanel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		gbc.gridwidth = 1;
		++gbc.gridy;
		label = new JLabel("TSH Length: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windowLength, gbc);

		++gbc.gridy;
		label = new JLabel("TSH Width: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windowWidth, gbc);

		++gbc.gridy;
		label = new JLabel("Flue Depth: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(windwayHeight, gbc);

		++gbc.gridy;
		label = new JLabel("Fipple Factor: ");
		gbc.gridx = 0;
		mouthpiecePanel.add(label, gbc);
		gbc.gridx = 1;
		mouthpiecePanel.add(fippleFactor, gbc);
	}

	@Override
	protected Mouthpiece getMouthpiece()
	{
		if (!fippleButton.isSelected())
		{
			return super.getMouthpiece();
		}
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

	@Override
	protected JPanel createBoreButtons()
	{
		JPanel buttonPanel = super.createBoreButtons();
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BorderLayout());
		newPanel.add(buttonPanel, BorderLayout.NORTH);

		JButton button = new JButton("Create hemi head");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				addHemiHead(boreList);
			}

		});
		newPanel.add(button, BorderLayout.SOUTH);

		return newPanel;
	}

	protected void addHemiHead(JideTable aBoreList)
	{
		List<BorePoint> borePoints = getBoreTableData();
		SortedPositionList<BorePoint> sortedPoints = new SortedPositionList<BorePoint>(
				borePoints);
		BorePoint topPoint = sortedPoints.getFirst();
		BorePoint hemiTopPoint = HemisphericalBoreHead
				.getHemiTopPoint(sortedPoints.toArray(new BorePoint[0]));
		if (topPoint != null && hemiTopPoint != null)
		{
			double origin = topPoint.getBorePosition();
			double headDiameter = hemiTopPoint.getBoreDiameter();
			if (!Double.isNaN(origin) && !Double.isNaN(headDiameter))
			{
				List<BorePoint> newPoints = new ArrayList<BorePoint>();
				HemisphericalBoreHead.addHemiHead(origin, headDiameter,
						newPoints);
				double hemiPosition = hemiTopPoint.getBorePosition();
				for (BorePoint point : sortedPoints)
				{
					if (point.getBorePosition() > hemiPosition)
					{
						newPoints.add(point);
					}
				}
				DefaultTableModel model = (DefaultTableModel) aBoreList
						.getModel();
				int lastRow = model.getRowCount() - 1;
				for (int i = lastRow; i >= 0; i--)
				{
					model.removeRow(i);
				}
				for (BorePoint point : newPoints)
				{
					model.addRow(new Object[] { point.getName(),
							point.getBorePosition(), point.getBoreDiameter() });
				}
			}
		}
	}

}
