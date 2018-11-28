/**
 * 
 */
package com.wwidesigner.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.DialogResponse;
import com.jidesoft.app.framework.gui.GUIApplication;
import com.jidesoft.app.framework.gui.PreferencesPane;
import com.wwidesigner.gui.util.DirectoryChooserPanel;
import com.wwidesigner.util.view.LengthTypeComboBox;

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
	public static final String FLUTE_STUDY_NAME = "FluteStudy";
	public static final String REED_STUDY_NAME = "ReedStudy";

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

	public static final String CONSTRAINTS_DIRECTORY = "Constraints directory";
	public static final String DEFAULT_CONSTRAINTS_DIRECTORY = "";

	public static final String OPTIMIZER_TYPE_OPT = "OptimizerType";
	public static final String OPT_DEFAULT_NAME = "Default";
	public static final String OPT_DIRECT_NAME = "DIRECT";

	public static final String LENGTH_TYPE_OPT = "Length Type";
	public static final String LENGTH_TYPE_DEFAULT = "IN";

	public static final String NUMBER_OF_STARTS = "NumStarts";
	public static final int DEFAULT_NUMBER_OF_STARTS = 30;

	public static final String WARN_ON_DIRTY_CLOSE_OPT = "Warn on dirty close";
	public static final boolean WARN_ON_DIRTY_CLOSE_DEFAULT = false;

	public static final String MAX_NOTE_FREQ_MULT = "Max Note Spectrum freq (multiplier)";
	public static final double DEFAULT_NOTE_FREQ_MULT = 3.17d;

	JRadioButton nafButton;
	JRadioButton whistleButton;
	JRadioButton fluteButton;
	JRadioButton reedButton;
	ButtonGroup studyGroup;

	JSpinner blowingLevelSpinner;
	SpinnerNumberModel blowingLevel;

	NumberFormat floatFormat;

	JFormattedTextField temperatureField;
	JFormattedTextField pressureField;
	JFormattedTextField humidityField;
	JFormattedTextField co2Field;
	JFormattedTextField noteFreqMultField;
	DirectoryChooserPanel constraintsDirChooser;

	LengthTypeComboBox lengthTypeComboBox;
	JCheckBox optimizerBox;

	JSpinner numStartsSpinner;
	SpinnerNumberModel numStarts;

	JTextField generalMessageField;
	JTextField whistleMessageField;

	OptionsTabView optionsPane;

	@Override
	protected void initializeComponents(DialogRequest request)
	{
		// Setup dialog components.

		nafButton = new JRadioButton("NAF Study");
		whistleButton = new JRadioButton("Whistle Study");
		fluteButton = new JRadioButton("Flute Study");
		reedButton = new JRadioButton("Reed Study");
		studyGroup = new ButtonGroup();
		studyGroup.add(nafButton);
		studyGroup.add(whistleButton);
		studyGroup.add(fluteButton);
		studyGroup.add(reedButton);

		blowingLevel = new SpinnerNumberModel(DEFAULT_BLOWING_LEVEL, 0, 10, 1);
		blowingLevelSpinner = new JSpinner(blowingLevel);
		blowingLevelSpinner.setName("Blowing Level");
		lengthTypeComboBox = new LengthTypeComboBox();
		optimizerBox = new JCheckBox();

		floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(1);
		temperatureField = new JFormattedTextField(floatFormat);
		pressureField = new JFormattedTextField(floatFormat);
		humidityField = new JFormattedTextField();
		co2Field = new JFormattedTextField();
		noteFreqMultField = new JFormattedTextField(floatFormat);
		temperatureField.setColumns(5);
		pressureField.setColumns(5);
		humidityField.setColumns(5);
		co2Field.setColumns(5);
		noteFreqMultField.setColumns(5);

		numStarts = new SpinnerNumberModel(DEFAULT_NUMBER_OF_STARTS, 1, 50, 1);
		numStartsSpinner = new JSpinner(numStarts);
		numStartsSpinner.setName("Number of Starts");

		constraintsDirChooser = new DirectoryChooserPanel();

		generalMessageField = new JTextField(40);
		generalMessageField.setEditable(false);
		generalMessageField.setText("");
		whistleMessageField = new JTextField(40);
		whistleMessageField.setEditable(false);
		whistleMessageField.setText("");

		optionsPane = new OptionsTabView();
		add(optionsPane);

		setDefaultStudy();
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
			fluteButton.setSelected(false);
			reedButton.setSelected(false);
		}
		else if (modelName.contentEquals(FLUTE_STUDY_NAME))
		{
			fluteButton.setSelected(true);
			whistleButton.setSelected(false);
			nafButton.setSelected(false);
			reedButton.setSelected(false);
		}
		else if (modelName.contentEquals(REED_STUDY_NAME))
		{
			fluteButton.setSelected(false);
			whistleButton.setSelected(false);
			nafButton.setSelected(false);
			reedButton.setSelected(true);
		}
		else
		{
			whistleButton.setSelected(true);
			nafButton.setSelected(false);
			fluteButton.setSelected(false);
			reedButton.setSelected(false);
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
		Number currentMaxNoteFreqMult = myPreferences
				.getDouble(MAX_NOTE_FREQ_MULT, DEFAULT_NOTE_FREQ_MULT);
		noteFreqMultField.setValue(currentMaxNoteFreqMult);

		String constraintsPath = myPreferences.get(CONSTRAINTS_DIRECTORY,
				DEFAULT_CONSTRAINTS_DIRECTORY);
		constraintsDirChooser.setSelectedDirectory(constraintsPath);

		String dimensionType = myPreferences.get(LENGTH_TYPE_OPT,
				LENGTH_TYPE_DEFAULT);
		lengthTypeComboBox.setSelectedLengthType(dimensionType);
		String optimizerName = myPreferences.get(OPTIMIZER_TYPE_OPT,
				OPT_DEFAULT_NAME);
		optimizerBox.setSelected(optimizerName.equals(OPT_DIRECT_NAME));

		Number currentNumStarts = myPreferences.getInt(NUMBER_OF_STARTS,
				DEFAULT_NUMBER_OF_STARTS);
		numStarts.setValue(currentNumStarts);
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
		else if (fluteButton.isSelected())
		{
			studyName = FLUTE_STUDY_NAME;
		}
		else if (reedButton.isSelected())
		{
			studyName = REED_STUDY_NAME;
		}
		else
		{
			studyName = WHISTLE_STUDY_NAME;
		}
		if (optimizerBox.isSelected())
		{
			optimizerName = OPT_DIRECT_NAME;
		}

		// Update the preferences, and re-set the view's study model.

		myPreferences.put(STUDY_MODEL_OPT, studyName);
		myPreferences.putInt(BLOWING_LEVEL_OPT,
				blowingLevel.getNumber().intValue());
		myPreferences.putDouble(TEMPERATURE_OPT,
				((Number) temperatureField.getValue()).doubleValue());
		myPreferences.putDouble(PRESSURE_OPT,
				((Number) pressureField.getValue()).doubleValue());
		myPreferences.putInt(HUMIDITY_OPT,
				((Number) humidityField.getValue()).intValue());
		myPreferences.putInt(CO2_FRACTION_OPT,
				((Number) co2Field.getValue()).intValue());
		myPreferences.put(OPTIMIZER_TYPE_OPT, optimizerName);
		myPreferences.putDouble(MAX_NOTE_FREQ_MULT,
				((Number) noteFreqMultField.getValue()).doubleValue());

		myPreferences.put(CONSTRAINTS_DIRECTORY,
				constraintsDirChooser.getSelectedDirectory());

		String dimensionType = lengthTypeComboBox.getSelectedLengthTypeName();
		myPreferences.put(LENGTH_TYPE_OPT, dimensionType);

		myPreferences.putInt(NUMBER_OF_STARTS,
				numStarts.getNumber().intValue());

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
				// Because some of these preference changes might impact Actions
				// in the UI ...
				studyView.updateActions();
			}
		}
	}

	@Override
	protected void validateComponents(DialogRequest request,
			DialogResponse response) throws ApplicationVetoException
	{
		// Validate the preferences.
		generalMessageField.setText("");
		generalMessageField.setForeground(Color.BLACK);
		whistleMessageField.setText("");
		whistleMessageField.setForeground(Color.BLACK);

		double currentTemperature = ((Number) temperatureField.getValue())
				.doubleValue();
		if (currentTemperature < 0 || currentTemperature > 50)
		{
			generalMessageField
					.setText("Temperature must be between 0 and 50 C.");
			generalMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		double currentPressure = ((Number) pressureField.getValue())
				.doubleValue();
		if (currentPressure < 10 || currentPressure > 150)
		{
			whistleMessageField
					.setText("Pressure must be between 10 and 150 kPa.");
			whistleMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		int currentHumidity = ((Number) humidityField.getValue()).intValue();
		if (currentHumidity < 0 || currentHumidity > 100)
		{
			generalMessageField.setText("Humidity must be between 0 and 100%.");
			generalMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		int currentCO2 = ((Number) co2Field.getValue()).intValue();
		if (currentCO2 < 0 || currentCO2 > 100000)
		{
			whistleMessageField
					.setText("CO2 must be between 0 and 100,000 ppm.");
			whistleMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		double currentMaxNoteFreqMult = ((Number) noteFreqMultField.getValue())
				.doubleValue();
		if (currentMaxNoteFreqMult < 1.d || currentMaxNoteFreqMult > 100.d)
		{
			generalMessageField.setText(
					"Maximum note frequency multiplier must be between 1 and 100.");
			generalMessageField.setForeground(Color.RED);
			throw new ApplicationVetoException();
		}
		if (nafButton.isSelected())
		{
			String constraintsPath = constraintsDirChooser
					.getSelectedDirectory();
			// Allow an empty or blank path
			if (constraintsPath != null && constraintsPath.trim().length() > 0)
			{
				try
				{
					File directory = new File(constraintsPath);
					if (!directory.exists() || !directory.isDirectory()
							|| !directory.canRead() || !directory.canWrite())
					{
						throw new Exception();
					}
				}
				catch (Exception e)
				{
					generalMessageField.setText(
							"Constraints directory location is not valid");
					generalMessageField.setForeground(Color.RED);
					throw new ApplicationVetoException();
				}
			}
		}
	}

	/**
	 * Convenience method so that the application can be distributed with any of
	 * the studies as the initial default.
	 */
	protected void setDefaultStudy()
	{
		nafButton.setSelected(true);
	}

	class OptionsTabView extends JTabbedPane
	{
		OptionsTabView()
		{
			add(new StudyPanel(), "General Study Options");
			add(new NafPanel(), "NAF Study Options");
			add(new WhistlePanel(), "Whistle/Flute Study Options");
		}
	}

	class StudyPanel extends JPanel
	{
		StudyPanel()
		{
			setLayout(new GridBagLayout());

			JPanel topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			JPanel studyButtonPanel = new JPanel(new GridLayout(1, 4));
			studyButtonPanel.add(nafButton);
			studyButtonPanel.add(whistleButton);
			studyButtonPanel.add(fluteButton);
			studyButtonPanel.add(reedButton);
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 4;
			topPanel.add(studyButtonPanel, gbc);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(10, 0, 0, 0);
			topPanel.add(new JLabel(LENGTH_TYPE_OPT + ": "), gbc);
			gbc.gridx = 1;
			topPanel.add(lengthTypeComboBox, gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.insets = new Insets(0, 0, 0, 0);
			topPanel.add(new JLabel("Temperature, C: "), gbc);
			gbc.gridx = 1;
			topPanel.add(temperatureField, gbc);
			gbc.gridx = 2;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(10, 20, 0, 0);
			topPanel.add(new JLabel("Use DIRECT optimizer (slow & thorough): "),
					gbc);
			gbc.gridx = 3;
			gbc.insets = new Insets(10, 0, 0, 0);
			topPanel.add(optimizerBox, gbc);
			gbc.gridwidth = 1;
			gbc.gridx = 2;
			gbc.gridy = 2;
			gbc.insets = new Insets(0, 20, 0, 0);
			topPanel.add(new JLabel(MAX_NOTE_FREQ_MULT + ": "), gbc);
			gbc.gridx = 3;
			gbc.insets = new Insets(0, 0, 0, 0);
			topPanel.add(noteFreqMultField, gbc);
			gbc.gridwidth = 1;
			gbc.gridx = 0;
			gbc.gridy = 3;
			topPanel.add(new JLabel("Relative Humidity, %: "), gbc);
			gbc.gridx = 1;
			topPanel.add(humidityField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			add(topPanel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(10, 0, 0, 0);
			add(new JLabel(CONSTRAINTS_DIRECTORY + ": "), gbc);
			gbc.gridy = 2;
			gbc.insets = new Insets(0, 0, 0, 0);
			gbc.anchor = GridBagConstraints.CENTER;
			add(constraintsDirChooser, gbc);
			gbc.gridx = 0;
			gbc.gridy = 3;
			gbc.insets = new Insets(5, 0, 5, 0);
			add(generalMessageField, gbc);
		}
	}

	class NafPanel extends JPanel
	{
		NafPanel()
		{
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			add(new JLabel("Number of starts for multi-Start optimizations: "),
					gbc);
			gbc.gridx = 1;
			add(numStartsSpinner, gbc);
		}
	}

	class WhistlePanel extends JPanel
	{
		WhistlePanel()
		{
			setLayout(new GridBagLayout());

			JPanel topPanel = new JPanel();
			topPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.insets = new Insets(5, 0, 3, 0);
			topPanel.add(new JLabel("Blowing Level: "), gbc);
			gbc.gridx = 1;
			topPanel.add(blowingLevelSpinner, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			topPanel.add(new JLabel("Pressure, kPa: "), gbc);
			gbc.gridx = 1;
			topPanel.add(pressureField, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			topPanel.add(new JLabel("CO2, ppm: "), gbc);
			gbc.gridx = 1;
			topPanel.add(co2Field, gbc);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			add(topPanel, gbc);

			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.insets = new Insets(10, 0, 5, 0);
			add(whistleMessageField, gbc);
		}
	}

}
