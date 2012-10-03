package com.wwidesigner.gui;

import java.awt.Dimension;
import java.io.File;

import javax.swing.Action;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelAdapter;
import com.jidesoft.app.framework.DataModelEvent;
import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.JDAFConstants;
import com.jidesoft.app.framework.SecondaryBasicDataModel;
import com.jidesoft.app.framework.event.EventManager;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.file.TextFileFormat;
import com.jidesoft.app.framework.gui.ActionKeys;
import com.jidesoft.app.framework.gui.ApplicationWindowsUI;
import com.jidesoft.app.framework.gui.DataViewAdapter;
import com.jidesoft.app.framework.gui.DataViewEvent;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.MenuConstants;
import com.jidesoft.app.framework.gui.MessageDialogRequest;
import com.jidesoft.app.framework.gui.actions.ComponentAction;
import com.jidesoft.app.framework.gui.feature.AutoInstallActionsFeature;
import com.jidesoft.app.framework.gui.feature.GlobalSelectionFeature;
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
public class NafOptimizationRunner extends FileBasedApplication
{

	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		new NafOptimizationRunner().run(args);
	}

	public NafOptimizationRunner()
	{
		super("NAF Optimization Runner", TDI_APPLICATION_STYLE);
		addFileMapping(new TextFileFormat("xml", "XML"), CodeEditorView.class);

		getApplicationUIManager().setUseJideDockingFramework(true);
		getApplicationUIManager().setUseJideActionFramework(true);

		addApplicationFeature(new AutoInstallActionsFeature());

		// no single tab display

		DockingApplicationFeature docking = new DockingApplicationFeature();
		DockableConfiguration config = new DockableConfiguration();

		// config for "Console" model-view
		config.setFrameName("Console");
		config.setInitState(DockContext.STATE_FRAMEDOCKED);
		config.setInitSide(DockContext.DOCK_SIDE_SOUTH);
		config.setInitIndex(1);
		config.setCriteria(System.out.toString());
		config.setDataModelClass(SecondaryBasicDataModel.class);
		config.setDataViewClass(TextView.class);
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

		// application settings
		setExitApplicationOnLastDataView(false);
		setNewDataOnRun(false);

		Action action = new ToggleFrameAction("Console", true);
		action.putValue(AutoInstallActionsFeature.MENU_ID,
				MenuConstants.WINDOW_MENU_ID);
		getActionMap().put("consoleToggle", action);

		action = new ToggleFrameAction("Study", true);
		action.putValue(AutoInstallActionsFeature.MENU_ID,
				MenuConstants.WINDOW_MENU_ID);
		getActionMap().put("studyToggle", action);

		// The stock JDAF UndoAction and RedoAction are focused on the state of
		// the UndoManager of the focused DataModel. But the CodeEditor has its
		// own Undo and Redo actions. So we use a ComponentAction which will
		// automatically delegate to the CodeEditors Undo and Redo actions in
		// its
		// ActionMap when the CodeEditor is focused
		getActionMap().put(ActionKeys.UNDO, new ComponentAction("undo"));
		getActionMap().put(ActionKeys.REDO, new ComponentAction("redo"));

		// add global selection
		addApplicationFeature(new GlobalSelectionFeature(false, true));

		// set the global selection when a file editor is activated
		addDataViewListener(new DataViewAdapter()
		{
			@Override
			public void dataViewActivated(DataViewEvent e)
			{
				DataModel model = getDataModel(e.getDataView());
				if (e.isPrimary() && model instanceof FileDataModel)
				{
					File file = ((FileDataModel) model).getFile();

					// global selection
					GlobalSelectionFeature.getFeature(
							NafOptimizationRunner.this).setSelection(file);
				}
			}
		});

		// window size
		ApplicationWindowsUI windowsUI = getApplicationUIManager()
				.getWindowsUI();
		windowsUI.setPreferredWindowSize(windowsUI
				.getPreferredMaximumWindowSize());

		EventManager eventManager = getEventManager();
		eventManager.addEvent("FileOpened");
		eventManager.addEvent("FileClosed");

		getApplicationUIManager().setUseJideDocumentPane(true);
		addDataModelListener(new DataModelAdapter()
		{
			@Override
			public void dataModelClosing(DataModelEvent dataModelEvent)
					throws ApplicationVetoException
			{
				DataModel dataModel = dataModelEvent.getDataModel();
				if (dataModel.isDirty())
				{

					int reply = MessageDialogRequest.showConfirmDialog(
							NafOptimizationRunner.this,
							"Would you like to save before closing", "Warning",
							MessageDialogRequest.YES_NO_CANCEL_DIALOG,
							MessageDialogRequest.WARNING_STYLE);
					if (reply == JDAFConstants.RESPONSE_NO)
					{
						return;
					}
					else if (reply == JDAFConstants.RESPONSE_YES)
					{
						try
						{
							dataModel.saveData();
						}
						catch (DataModelException ex)
						{
							throw new ApplicationVetoException(
									"Failed to save data", ex);
						}
					}
					else
					{
						// the user cancelled
						throw new ApplicationVetoException();
					}
				}
			}

			@Override
			public void dataModelOpened(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						"FileOpened", dataModelEvent);
			}

			@Override
			public void dataModelClosed(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						"FileClosed", dataModelEvent);
			}
		});

	}

	public static class DockedView extends DataViewPane
	{
		protected void initializeComponents()
		{
			setPreferredSize(new Dimension(300, 600));
		}
	}

	public static class SecondaryBasicDataModel2 extends
			SecondaryBasicDataModel
	{
	}
}
