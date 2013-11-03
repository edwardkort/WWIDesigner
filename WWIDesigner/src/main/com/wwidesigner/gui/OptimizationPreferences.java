/**
 * 
 */
package com.wwidesigner.gui;

import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.DialogResponse;
import com.jidesoft.app.framework.gui.GUIApplication;
import com.jidesoft.app.framework.gui.PreferencesPane;

/**
 * Dialog pane to display/modify preferences for the optimization application.
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
	
	public static final String OPTIMIZER_TYPE_OPT = "OptimizerType";
	public static final String OPT_DEFAULT_NAME = "Default";
	public static final String OPT_BOBYQA_NAME = "BOBYQA";
	public static final String OPT_CMAES_NAME = "CMAES";
	public static final String OPT_SIMPLEX_NAME = "Simplex";
	public static final String OPT_POWELL_NAME = "Powell";

	JRadioButton nafButton;
	JRadioButton whistleButton;
	ButtonGroup studyGroup;
	
	JRadioButton defaultOptButton;
	JRadioButton bobyqaOptButton;
	JRadioButton cmaesOptButton;
	JRadioButton simplexOptButton;
	JRadioButton powellOptButton;
	ButtonGroup optimizerGroup;

	JSpinner blowingLevelSpinner;
	SpinnerNumberModel blowingLevel;

	@Override
	protected void initializeComponents(DialogRequest request) {
		// Setup dialog components.
		nafButton = new JRadioButton("NAF Study");
		nafButton.setSelected(true);
		whistleButton = new JRadioButton("Whistle Study");
		studyGroup = new ButtonGroup();
		studyGroup.add(nafButton);
		studyGroup.add(whistleButton);

		JPanel studyPanel = new JPanel();
		studyPanel.setLayout(new BoxLayout(studyPanel,BoxLayout.Y_AXIS));
		studyPanel.add(nafButton);
		studyPanel.add(whistleButton);

		blowingLevel = new SpinnerNumberModel(DEFAULT_BLOWING_LEVEL,0,10,1);
		blowingLevelSpinner = new JSpinner(blowingLevel);
		blowingLevelSpinner.setName("Blowing Level");

		defaultOptButton = new JRadioButton(OPT_DEFAULT_NAME);
		defaultOptButton.setSelected(true);
		bobyqaOptButton = new JRadioButton(OPT_BOBYQA_NAME);
		cmaesOptButton = new JRadioButton(OPT_CMAES_NAME);
		simplexOptButton = new JRadioButton(OPT_SIMPLEX_NAME);
		powellOptButton = new JRadioButton(OPT_POWELL_NAME);
		optimizerGroup = new ButtonGroup();
		optimizerGroup.add(defaultOptButton);
		optimizerGroup.add(bobyqaOptButton);
		optimizerGroup.add(cmaesOptButton);
		optimizerGroup.add(simplexOptButton);
		optimizerGroup.add(powellOptButton);

		JPanel optimizerPanel = new JPanel();
		optimizerPanel.setLayout(new BoxLayout(optimizerPanel,BoxLayout.Y_AXIS));
		optimizerPanel.add(defaultOptButton);
		optimizerPanel.add(bobyqaOptButton);
		optimizerPanel.add(cmaesOptButton);
		optimizerPanel.add(simplexOptButton);
		optimizerPanel.add(powellOptButton);

		this.add(studyPanel);
		this.add(new JLabel("Blowing Level:"));
		this.add(blowingLevelSpinner);
		this.add(new JLabel("Optimizer Type:"));
		this.add(optimizerPanel);
	}

	@Override
	protected void updateComponents(DialogRequest request) {
		// Set or re-set the value state of components.
		Preferences myPreferences = getApplication().getPreferences();
		String modelName = myPreferences.get(STUDY_MODEL_OPT,NAF_STUDY_NAME);
		if ( modelName.contentEquals(NAF_STUDY_NAME) )
		{
			nafButton.setSelected(true);
			whistleButton.setSelected(false);
		}
		else
		{
			whistleButton.setSelected(true);
			nafButton.setSelected(false);
		}
		Number currentLevel = myPreferences.getInt(BLOWING_LEVEL_OPT, DEFAULT_BLOWING_LEVEL);
		blowingLevel.setValue(currentLevel);
		
		String optimizerType = myPreferences.get(OPTIMIZER_TYPE_OPT, OPT_DEFAULT_NAME);
		defaultOptButton.setSelected(false);
		bobyqaOptButton.setSelected(false);
		cmaesOptButton.setSelected(false);
		simplexOptButton.setSelected(false);
		powellOptButton.setSelected(false);
		if ( optimizerType.contentEquals(OPT_BOBYQA_NAME) )
		{
			bobyqaOptButton.setSelected(true);
		}
		else if ( optimizerType.contentEquals(OPT_CMAES_NAME) )
		{
			cmaesOptButton.setSelected(true);
		}
		else if ( optimizerType.contentEquals(OPT_SIMPLEX_NAME) )
		{
			simplexOptButton.setSelected(true);
		}
		else if ( optimizerType.contentEquals(OPT_POWELL_NAME) )
		{
			powellOptButton.setSelected(true);
		}
		else
		{
			defaultOptButton.setSelected(true);
		}
	}

	@Override
	protected void updatePreferences(GUIApplication application) {
		// Update the application preferences, from the dialog values.
		Preferences myPreferences = application.getPreferences();
		String priorStudyName = myPreferences.get(STUDY_MODEL_OPT, "");
		String studyName;
		String optimizerName;

		if (nafButton.isSelected())
		{
			studyName = NAF_STUDY_NAME;
		}
		else
		{
			studyName = WHISTLE_STUDY_NAME;
		}
		
		if (bobyqaOptButton.isSelected())
		{
			optimizerName = OPT_BOBYQA_NAME;
		}
		else if (cmaesOptButton.isSelected())
		{
			optimizerName = OPT_CMAES_NAME;
		}
		else if (simplexOptButton.isSelected())
		{
			optimizerName = OPT_SIMPLEX_NAME;
		}
		else if (powellOptButton.isSelected())
		{
			optimizerName = OPT_POWELL_NAME;
		}
		else
		{
			optimizerName = OPT_DEFAULT_NAME;
		}

		// Update the preferences, and re-set the view's study model.
		
		myPreferences.put( STUDY_MODEL_OPT, studyName );
		myPreferences.putInt(BLOWING_LEVEL_OPT, blowingLevel.getNumber().intValue());
		myPreferences.put( OPTIMIZER_TYPE_OPT, optimizerName );

		DataView[] views = application.getFocusedViews();
		for (DataView view : views)
		{
			if ( view instanceof StudyView )
			{
				StudyView studyView = (StudyView) view;
				if ( ! studyName.contentEquals(priorStudyName))
				{
					// Study model name has changed.
					studyView.setStudyModel(studyName);
				}
				studyView.getStudyModel().setPreferences(myPreferences);
			}
		}
	}

	@Override
	protected void validateComponents(DialogRequest request, DialogResponse response) 
			throws ApplicationVetoException {
		// Validate the preferences.
	}
}
