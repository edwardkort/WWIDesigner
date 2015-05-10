package com.wwidesigner.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelAdapter;
import com.jidesoft.app.framework.DataModelEvent;
import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.ProgressEvent;
import com.jidesoft.app.framework.ProgressListener;
import com.jidesoft.app.framework.SecondaryBasicDataModel;
import com.jidesoft.app.framework.activity.Activity;
import com.jidesoft.app.framework.activity.ActivityAction;
import com.jidesoft.app.framework.event.EventManager;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.file.TextFileFormat;
import com.jidesoft.app.framework.gui.ActionKeys;
import com.jidesoft.app.framework.gui.ApplicationDialogsUI;
import com.jidesoft.app.framework.gui.ApplicationMenuBarsUI;
import com.jidesoft.app.framework.gui.ApplicationToolBarsUI;
import com.jidesoft.app.framework.gui.ApplicationWindowsUI;
import com.jidesoft.app.framework.gui.DataViewAdapter;
import com.jidesoft.app.framework.gui.DataViewEvent;
import com.jidesoft.app.framework.gui.FileDialogRequest;
import com.jidesoft.app.framework.gui.FileDialogRequestHandler;
import com.jidesoft.app.framework.gui.GUIApplicationAction;
import com.jidesoft.app.framework.gui.MenuBarCustomizer;
import com.jidesoft.app.framework.gui.MenuConstants;
import com.jidesoft.app.framework.gui.MenuGroup;
import com.jidesoft.app.framework.gui.PreferencesDialogRequest;
import com.jidesoft.app.framework.gui.PreferencesPane;
import com.jidesoft.app.framework.gui.StandardDialogRequest;
import com.jidesoft.app.framework.gui.ToolBarCustomizer;
import com.jidesoft.app.framework.gui.actions.ComponentAction;
import com.jidesoft.app.framework.gui.feature.AutoInstallActionsFeature;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.app.framework.gui.framed.DockableConfiguration;
import com.jidesoft.app.framework.gui.framed.DockableFrameCustomizer;
import com.jidesoft.app.framework.gui.framed.DockingApplicationFeature;
import com.jidesoft.app.framework.gui.framed.ToggleFrameAction;
import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import com.wwidesigner.gui.util.FileOpenDialogPreviewPane;
import com.wwidesigner.note.wizard.TuningWizardDialog;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.util.Constants.LengthType;

/**
 * Main program to present a study view to the user, and allow them to interact
 * with instrument study models to analyze and optimize instruments.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class WIDesigner extends FileBasedApplication implements EventSubscriber
{
	static final String FILE_OPENED_EVENT_ID = "FileOpened";
	static final String FILE_CLOSED_EVENT_ID = "FileClosed";
	static final String FILE_SAVED_EVENT_ID = "FileSaved";
	static final String TUNING_ACTIVE_EVENT_ID = "TuningActive";
	static final String OPTIMIZATION_ACTIVE_EVENT_ID = "OptimizationActive";
	static final String WINDOW_RENAMED_EVENT_ID = "WindowRenamed";
	static final String CONSTRAINTS_ACTIVE_EVENT_ID = "ConstraintsActive";
	static final String CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID = "ConstraintsSaveAsActive";
	static final String INSTRUMENT_IN_FOCUS_EVENT_ID = "InstrumentInFocus";
	static final String INSTRUMENT_SELECTED_EVENT_ID = "InstrumentSelected";

	static final String CONSOLE_ACTION_ID = "Console";
	static final String STUDY_ACTION_ID = "Study";
	static final String CALCULATE_TUNING_ACTION_ID = "Calculate tuning";
	static final String GRAPH_TUNING_ACTION_ID = "Graph tuning";
	static final String OPTIMIZE_INSTRUMENT_ACTION_ID = "Optimize instrument";
	static final String SKETCH_INSTRUMENT_ACTION_ID = "Sketch instrument";
	static final String CREATE_INSTRUMENT_FILE_ACTION_ID = "New Instrument";
	static final String CREATE_TUNING_FILE_ACTION_ID = "New Tuning ...";
	static final String COMPARE_INSTRUMENT_ACTION_ID = "Compare instruments";
	static final String CLEAR_CONSOLE_ACTION_ID = "Clear Console";
	static final String WARN_ON_DIRTY_CLOSE_ACTION_ID = "Warn on dirty close";
	static final String RENAME_WINDOW_ACTION_ID = "Rename window";
	static final String TOGGLE_VIEW_ACTION_ID = "Toggle data view";
	static final String SAVE_CONSTRAINTS_ACTION_ID = "Save constraints";
	static final String OPEN_CONSTRAINTS_ACTION_ID = "Open constraints...";
	static final String SAVE_AS_CONSTRAINTS_ACTION_ID = "Save-as constraints...";
	static final String CREATE_DEFAULT_CONSTRAINTS_ACTION_ID = "Create default constraints";
	static final String CREATE_BLANK_CONSTRAINTS_ACTION_ID = "Create blank constraints";

	protected boolean isWarnOnDirtyClose = false;

	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}

		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		FileBasedApplication app = new WIDesigner();

		app.getApplicationUIManager().setSetsLookAndFeel(false);
		app.run(args);
	}

	public WIDesigner()
	{
		super("Woodwind Instrument Designer", TDI_APPLICATION_STYLE);

		// Set behaviour
		getApplicationUIManager().setUseJideDockingFramework(true);
		getApplicationUIManager().setUseJideActionFramework(true);
		addApplicationFeature(new AutoInstallActionsFeature());
		setExitApplicationOnLastDataView(false);
		setNewDataOnRun(false);

		// window size
		ApplicationWindowsUI windowsUI = getApplicationUIManager()
				.getWindowsUI();
		windowsUI.setPreferredWindowSize(windowsUI
				.getPreferredMaximumWindowSize());

		getApplicationUIManager().setUseJideDocumentPane(true);

		// Add my UI customizations
		PreferencesPane preferencesDialog = new OptimizationPreferences();
		PreferencesDialogRequest.installPreferences(this, preferencesDialog);

		addFileMapping(new TextFileFormat("xml", "XML"), XmlToggleView.class);

		addActions();
		addDockedViews();
		addEvents();
		addListeners();
		addWindowMenuToggles();
		customizeMenus();
		addToolBar();
		customizeAboutBox();

		// The stock JDAF UndoAction and RedoAction are focused on the state of
		// the UndoManager of the focused DataModel. But the CodeEditor has its
		// own Undo and Redo actions. So we use a ComponentAction which will
		// automatically delegate to the CodeEditors Undo and Redo actions in
		// its
		// ActionMap when the CodeEditor is focused
		getActionMap().put(ActionKeys.UNDO, new ComponentAction("undo"));
		getActionMap().put(ActionKeys.REDO, new ComponentAction("redo"));

	}

	protected void addActions()
	{
		addCalculateTuningAction();
		addGraphTuningAction();
		addOptimizeInstrumentAction();
		addSketchInstrumentAction();
		addCreatingTuningFileAction();
		addCompareInstrumentsAction();
		addClearConsoleAction();
		addWarnOnDirtyCloseAction();
		addRenameWindowAction();
		addToggleViewAction();
		addConstraintsOpenAction();
		addConstraintsSaveAs();
		addConstraintsCreateDefault();
		addConstraintsCreateBlank();
	}

	protected void addToolBar()
	{
		addToolBarCustomizer(new ToolBarCustomizer()
		{

			@Override
			public void createApplicationToolBar(String toolbarName,
					Container toolbar, ApplicationToolBarsUI toolbarsUI)
			{
				if (toolbarName.equals("Tools"))
				{
					AbstractButton button;
					button = toolbarsUI.addToolBarButton(toolbar,
							CALCULATE_TUNING_ACTION_ID);
					button = toolbarsUI.addToolBarButton(toolbar,
							GRAPH_TUNING_ACTION_ID);
					button = toolbarsUI.addToolBarButton(toolbar,
							OPTIMIZE_INSTRUMENT_ACTION_ID);
					button = toolbarsUI.addToolBarButton(toolbar,
							SKETCH_INSTRUMENT_ACTION_ID);
					button = toolbarsUI.addToolBarButton(toolbar,
							COMPARE_INSTRUMENT_ACTION_ID);
					toolbarsUI.addToolBarSeparator(toolbar);
					button = toolbarsUI.addToolBarButton(toolbar,
							TOGGLE_VIEW_ACTION_ID);
					button.setText("Vu");
				}
			}

			@Override
			public void customizeStandardToolBar(String toolbarName,
					Container toolbar, ApplicationToolBarsUI toolbarsUI)
			{
			}

			@Override
			public String[] getToolbarNames()
			{
				return new String[] { "Tools" };
			}

		});
	}

	protected void customizeMenus()
	{
		addMenuBarCustomizer(new MenuBarCustomizer()
		{
			public JMenu[] createApplicationMenus(
					ApplicationMenuBarsUI menuBarUI)
			{
				JMenu menu = menuBarUI.defaultMenu("Tool Menu", "Tool");
				JMenuItem menuItem;
				menuItem = menu.add(menuBarUI
						.getAction(CALCULATE_TUNING_ACTION_ID));
				menuItem.setMnemonic('C');
				menuItem = menu
						.add(menuBarUI.getAction(GRAPH_TUNING_ACTION_ID));
				menuItem.setMnemonic('G');
				menuItem = menu.add(menuBarUI
						.getAction(OPTIMIZE_INSTRUMENT_ACTION_ID));
				menuItem.setMnemonic('O');
				menuItem = menu.add(menuBarUI
						.getAction(SKETCH_INSTRUMENT_ACTION_ID));
				menuItem.setMnemonic('S');
				menuItem = menu.add(menuBarUI
						.getAction(COMPARE_INSTRUMENT_ACTION_ID));
				menuItem.setMnemonic('m');
				menu.setMnemonic('T');
				return new JMenu[] { menu };
			}

			@Override
			public void customizeStandardMenu(String menuID, JMenu menu,
					ApplicationMenuBarsUI menuBarsUI)
			{
				JMenuItem menuItem;
				Action action;
				if (menuID == FILE_MENU_ID)
				{
					MenuGroup group = menuBarsUI.getMenuGroup(
							FILE_NEW_GROUP_ID, menu);
					action = menuBarsUI.getAction(CREATE_TUNING_FILE_ACTION_ID);
					menuItem = group.addMenuItem(action);
					menuItem.setMnemonic('T');

					group = menuBarsUI.getMenuGroup(FILE_USER_GROUP_ID, menu);
					action = menuBarsUI.getAction(OPEN_CONSTRAINTS_ACTION_ID);
					menuItem = group.addMenuItem(action);
					action = menuBarsUI
							.getAction(SAVE_AS_CONSTRAINTS_ACTION_ID);
					menuItem = group.addMenuItem(action);
					action = menuBarsUI
							.getAction(CREATE_DEFAULT_CONSTRAINTS_ACTION_ID);
					menuItem = group.addMenuItem(action);
					action = menuBarsUI
							.getAction(CREATE_BLANK_CONSTRAINTS_ACTION_ID);
					menuItem = group.addMenuItem(action);
				}
				else if (menuID == WINDOW_MENU_ID)
				{
					MenuGroup group = menuBarsUI.getMenuGroup(
							WINDOW_USER_GROUP_ID, menu);
					action = menuBarsUI.getAction(CLEAR_CONSOLE_ACTION_ID);
					menuItem = group.addMenuItem(action);
					menuItem.setMnemonic('l');
					action = menuBarsUI.getAction(RENAME_WINDOW_ACTION_ID);
					menuItem = group.addMenuItem(action);
					menuItem.setMnemonic('R');
					action = menuBarsUI.getAction(TOGGLE_VIEW_ACTION_ID);
					menuItem = group.addMenuItem(action);
					menuItem.setMnemonic('T');
					JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(
							menuBarsUI.getAction(WARN_ON_DIRTY_CLOSE_ACTION_ID));
					cbItem.setSelected(getApplicationWarnOnDirtyClose());
					cbItem.setMnemonic('W');
					group.addMenuItem(cbItem);
					group.insertSeparator(3);
					group.insertSeparator(6);
				}
			}
		});
	}

	protected void addToggleViewAction()
	{
		Action action;
		URL imageUrl;

		action = new GUIApplicationAction(TOGGLE_VIEW_ACTION_ID)
		{
			public void actionPerformedDetached(ActionEvent event)
			{
				DataView view = getFocusedView();
				if (view instanceof XmlToggleView)
				{
					try
					{
						((XmlToggleView) view).toggleView();
					}
					catch (DataModelException ex)
					{
						getStudyView().showException(ex);
					}
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION,
				"Toggle data view of current editor tab");
		imageUrl = WIDesigner.class.getResource("images/view.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		action.setEnabled(false);
		getActionMap().put(TOGGLE_VIEW_ACTION_ID, action);
	}

	protected void addRenameWindowAction()
	{
		Action action;

		action = new GUIApplicationAction(RENAME_WINDOW_ACTION_ID)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformedDetached(ActionEvent event)
			{
				DataModel focusedModel = getFocusedModel();
				if (focusedModel != null)
				{
					String oldName = focusedModel.getName();
					String newName = JOptionPane.showInputDialog(
							getApplicationUIManager().getWindowsUI()
									.getDialogParent(), "Enter a new name for "
									+ oldName, oldName);
					if (newName != null && !newName.equals(oldName))
					{
						focusedModel.setName(newName);
					}
					getEventManager().publish(WINDOW_RENAMED_EVENT_ID,
							focusedModel);
				}
			}
		};
		action.setEnabled(false);
		getActionMap().put(RENAME_WINDOW_ACTION_ID, action);
	}

	protected void addWarnOnDirtyCloseAction()
	{
		Action action;

		action = new GUIApplicationAction(WARN_ON_DIRTY_CLOSE_ACTION_ID)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformedDetached(ActionEvent event)
			{
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) event
						.getSource();
				isWarnOnDirtyClose = source.isSelected();
				setApplicationWarnOnDirtyClose();
			}
		};
		getActionMap().put(WARN_ON_DIRTY_CLOSE_ACTION_ID, action);
	}

	protected void addClearConsoleAction()
	{
		Action action;

		action = new GUIApplicationAction(CLEAR_CONSOLE_ACTION_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformedDetached(ActionEvent e)
			{
				ConsoleView view = getConsoleView();
				view.clear();
			}
		};
		getActionMap().put(CLEAR_CONSOLE_ACTION_ID, action);
	}

	protected void addConstraintsOpenAction()
	{
		Action action = new GUIApplicationAction(OPEN_CONSTRAINTS_ACTION_ID)
		{
			public void actionPerformedDetached(ActionEvent e)
			{
				File priorStartDirectory = getLastDirectory();
				File startDirectory = getConstraintsLeafDirectory();
				setLastDirectory(startDirectory);
				File constraintsFile = FileDialogRequest.showOpenDialog(
						getApplication(), startDirectory);
				setLastDirectory(priorStartDirectory);
				if (constraintsFile != null)
				{
					try
					{
						openData(constraintsFile);
					}
					catch (Exception ex)
					{
					}
				}
			}
		};
		getActionMap().put(OPEN_CONSTRAINTS_ACTION_ID, action);
		action.setEnabled(false);
	}

	protected void addConstraintsSaveAs()
	{
		Action action = new GUIApplicationAction(SAVE_AS_CONSTRAINTS_ACTION_ID)
		{
			public void actionPerformedDetached(ActionEvent e)
			{
				File priorStartDirectory = getLastDirectory();
				try
				{
					FileDataModel fileModel = (FileDataModel) getFocusedModel();
					String xmlString = fileModel.getData().toString();
					Constraints constraints = StudyModel
							.getConstraints(xmlString);
					File leaf = getConstraintsLeafDirectory(constraints);
					setLastDirectory(leaf);
					saveDataAs(fileModel);
					setLastDirectory(priorStartDirectory);
				}
				catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		};
		getActionMap().put(SAVE_AS_CONSTRAINTS_ACTION_ID, action);
		action.setEnabled(false);
	}

	protected void addConstraintsCreateDefault()
	{
		final Activity createDefaultActivity = new Activity(
				CREATE_DEFAULT_CONSTRAINTS_ACTION_ID)
		{

			@Override
			public void activityPerformed() throws Exception
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.getDefaultConstraints();
				}
			}
		};
		Action action = new ActivityAction(createDefaultActivity)
		{
			public void actionPerformed(ActionEvent e)
			{
				getActivityManager().run(createDefaultActivity);
			}
		};
		getActionMap().put(CREATE_DEFAULT_CONSTRAINTS_ACTION_ID, action);
		action.setEnabled(false);
	}

	protected void addConstraintsCreateBlank()
	{
		Action action;

		final Activity createBlankActivity = new Activity(
				CREATE_BLANK_CONSTRAINTS_ACTION_ID)
		{

			@Override
			public void activityPerformed() throws Exception
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.getBlankConstraints();
				}
			}
		};
		action = new ActivityAction(createBlankActivity)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getActivityManager().run(createBlankActivity);
			}
		};
		getActionMap().put(CREATE_BLANK_CONSTRAINTS_ACTION_ID, action);
		action.setEnabled(false);
	}

	/**
	 * Get the Constraints root directory from the Preferences.
	 * 
	 * @return An empty String if the directory has not been set in the Options.
	 */
	public String getConstraintsRootDirectoryPath()
	{
		String path = "";
		String constraintsDirectory = getPreferences().get(
				OptimizationPreferences.CONSTRAINTS_DIRECTORY,
				OptimizationPreferences.DEFAULT_CONSTRAINTS_DIRECTORY);
		if (constraintsDirectory != null)
		{
			constraintsDirectory = constraintsDirectory.trim();
			if (constraintsDirectory.length() > 0)
			{
				path = constraintsDirectory;
			}
		}

		return path;
	}

	public File getConstraintsLeafDirectory()
	{
		String rootPath = getConstraintsRootDirectoryPath();

		return getStudyView().getConstraintsLeafDirectory(rootPath);
	}

	public File getConstraintsLeafDirectory(Constraints constraints)
	{
		String rootPath = getConstraintsRootDirectoryPath();

		return getStudyView()
				.getConstraintsLeafDirectory(rootPath, constraints);
	}

	protected void addCompareInstrumentsAction()
	{
		Action action;
		URL imageUrl;

		action = new GUIApplicationAction(COMPARE_INSTRUMENT_ACTION_ID)
		{
			@Override
			public void actionPerformedDetached(ActionEvent e)
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.compareInstrument();
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION,
				"Compare instrument selected in study to the current editor tab");
		imageUrl = WIDesigner.class.getResource("images/compare.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		action.setEnabled(false);
		getActionMap().put(COMPARE_INSTRUMENT_ACTION_ID, action);
	}

	protected void addCreatingTuningFileAction()
	{
		Action action;
		URL imageUrl;

		action = new GUIApplicationAction(CREATE_TUNING_FILE_ACTION_ID)
		{
			@Override
			public void actionPerformedDetached(ActionEvent e)
			{
				TuningWizardDialog wizard = new TuningWizardDialog(
						getApplicationUIManager().getWindowsUI()
								.getDialogParent(), "Tuning File Wizard", false);
				wizard.setCurrentSaveDirectory(getLastDirectory());
				wizard.setVisible(true);
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION,
				"Use the Tuning Wizard to create a tuning file from notes and fingerings");
		imageUrl = WIDesigner.class.getResource("images/tuning.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		getActionMap().put(CREATE_TUNING_FILE_ACTION_ID, action);
	}

	protected void addSketchInstrumentAction()
	{
		Action action;
		URL imageUrl;

		action = new GUIApplicationAction(SKETCH_INSTRUMENT_ACTION_ID)
		{
			@Override
			public void actionPerformedDetached(ActionEvent e)
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.sketchInstrument();
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION,
				"Draw a sketch of an instrument");
		imageUrl = WIDesigner.class.getResource("images/sketch.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		action.setEnabled(false);
		getActionMap().put(SKETCH_INSTRUMENT_ACTION_ID, action);
	}

	protected void addOptimizeInstrumentAction()
	{
		Action action;
		String message;
		URL imageUrl;

		final Activity optActivity = new Activity(OPTIMIZE_INSTRUMENT_ACTION_ID)
		{

			@Override
			public void activityPerformed() throws Exception
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.optimizeInstrument();
				}
			}
		};
		message = "Calculating optimized instrument.\nThis may take several minutes.\nPlease be patient.";
		optActivity
				.addProgressListener(new BlockingProgressListener(
						getApplicationUIManager().getWindowsUI(), "Optimizing",
						message));
		action = new ActivityAction(optActivity)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getActivityManager().run(optActivity);
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Optimize instrument");
		imageUrl = WIDesigner.class.getResource("images/optimize.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		action.setEnabled(false);
		getActionMap().put(OPTIMIZE_INSTRUMENT_ACTION_ID, action);
	}

	protected void addGraphTuningAction()
	{
		Action action;
		String message;
		URL imageUrl;
		final Activity graphActivity = new Activity(GRAPH_TUNING_ACTION_ID)
		{

			@Override
			public void activityPerformed() throws Exception
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.graphTuning();
				}
			}
		};
		message = "Calculating instrument tuning.\nThis may take several seconds.";
		graphActivity.addProgressListener(new BlockingProgressListener(
				getApplicationUIManager().getWindowsUI(),
				GRAPH_TUNING_ACTION_ID, message));
		action = new ActivityAction(graphActivity)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getActivityManager().run(graphActivity);
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION,
				"Draw graph of instrument tuning");
		imageUrl = WIDesigner.class.getResource("images/graph.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		action.setEnabled(false);
		getActionMap().put(GRAPH_TUNING_ACTION_ID, action);
	}

	protected void addCalculateTuningAction()
	{
		Action action;
		String message;
		URL imageUrl;
		// The complexity in this method is due to the poor way that JDAF
		// threads Actions and Activities.
		// Anything simpler just didn't work right!
		final Activity activity = new Activity(CALCULATE_TUNING_ACTION_ID)
		{

			@Override
			public void activityPerformed() throws Exception
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.getTuning();
				}
			}
		};
		message = "Calculating instrument tuning.\nThis may take several seconds.";
		activity.addProgressListener(new BlockingProgressListener(
				getApplicationUIManager().getWindowsUI(),
				CALCULATE_TUNING_ACTION_ID, message));
		action = new ActivityAction(activity)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getActivityManager().run(activity);
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION,
				"Calculate instrument tuning table");
		imageUrl = WIDesigner.class.getResource("images/calculate.png");
		if (imageUrl != null)
		{
			action.putValue(Action.SMALL_ICON, new ImageIcon(imageUrl));
			action.putValue(Action.LARGE_ICON_KEY, new ImageIcon(imageUrl));
		}
		action.setEnabled(false);
		getActionMap().put(CALCULATE_TUNING_ACTION_ID, action);
	}

	protected void addWindowMenuToggles()
	{
		Action action = new ToggleFrameAction(CONSOLE_ACTION_ID, true);
		action.putValue(AutoInstallActionsFeature.MENU_ID,
				MenuConstants.WINDOW_MENU_ID);
		getActionMap().put("consoleToggle", action);

		action = new ToggleFrameAction(STUDY_ACTION_ID, true);
		action.putValue(AutoInstallActionsFeature.MENU_ID,
				MenuConstants.WINDOW_MENU_ID);
		getActionMap().put("studyToggle", action);
	}

	protected void addEvents()
	{
		EventManager eventManager = getEventManager();
		eventManager.addEvent(FILE_OPENED_EVENT_ID);
		eventManager.addEvent(FILE_CLOSED_EVENT_ID);
		eventManager.addEvent(FILE_SAVED_EVENT_ID);
		eventManager.addEvent(TUNING_ACTIVE_EVENT_ID);
		eventManager.addEvent(OPTIMIZATION_ACTIVE_EVENT_ID);
		eventManager.addEvent(WINDOW_RENAMED_EVENT_ID);
		eventManager.addEvent(CONSTRAINTS_ACTIVE_EVENT_ID);
		eventManager.addEvent(CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID);
		eventManager.addEvent(INSTRUMENT_SELECTED_EVENT_ID);

		eventManager.subscribe(TUNING_ACTIVE_EVENT_ID, this);
		eventManager.subscribe(OPTIMIZATION_ACTIVE_EVENT_ID, this);
		eventManager.subscribe(CONSTRAINTS_ACTIVE_EVENT_ID, this);
		eventManager.subscribe(CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID, this);
		eventManager.subscribe(INSTRUMENT_SELECTED_EVENT_ID, this);
	}

	protected void addListeners()
	{
		// Add preview panel to Open file dialog
		((FileDialogRequestHandler) getApplicationUIManager().getDialogsUI()
				.getDialogHandler(FileDialogRequest.class))
				.setUseAWTFileDialogs(false);
		getApplicationUIManager().getDialogsUI().addDialogListener(
				new FileOpenDialogPreviewPane());

		addDataModelListener(new DataModelAdapter()
		{
			@Override
			public void dataModelOpened(DataModelEvent dataModelEvent)
			{
				getEventManager().publish(FILE_OPENED_EVENT_ID,
						dataModelEvent.getDataModel());
			}

			@Override
			public void dataModelClosed(DataModelEvent dataModelEvent)
			{
				getEventManager().publish(FILE_CLOSED_EVENT_ID,
						dataModelEvent.getDataModel());
			}

			@Override
			public void dataModelSaved(DataModelEvent dataModelEvent)
			{
				getEventManager().publish(FILE_SAVED_EVENT_ID,
						dataModelEvent.getDataModel());
			}

			@Override
			public void dataModelClosing(DataModelEvent dataModelEvent)
					throws ApplicationVetoException
			{
				DataModel dataModel = dataModelEvent.getDataModel();
				if (dataModel.isDirty())
				{
					if (!isWarnOnDirtyClose)
					{
						dataModel.setDirty(false);
						return;
					}
				}
			}

		});

		addDataViewListener(new DataViewAdapter()
		{
			@Override
			public void dataViewActivated(DataViewEvent event)
			{
				checkConstraintsFocused();

				DataView view = event.getDataView();
				if (view instanceof XmlToggleView)
				{
					DataModel model = getDataModel(view);
					String name = model.getName();
					Action action = getActionMap().get(RENAME_WINDOW_ACTION_ID);
					if (name.length() > 0 && name.startsWith("Untitled"))
					{
						action.setEnabled(true);
					}
					else
					{
						action.setEnabled(false);
					}

					StudyModel studyModel = getStudyView().getStudyModel();
					int numberOfToggles = studyModel
							.getNumberOfToggleViews((BasicDataModel) model);
					action = getActionMap().get(TOGGLE_VIEW_ACTION_ID);
					if (numberOfToggles > 1)
					{
						action.setEnabled(true);
					}
					else
					{
						action.setEnabled(false);
					}

					if (StudyModel.INSTRUMENT_CATEGORY_ID.equals(model
							.getSemanticName()))
					{
						setCompareInstrumentAction(model.getName(), true);
					}
					else
					{
						setCompareInstrumentAction(null, true);
					}
				}
			}

			@Override
			public void dataViewClosed(DataViewEvent event)
			{
				checkConstraintsFocused();
			}

			@Override
			public void dataViewOpened(DataViewEvent event)
			{
				checkConstraintsFocused();
			}

		});

	}

	protected void setCompareInstrumentAction(String modelName,
			boolean isFromDataView)
	{
		Action action = getActionMap().get(COMPARE_INSTRUMENT_ACTION_ID);
		if (modelName == null || modelName.length() == 0)
		{
			action.setEnabled(false);
		}
		else if (isFromDataView)
		{
			String otherModelName = getStudyView().getStudyModel()
					.getSelectedInstrumentName();
			if (otherModelName == null || modelName.equals(otherModelName))
			{
				action.setEnabled(false);
			}
			else
			{
				action.setEnabled(true);
			}
		}
		else
		{
			DataModel otherModel = getFocusedModel();
			if (otherModel == null
					|| !StudyModel.INSTRUMENT_CATEGORY_ID.equals(otherModel
							.getSemanticName()))
			{
				action.setEnabled(false);
			}
			else
			{
				action.setEnabled(!modelName.equals(otherModel.getName()));
			}
		}
	}

	protected void addDockedViews()
	{
		DockingApplicationFeature docking = new DockingApplicationFeature();
		DockableConfiguration config = new DockableConfiguration();

		// config for "Console" model-view
		config.setFrameName("Console");
		config.setInitState(DockContext.STATE_FRAMEDOCKED);
		config.setInitSide(DockContext.DOCK_SIDE_SOUTH);
		config.setInitIndex(1);
		config.setCriteria(System.out.toString());
		config.setDataModelClass(SecondaryBasicDataModel.class);
		config.setDataViewClass(ConsoleView.class);
		docking.addDockableMapping(config);

		// config for "Study" pane
		config = new DockableConfiguration();
		config.setFrameName("Study");
		config.setInitState(DockContext.STATE_FRAMEDOCKED);
		config.setInitSide(DockContext.DOCK_SIDE_WEST);
		config.setInitIndex(0);
		config.setDataModelClass(SecondaryBasicDataModel2.class);
		config.setDataViewClass(StudyView.class);
		docking.addDockableMapping(config);

		// add feature
		addApplicationFeature(docking);

		// Set initial Study view width
		docking.addDockableCustomizer(new DockableFrameCustomizer()
		{

			@Override
			public void customizeDockable(DockingApplicationFeature feature,
					DockingManager manager, DockableFrame dockable,
					DataView dataView)
			{
				if (dataView instanceof StudyView)
				{
					dockable.setDockedWidth(230);
				}
			}
		});

	}

	protected void customizeAboutBox()
	{
		JTextPane aboutText = new JTextPane();
		try
		{
			aboutText
					.setPage(WIDesigner.class.getResource("images/about.html"));
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		aboutText.setEditable(false);

		StandardDialogRequest.setQueuedDialogRequestComponent(this,
				ApplicationDialogsUI.ABOUT_DIALOG_REQUEST_KEY, aboutText);

	}

	protected final class BlockingProgressListener implements ProgressListener
	{
		private JDialog dialog;

		protected BlockingProgressListener(ApplicationWindowsUI windowsUI,
				String activityName, String message)
		{
			dialog = new JDialog(windowsUI.getDialogParent(), activityName,
					true);
			Container contentPane = dialog.getContentPane();
			JTextPane textPane = new JTextPane();
			textPane.setText(message);
			textPane.setEditable(false);
			textPane.setMargin(new Insets(20, 20, 20, 20));
			contentPane.add(textPane, BorderLayout.CENTER);
			dialog.pack();
			dialog.setLocationRelativeTo(windowsUI.getDialogParent());
		}

		@Override
		public void progressStart(ProgressEvent e)
		{
			try
			{
				dialog.setVisible(true);
			}
			catch (Exception ex)
			{
			}
		}

		@Override
		public void progressProgressing(ProgressEvent e)
		{
			// Do nothing
		}

		@Override
		public void progressEnd(ProgressEvent e)
		{
			try
			{
				dialog.dispose();
			}
			catch (Exception ex)
			{
			}
		}
	}

	public static class SecondaryBasicDataModel2 extends
			SecondaryBasicDataModel
	{
	}

	@Override
	public void doEvent(SubscriberEvent e)
	{
		String eventName = e.getEvent();
		if (TUNING_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(CALCULATE_TUNING_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
			action = getActionMap().get(GRAPH_TUNING_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
		else if (OPTIMIZATION_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(OPTIMIZE_INSTRUMENT_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
		else if (CONSTRAINTS_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(OPEN_CONSTRAINTS_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
			action = getActionMap().get(CREATE_DEFAULT_CONSTRAINTS_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
			action = getActionMap().get(CREATE_BLANK_CONSTRAINTS_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
		else if (CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(SAVE_AS_CONSTRAINTS_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
		else if (INSTRUMENT_SELECTED_EVENT_ID.equals(eventName))
		{
			setCompareInstrumentAction((String) e.getSource(), false);
			Action action = getActionMap().get(SKETCH_INSTRUMENT_ACTION_ID);
			if (action != null)
			{
				action.setEnabled(((String) e.getSource()).length() > 0);
			}
		}
	}

	protected void checkConstraintsFocused()
	{
		try
		{
			FileDataModel fileModel = (FileDataModel) getFocusedModel();
			String xmlString = fileModel.getData().toString();
			Constraints constraints = StudyModel.getConstraints(xmlString);
			if (constraints != null)
			{
				getEventManager().publish(CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID,
						true);
			}
			else
			{
				getEventManager().publish(CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID,
						false);
			}
		}
		catch (Exception e)
		{
			getEventManager().publish(CONSTRAINTS_SAVE_AS_ACTIVE_EVENT_ID,
					false);
		}

	}

	public StudyView getStudyView()
	{
		StudyView studyView = null;
		DataView[] views = getApplicationUIManager().getWindowsUI()
				.getDataViews();
		for (DataView view : views)
		{
			if (view instanceof StudyView)
			{
				studyView = (StudyView) view;
				break;
			}
		}

		return studyView;
	}

	protected ConsoleView getConsoleView()
	{
		ConsoleView consoleView = null;
		DataView[] views = getApplicationUIManager().getWindowsUI()
				.getDataViews();
		for (DataView view : views)
		{
			if (view instanceof ConsoleView)
			{
				consoleView = (ConsoleView) view;
				break;
			}
		}

		return consoleView;
	}

	/**
	 * Returns the LengthType specified in the application preferences.
	 * 
	 * @return If not set (in the Options dialog), returns the default
	 *         LengthType.
	 */
	public LengthType getApplicationLengthType()
	{
		Preferences preferences = getPreferences();
		String lengthTypeName = preferences.get(
				OptimizationPreferences.LENGTH_TYPE_OPT,
				OptimizationPreferences.LENGTH_TYPE_DEFAULT);

		return LengthType.valueOf(lengthTypeName);
	}

	public boolean getApplicationWarnOnDirtyClose()
	{
		Preferences preferences = getPreferences();
		isWarnOnDirtyClose = preferences.getBoolean(
				OptimizationPreferences.WARN_ON_DIRTY_CLOSE_OPT,
				OptimizationPreferences.WARN_ON_DIRTY_CLOSE_DEFAULT);

		return isWarnOnDirtyClose;
	}

	public void setApplicationWarnOnDirtyClose()
	{
		Preferences preferences = getPreferences();
		preferences.putBoolean(OptimizationPreferences.WARN_ON_DIRTY_CLOSE_OPT,
				isWarnOnDirtyClose);
	}

}
