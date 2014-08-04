package com.wwidesigner.gui;

import java.awt.Color;
import java.util.prefs.Preferences;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.DialogResponse;
import com.jidesoft.app.framework.gui.GUIApplication;

// Functionality moved into OptimizationPreferences.
@Deprecated
public class OptimizationPreferencesWithTempConstraint extends
		OptimizationPreferences
{
	public static final String MIN_TOP_HOLE_RATIO_OPT = "Minimum top-hole position (ratio to bore length)";
	public static final double DEFAULT_MIN_TOP_HOLE_RATIO = 0.25;

	JFormattedTextField topHoleRatioField;

	@Override
	protected void initializeComponents(DialogRequest request)
	{
		super.initializeComponents(request);

		floatFormat.setMinimumFractionDigits(1);
		topHoleRatioField = new JFormattedTextField(floatFormat);
		topHoleRatioField.setColumns(5);
		JPanel panel = new JPanel();
		panel.add(new JLabel(MIN_TOP_HOLE_RATIO_OPT + ": "));
		panel.add(topHoleRatioField);
		add(panel);
	}

	@Override
	protected void updateComponents(DialogRequest request)
	{
		super.updateComponents(request);
		if (nafButton.isSelected())
		{
			Preferences myPreferences = getApplication().getPreferences();
			Number currentRatio = myPreferences.getDouble(
					MIN_TOP_HOLE_RATIO_OPT, DEFAULT_MIN_TOP_HOLE_RATIO);
			topHoleRatioField.setValue(currentRatio);
		}
	}

	@Override
	protected void updatePreferences(GUIApplication application)
	{
		if (nafButton.isSelected())
		{
			Preferences myPreferences = application.getPreferences();
			myPreferences.putDouble(MIN_TOP_HOLE_RATIO_OPT,
					((Number) topHoleRatioField.getValue()).doubleValue());
		}
		super.updatePreferences(application);
	}

	@Override
	protected void validateComponents(DialogRequest request,
			DialogResponse response) throws ApplicationVetoException
	{
		if (nafButton.isSelected())
		{
			double currentRatio = -1.0;
			if (topHoleRatioField.getValue() != null)
			{
				currentRatio = ((Number) topHoleRatioField.getValue())
					.doubleValue();
			}
			if (currentRatio < 0 || currentRatio > 1)
			{
				messageField
						.setText("Top hole position ratio must be between 0 and 1");
				messageField.setForeground(Color.RED);
				throw new ApplicationVetoException();
			}
		}
	}
}
