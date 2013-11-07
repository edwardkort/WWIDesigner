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
	
	public static final int NrOptimizers = 6;	// Including default
	public static final String OPTIMIZER_TYPE_OPT = "OptimizerType";
	public static final String OPT_DEFAULT_NAME = "Default";
	public static final String OPT_BOBYQA_NAME = "BOBYQA";
	public static final String OPT_CMAES_NAME = "CMAES";
	public static final String OPT_MULTISTART_NAME = "Multi-Start";
	public static final String OPT_SIMPLEX_NAME = "Simplex";
	public static final String OPT_POWELL_NAME = "Powell";
	
	protected static String[] OptimizerName = new String[] { OPT_DEFAULT_NAME, OPT_BOBYQA_NAME, 
			OPT_CMAES_NAME, OPT_MULTISTART_NAME, OPT_SIMPLEX_NAME, OPT_POWELL_NAME};

	JRadioButton nafButton;
	JRadioButton whistleButton;
	ButtonGroup studyGroup;
	
	JRadioButton[] optButton = new JRadioButton[NrOptimizers];
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

		optimizerGroup = new ButtonGroup();
		
		for (int i = 0; i < NrOptimizers; ++ i)
		{
			optButton[i] = new JRadioButton(OptimizerName[i]);
			optimizerGroup.add(optButton[i]);
		}
		optButton[0].setSelected(true);

		JPanel optimizerPanel = new JPanel();
		optimizerPanel.setLayout(new BoxLayout(optimizerPanel,BoxLayout.Y_AXIS));
		
		// Add all but Powell optimizer to the optimizerPanel.
		for (int i = 0; i < NrOptimizers - 1; ++ i)
		{
			optimizerPanel.add(optButton[i]);
		}

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

		// Ensure first button, Default, is selected if optimizer name not found.
		optButton[0].setSelected(true);
		for (int i = 1; i < NrOptimizers; ++ i)
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
	}

	@Override
	protected void updatePreferences(GUIApplication application) {
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
		
		for (int i = 0; i < NrOptimizers; ++ i)
		{
			if (optButton[i].isSelected())
			{
				optimizerName = OptimizerName[i];
			}
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
