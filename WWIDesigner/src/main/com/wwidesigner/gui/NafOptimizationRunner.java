package com.wwidesigner.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelAdapter;
import com.jidesoft.app.framework.DataModelEvent;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.ProgressEvent;
import com.jidesoft.app.framework.ProgressListener;
import com.jidesoft.app.framework.SecondaryBasicDataModel;
import com.jidesoft.app.framework.activity.Activity;
import com.jidesoft.app.framework.activity.ActivityAction;
import com.jidesoft.app.framework.event.EventManager;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.TextFileFormat;
import com.jidesoft.app.framework.gui.ActionKeys;
import com.jidesoft.app.framework.gui.ApplicationMenuBarsUI;
import com.jidesoft.app.framework.gui.ApplicationWindowsUI;
import com.jidesoft.app.framework.gui.DataViewAdapter;
import com.jidesoft.app.framework.gui.DataViewEvent;
import com.jidesoft.app.framework.gui.GUIApplicationAction;
import com.jidesoft.app.framework.gui.MenuBarCustomizer;
import com.jidesoft.app.framework.gui.MenuConstants;
import com.jidesoft.app.framework.gui.MenuGroup;
import com.jidesoft.app.framework.gui.actions.ComponentAction;
import com.jidesoft.app.framework.gui.feature.AutoInstallActionsFeature;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.app.framework.gui.framed.DockableConfiguration;
import com.jidesoft.app.framework.gui.framed.DockingApplicationFeature;
import com.jidesoft.app.framework.gui.framed.ToggleFrameAction;
import com.jidesoft.docking.DockContext;

/**
 * DockedTextEditor2.java
 * <p/>
 * This example is similar to DockedTextEditor, but uses the
 * DockingApplicationFeature. No docking content it provided, this is just to
 * show how to facilitate Dockable DataViews. Notice that the DataModels used by
 * the docking are of secondary status. This is required.
 */
public class NafOptimizationRunner extends FileBasedApplication implements
		EventSubscriber
{
	static final String FILE_OPENED_EVENT_ID = "FileOpened";
	static final String FILE_CLOSED_EVENT_ID = "FileClosed";
	static final String FILE_SAVED_EVENT_ID = "FileSaved";
	static final String TUNING_ACTIVE_EVENT_ID = "TuningActive";
	static final String OPTIMIZATION_ACTIVE_EVENT_ID = "OptimizationActive";
	static final String WINDOW_RENAMED_EVENT_ID = "WindowRenamed";

	static final String CONSOLE_ACTION_ID = "Console";
	static final String STUDY_ACTION_ID = "Study";
	static final String CALCULATE_TUNING_ACTION_ID = "Calculate tuning";
	static final String OPTIMIZE_INSTRUMENT_ACTION_ID = "Optimize instrument";
	static final String CLEAR_CONSOLE_ACTION_ID = "Clear Console";
	static final String WARN_ON_DIRTY_CLOSE_ACTION_ID = "Warn on dirty close";
	static final String RENAME_WINIDOW_ACTION_ID = "Rename window";

	protected boolean isWarnOnDirtyClose = true;

	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		new NafOptimizationRunner().run(args);
	}

	public NafOptimizationRunner()
	{
		super("NAF Optimization Runner", TDI_APPLICATION_STYLE);

		// Set behavioe
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
		addFileMapping(new TextFileFormat("xml", "XML"), CodeEditorView.class);
		addDockedViews();
		addEvents();
		addWindowMenuToggles();
		addToolMenu();

		// The stock JDAF UndoAction and RedoAction are focused on the state of
		// the UndoManager of the focused DataModel. But the CodeEditor has its
		// own Undo and Redo actions. So we use a ComponentAction which will
		// automatically delegate to the CodeEditors Undo and Redo actions in
		// its
		// ActionMap when the CodeEditor is focused
		getActionMap().put(ActionKeys.UNDO, new ComponentAction("undo"));
		getActionMap().put(ActionKeys.REDO, new ComponentAction("redo"));

		addDataModelListener(new DataModelAdapter()
		{
			@Override
			public void dataModelOpened(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						FILE_OPENED_EVENT_ID, dataModelEvent.getDataModel());
			}

			@Override
			public void dataModelClosed(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						FILE_CLOSED_EVENT_ID, dataModelEvent.getDataModel());
			}

			@Override
			public void dataModelSaved(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						FILE_SAVED_EVENT_ID, dataModelEvent.getDataModel());
			}

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
				DataView view = event.getDataView();
				if (view instanceof CodeEditorView)
				{
					DataModel model = getDataModel(view);
					String name = model.getName();
					Action action = getActionMap()
							.get(RENAME_WINIDOW_ACTION_ID);
					if (name.length() > 0 && name.startsWith("Untitled"))
					{
						action.setEnabled(true);
					}
					else
					{
						action.setEnabled(false);
					}
				}
			}

		});

	}

	protected void addToolMenu()
	{
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
		String message = "Calculating instrument tuning.\nThis may take several seconds.";
		activity.addProgressListener(new BlockingProgressListener(
				getApplicationUIManager().getWindowsUI(),
				CALCULATE_TUNING_ACTION_ID, message));
		Action action = new ActivityAction(activity)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getApplication().getActivityManager().run(activity);
			}
		};
		getActionMap().put(CALCULATE_TUNING_ACTION_ID, action);
		action.setEnabled(false);

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
		optActivity.addProgressListener(new BlockingProgressListener(
				getApplicationUIManager().getWindowsUI(),
				OPTIMIZE_INSTRUMENT_ACTION_ID, message));
		action = new ActivityAction(optActivity)
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getApplication().getActivityManager().run(optActivity);
			}
		};
		getActionMap().put(OPTIMIZE_INSTRUMENT_ACTION_ID, action);
		action.setEnabled(false);

		action = new GUIApplicationAction(CLEAR_CONSOLE_ACTION_ID)
		{
			@Override
			public void actionPerformedDetached(ActionEvent e)
			{
				ConsoleView view = getConsoleView();
				view.clear();
			}
		};
		getActionMap().put(CLEAR_CONSOLE_ACTION_ID, action);

		action = new GUIApplicationAction(WARN_ON_DIRTY_CLOSE_ACTION_ID)
		{
			@Override
			public void actionPerformedDetached(ActionEvent event)
			{
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) event
						.getSource();
				isWarnOnDirtyClose = source.getState();
			}
		};
		getActionMap().put(WARN_ON_DIRTY_CLOSE_ACTION_ID, action);

		action = new GUIApplicationAction(RENAME_WINIDOW_ACTION_ID)
		{
			public void actionPerformedDetached(ActionEvent event)
			{
				DataModel focusedModel = getApplication().getFocusedModel();
				if (focusedModel != null)
				{
					String oldName = focusedModel.getName();
					String newName = JOptionPane.showInputDialog(
							getApplication().getApplicationUIManager()
									.getWindowsUI().getDialogParent(),
							"Enter a new name for " + oldName, oldName);
					if (newName != null && !newName.equals(oldName))
					{
						focusedModel.setName(newName);
					}
					getApplication().getEventManager().publish(
							WINDOW_RENAMED_EVENT_ID, focusedModel);
				}
			}
		};
		action.setEnabled(false);
		getActionMap().put(RENAME_WINIDOW_ACTION_ID, action);

		addMenuBarCustomizer(new MenuBarCustomizer()
		{
			public JMenu[] createApplicationMenus(
					ApplicationMenuBarsUI menuBarUI)
			{
				JMenu menu = menuBarUI.defaultMenu("Tool Menu", "Tool");
				menu.add(menuBarUI.getAction(CALCULATE_TUNING_ACTION_ID));
				menu.add(menuBarUI.getAction(OPTIMIZE_INSTRUMENT_ACTION_ID));
				return new JMenu[] { menu };
			}

			@Override
			public void customizeStandardMenu(String menuID, JMenu menu,
					ApplicationMenuBarsUI menuBarsUI)
			{
				if (menuID == WINDOW_MENU_ID)
				{
					MenuGroup group = menuBarsUI.getMenuGroup(
							WINDOW_USER_GROUP_ID, menu);
					group.addMenuItem(menuBarsUI
							.getAction(CLEAR_CONSOLE_ACTION_ID));
					JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(
							menuBarsUI.getAction(WARN_ON_DIRTY_CLOSE_ACTION_ID));
					cbItem.setSelected(isWarnOnDirtyClose);
					group.addMenuItem(menuBarsUI
							.getAction(RENAME_WINIDOW_ACTION_ID));
					group.addMenuItem(cbItem);
					group.insertSeparator(3);
					group.insertSeparator(5);
				}
			}
		});
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

		eventManager.subscribe(TUNING_ACTIVE_EVENT_ID, this);
		eventManager.subscribe(OPTIMIZATION_ACTIVE_EVENT_ID, this);
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
				dialog.setVisible(false);
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
		}
		else if (OPTIMIZATION_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(OPTIMIZE_INSTRUMENT_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
	}

	protected StudyView getStudyView()
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

}
