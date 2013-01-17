/**
 * 
 */
package com.wwidesigner.gui;

import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
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
 * Dialog pane to display/modify perferences for the optimization application.
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

	JPanel studyGroup;
	JRadioButton nafButton;
	JRadioButton whistleButton;
	JSpinner blowingLevelSpinner;
	SpinnerNumberModel blowingLevel;

	@Override
	protected void initializeComponents(DialogRequest request) {
		// Setup dialog components.
		nafButton = new JRadioButton("NAF Study");
		nafButton.setSelected(true);
		whistleButton = new JRadioButton("Whistle Study");
		studyGroup = new JPanel();
		studyGroup.setLayout(new BoxLayout(studyGroup,BoxLayout.Y_AXIS));
		blowingLevel = new SpinnerNumberModel(DEFAULT_BLOWING_LEVEL,0,10,1);
		blowingLevelSpinner = new JSpinner(blowingLevel);
		blowingLevelSpinner.setName("Blowing Level");
		studyGroup.add(nafButton);
		studyGroup.add(whistleButton);
		this.add(studyGroup);
		this.add(new JLabel("Blowing Level:"));
		this.add(blowingLevelSpinner);
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
	}

	@Override
	protected void updatePreferences(GUIApplication application) {
		// Update the application preferences, from the dialog values.
		Preferences myPreferences = application.getPreferences();
		String priorStudyName = myPreferences.get(STUDY_MODEL_OPT, "");
		String studyName;

		if (nafButton.isSelected())
		{
			studyName = NAF_STUDY_NAME;
		}
		else
		{
			studyName = WHISTLE_STUDY_NAME;
		}

		// Update the preferences, and re-set the view's study model.
		
		myPreferences.put( STUDY_MODEL_OPT, studyName );
		myPreferences.putInt(BLOWING_LEVEL_OPT, blowingLevel.getNumber().intValue());

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
