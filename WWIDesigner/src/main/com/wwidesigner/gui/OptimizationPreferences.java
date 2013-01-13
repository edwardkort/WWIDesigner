/**
 * 
 */
package com.wwidesigner.gui;

import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

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

	ButtonGroup studyGroup;
	JRadioButton nafButton;
	JRadioButton whistleButton;

	@Override
	protected void initializeComponents(DialogRequest request) {
		// Setup dialog components.
		nafButton = new JRadioButton("NAF Study");
		nafButton.setSelected(true);
		whistleButton = new JRadioButton("Whistle Study");
		studyGroup = new ButtonGroup();
		studyGroup.add(nafButton);
		studyGroup.add(whistleButton);
		this.add(nafButton);
		this.add(whistleButton);
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
	}

	@Override
	protected void updatePreferences(GUIApplication application) {
		// Update the application preferences, from the dialog values.
		Preferences myPreferences = application.getPreferences();
		String studyName;
		if (nafButton.isSelected())
		{
			studyName = NAF_STUDY_NAME;
		}
		else
		{
			studyName = WHISTLE_STUDY_NAME;
		}
		if ( ! studyName.contentEquals(myPreferences.get(STUDY_MODEL_OPT, "")))
		{
			// Study model name has changed.
			// Update the preferences, and re-set the view's study model.
			myPreferences.put( STUDY_MODEL_OPT, studyName );
			DataView[] views = application.getFocusedViews();
			for (DataView view : views)
			{
				if ( view instanceof StudyView )
				{
					((StudyView)view).setStudyModel(studyName);
				}
			}
		}
	}

	@Override
	protected void validateComponents(DialogRequest request, DialogResponse response) 
			throws ApplicationVetoException {
		// Validate the preferences.
	}
}
