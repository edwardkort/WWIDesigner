/**
 * 
 */
package com.wwidesigner.gui;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.DialogResponse;
import com.jidesoft.app.framework.gui.GUIApplication;
import com.jidesoft.app.framework.gui.PreferencesPane;
import com.wwidesigner.util.Constants.LengthType;

/**
 * Dialog pane to display/modify preferences for the optimization application.
 * 
 * @author Burton Patkau
 *
 */
public class OptimizationPreferences extends PreferencesPane
{
	public static final String STUDY_MODEL_OPT = "StudyModel";
	public static final String NAF_STUDY_NAME = "NafStudy";
	public static final String WHISTLE_STUDY_NAME = "WhistleStudy";

	public static final String BLOWING_LEVEL_OPT = "BlowingLevel";
	public static final int DEFAULT_BLOWING_LEVEL = 5;

	public static final String TEMPERATURE_OPT = "AirTemperature";
	public static final String PRESSURE_OPT = "AirPressure";
	public static final String HUMIDITY_OPT = "RelHumidity";
	public static final String CO2_FRACTION_OPT = "CO2Fraction";
	public static final double DEFAULT_TEMPERATURE = 20;
	public static final double DEFAULT_PRESSURE = 101.325;
	public static final int DEFAULT_HUMIDITY = 45;
	public static final int DEFAULT_CO2_FRACTION = 390;

	public static final String MIN_TOP_HOLE_RATIO_OPT = "Minimum top-hole position (ratio to bore length)";
	public static final double DEFAULT_MIN_TOP_HOLE_RATIO = 0.25;

	public static final int NrOptimizers = 6; // Including default
	public static final String OPTIMIZER_TYPE_OPT = "OptimizerType";
	public static final String OPT_DEFAULT_NAME = "Default";
	public static final String OPT_BOBYQA_NAME = "BOBYQA";
	public static final String OPT_CMAES_NAME = "CMAES";
	public static final String OPT_MULTISTART_NAME = "Multi-Start";
	public static final String OPT_SIMPLEX_NAME = "Simplex";
	public static final String OPT_POWELL_NAME = "Powell";

	public static final String LENGTH_TYPE_OPT = "Length Type";
	public static final String LENGTH_TYPE_DEFAULT = "IN";

	protected static String[] OptimizerName = new String[] { OPT_DEFAULT_NAME,
			OPT_BOBYQA_NAME, OPT_CMAES_NAME, OPT_MULTISTART_NAME,
			OPT_SIMPLEX_NAME, OPT_POWELL_NAME };

	JRadioButton nafButton;
	JRadioButton whistleButton;
	ButtonGroup studyGroup;

	JRadioButton[] optButton = new JRadioButton[NrOptimizers];
	ButtonGroup optimizerGroup;

	JSpinner blowingLevelSpinner;
	SpinnerNumberModel blowingLevel;

	NumberFormat floatFormat;

	JFormattedTextField temperatureField;
	JFormattedTextField pressureField;
	JFormattedTextField humidityField;
	JFormattedTextField co2Field;
	JFormattedTextField topHoleRatioField;

	JComboBox<LengthType> lengthTypeComboBox;

	JTextField messageField;

	@Override
	protected void initializeComponents(DialogRequest request)
	{
		// Setup dialog components.

		JPanel studyPanel = new JPanel();
		studyPanel.setLayout(new BoxLayout(studyPanel, BoxLayout.Y_AXIS));

		nafButton = new JRadioButton("NAF Study");
		nafButton.setSelected(true);
		whistleButton = new JRadioButton("Whistle Study");
		studyGroup = new ButtonGroup();
		studyGroup.add(nafButton);
		studyGroup.add(whistleButton);

		blowingLevel = new SpinnerNumberModel(DEFAULT_BLOWING_LEVEL, 0, 10, 1);
		blowingLevelSpinner = new JSpinner(blowingLevel);
		blowingLevelSpinner.setName("Blowing Level");
		lengthTypeComboBox = new JComboBox<LengthType>(LengthType.values());
		((JLabel)lengthTypeComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

		studyPanel.add(nafButton);
		studyPanel.add(whistleButton);
		studyPanel.add(new JLabel(" "));
		studyPanel.add(new JLabel(LENGTH_TYPE_OPT + ":"));
		studyPanel.add(lengthTypeComboBox);
		studyPanel.add(new JLabel(" "));
		studyPanel.add(new JLabel("Blowing Level:"));
		studyPanel.add(blowingLevelSpinner);
		studyPanel.add(new JLabel(" "));
		studyPanel.add(new JLabel(" "));

		JPanel airPanel = new JPanel();
		airPanel.setLayout(new BoxLayout(airPanel, BoxLayout.Y_AXIS));
		floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(1);
		temperatureField = new JFormattedTextField(floatFormat);
		pressureField = new JFormattedTextField(floatFormat);
		humidityField = new JFormattedTextField();
		co2Field = new JFormattedTextField();
		temperatureField.setColumns(5);
		pressureField.setColumns(5);
		humidityField.setColumns(5);
		co2Field.setColumns(5);
		JLabel temperatureLabel = new JLabel("Temperature, C:");
		JLabel pressureLabel = new JLabel("Pressure, kPa:");
		JLabel humidityLabel = new JLabel("Relative Humidity, %:");
		JLabel co2Label = new JLabel("CO2, ppm:");
		temperatureLabel.setLabelFor(temperatureField);
		pressureLabel.setLabelFor(pressureField);
		humidityLabel.setLabelFor(humidityField);
		co2Label.setLabelFor(co2Field);
		airPanel.add(temperatureLabel);
		airPanel.add(temperatureField);
		airPanel.add(pressureLabel);
		airPanel.add(pressureField);
		airPanel.add(humidityLabel);
		airPanel.add(humidityField);
		airPanel.add(co2Label);
		airPanel.add(co2Field);

		JPanel topHolePanel = new JPanel();
		floatFormat.setMinimumFractionDigits(1);
		topHoleRatioField = new JFormattedTextField(floatFormat);
		topHoleRatioField.setColumns(5);
		topHolePanel.add(new JLabel(MIN_TOP_HOLE_RATIO_OPT + ": "));
		topHolePanel.add(topHoleRatioField);

		messageField = new JTextField();
		messageField.setEditable(false);
		messageField.setText("");
		airPanel.add(messageField);

		JPanel optimizerPanel = new JPanel();
		optimizerPanel
				.setLayout(new BoxLayout(optimizerPanel, BoxLayout.Y_AXIS));
		optimizerPanel.add(new JLabel("Optimizer Type:"));

		optimizerGroup = new ButtonGroup();

		for (int i = 0; i < NrOptimizers; ++i)
		{
			optButton[i] = new JRadioButton(OptimizerName[i]);
			optimizerGroup.add(optButton[i]);
		}
		optButton[0].setSelected(true);

		// Add all but Powell optimizer to the optimizerPanel.
		for (int i = 0; i < NrOptimizers - 1; ++i)
		{
			optimizerPanel.add(optButton[i]);
		}

		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
		optionsPanel.add(studyPanel);
		optionsPanel.add(airPanel);
		optionsPanel.add(optimizerPanel);

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(optionsPanel);
		this.add(topHolePanel);
		this.add(messageField);
	}

	@Override
	protected void updateComponents(DialogRequest request)
	{
		// Set or re-set the value state of components.
		Preferences myPreferences = getApplication().getPreferences();
		String modelName = myPreferences.get(STUDY_MODEL_OPT, NAF_STUDY_NAME);
		if (modelName.contentEquals(NAF_STUDY_NAME))
		{
			nafButton.setSelected(true);
			whistleButton.setSelected(false);
		}
		else
		{
			whistleButton.setSelected(true);
			nafButton.setSelected(false);
		}
		Number currentLevel = myPreferences.getInt(BLOWING_LEVEL_OPT,
				DEFAULT_BLOWING_LEVEL);
		blowingLevel.setValue(currentLevel);
		Number currentTemperature = myPreferences.getDouble(TEMPERATURE_OPT,
				DEFAULT_TEMPERATURE);
		temperatureField.setValue(currentTemperature);
		Number currentPressure = myPreferences.getDouble(PRESSURE_OPT,
				DEFAULT_PRESSURE);
		pressureField.setValue(currentPressure);
		Number currentHumidity = myPreferences.getInt(HUMIDITY_OPT,
				DEFAULT_HUMIDITY);
		humidityField.setValue(currentHumidity);
		Number currentCO2 = myPreferences.getInt(CO2_FRACTION_OPT,
				DEFAULT_CO2_FRACTION);
		co2Field.setValue(currentCO2);

		String optimizerType = myPreferences.get(OPTIMIZER_TYPE_OPT,
				OPT_DEFAULT_NAME);

		// Ensure first button, Default, is selected if optimizer name not
		// found.
		optButton[0].setSelected(true);
		for (int i = 1; i < NrOptimizers; ++i)
		{
			if (optimizerType.contentEquals(OptimizerName[i]))
			{
				optButton[0].setSelected(false);
				optButton[i].setSelected(true);
			}
			else
			{
				optButton[i].setSelected(false);
			}
		}
		if (nafButton.isSelected())
		{
			Number currentRatio = myPreferences.getDouble(
					MIN_TOP_HOLE_RATIO_OPT, DEFAULT_MIN_TOP_HOLE_RATIO);
			topHoleRatioField.setValue(currentRatio);
		}
		else
		{
			topHoleRatioField.setValue(DEFAULT_MIN_TOP_HOLE_RATIO);
		}
		
		String dimensionType = myPreferences.get(LENGTH_TYPE_OPT, LENGTH_TYPE_DEFAULT);
		lengthTypeComboBox.setSelectedItem(LengthType.valueOf(dimensionType));
	}

	@Override
	protected void updatePreferences(GUIApplication application)
	{
		// Update the application preferences, from the dialog values.
		Preferences myPreferences = application.getPreferences();
		String priorStudyName = myPreferences.get(STUDY_MODEL_OPT, "");
		String studyName;
		String optimizerName = OPT_DEFAULT_NAME;

		if (nafButton.isSelected())
		{
			studyName = NAF_STUDY_NAME;
		}
		else
		{
			studyName = WHISTLE_STUDY_NAME;
		}

		for (int i = 0; i < NrOptimizers; ++i)
		{
			if (optButton[i].isSelected())
			{
				optimizerName = OptimizerName[i];
			}
		}

		// Update the preferences, and re-set the view's study model.

		myPreferences.put(STUDY_MODEL_OPT, studyName);
		myPreferences.putInt(BLOWING_LEVEL_OPT, blowingLevel.getNumber()
				.intValue());
		myPreferences.putDouble(TEMPERATURE_OPT,
				((Number) temperatureField.getValue()).doubleValue());
		myPreferences.putDouble(PRESSURE_OPT,
				((Number) pressureField.getValue()).doubleValue());
		myPreferences.putInt(HUMIDITY_OPT,
				((Number) humidityField.getValue()).intValue());
		myPreferences.putInt(CO2_FRACTION_OPT,
				((Number) co2Field.getValue()).intValue());
		myPreferences.put(OPTIMIZER_TYPE_OPT, optimizerName);

		if (nafButton.isSelected())
		{
			myPreferences.putDouble(MIN_TOP_HOLE_RATIO_OPT,
					((Number) topHoleRatioField.getValue()).doubleValue());
		}
		else
		{
			myPreferences.putDouble(MIN_TOP_HOLE_RATIO_OPT,
					DEFAULT_MIN_TOP_HOLE_RATIO);
		}

		LengthType dimensionType = (LengthType)lengthTypeComboBox.getSelectedItem();
		myPreferences.put(LENGTH_TYPE_OPT, dimensionType.name());

		DataView[] views = application.getFocusedViews();
		for (DataView view : views)
		{
			if (view instanceof StudyView)
			{
				StudyView studyView = (StudyView) view;
				if (!studyName.contentEquals(priorStudyName))
				{
					// Study model name has changed.
					studyView.setStudyModel(studyName);
				}
				studyView.getStudyModel().setPreferences(myPreferences);
			}
		}
	}

	@Override
	protected void validateComponents(DialogRequest request,
			DialogResponse response) throws ApplicationVetoException
	{
		// Validate the preferences.
		messageField.setText("");
		messageField.setForeground(Color.BLACK);

		double currentTemperature = ((Number) temperatureField.getValue())
				.doubleValue();
		if (currentTemperature < 0 || currentTemperature > 50)
		{
			messageField.setText("Temperature must be between 0 and 50 C.");
			messageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		double currentPressure = ((Number) pressureField.getValue())
				.doubleValue();
		if (currentPressure < 10 || currentPressure > 150)
		{
			messageField.setText("Pressure must be between 10 and 150 kPa.");
			messageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		int currentHumidity = ((Number) humidityField.getValue()).intValue();
		if (currentHumidity < 0 || currentHumidity > 100)
		{
			messageField.setText("Humidity must be between 0 and 100%.");
			messageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		int currentCO2 = ((Number) co2Field.getValue()).intValue();
		if (currentCO2 < 0 || currentCO2 > 100000)
		{
			messageField.setText("CO2 must be between 0 and 100,000 ppm.");
			messageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
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
