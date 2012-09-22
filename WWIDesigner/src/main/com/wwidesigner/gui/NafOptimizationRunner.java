package com.wwidesigner.gui;

import com.jidesoft.app.framework.ApplicationVetoException;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelAdapter;
import com.jidesoft.app.framework.DataModelEvent;
import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.JDAFConstants;
import com.jidesoft.app.framework.SecondaryBasicDataModel;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.file.FileFormat;
import com.jidesoft.app.framework.file.TextFileFormat;
import com.jidesoft.app.framework.gui.ActionKeys;
import com.jidesoft.app.framework.gui.ApplicationWindowsUI;
import com.jidesoft.app.framework.gui.DataViewAdapter;
import com.jidesoft.app.framework.gui.DataViewEvent;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.GUIApplication;
import com.jidesoft.app.framework.gui.MenuConstants;
import com.jidesoft.app.framework.gui.MessageDialogRequest;
import com.jidesoft.app.framework.gui.SplitApplicationUI;
import com.jidesoft.app.framework.gui.actions.ComponentAction;
import com.jidesoft.app.framework.gui.feature.AutoInstallActionsFeature;
import com.jidesoft.app.framework.gui.feature.GlobalSelectionFeature;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.app.framework.gui.filebased.FileIcon;
import com.jidesoft.app.framework.gui.filebased.OpenFileAction;
import com.jidesoft.app.framework.gui.framed.DockableConfiguration;
import com.jidesoft.app.framework.gui.framed.DockingApplicationFeature;
import com.jidesoft.app.framework.gui.framed.FrameConfiguration;
import com.jidesoft.app.framework.gui.framed.FramedApplicationFeature;
import com.jidesoft.app.framework.gui.framed.ToggleFrameAction;
import com.jidesoft.docking.DockContext;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.tree.TreeUtils;
import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 * DockedTextEditor2.java
 * <p/>
 * This example is similar to DockedTextEditor, but uses the DockingApplicationFeature. No docking content it provided,
 * this is just to show how to facilitate Dockable DataViews. Notice that the DataModels used by the docking are of
 * secondary status. This is required.
 */
public class NafOptimizationRunner extends FileBasedApplication {

    public static void main(String[] args) {
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
        new NafOptimizationRunner().run(args);
    }

    public NafOptimizationRunner() {
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
        config.setInitIndex(0);
        config.setDataModelClass(SecondaryBasicDataModel.class);
        config.setDataViewClass(DockedView.class);
        docking.addDockableMapping(config);
        
        // config for "Study" pane
        config = new DockableConfiguration();
        config.setFrameName("Study");
        config.setInitState(DockContext.STATE_FRAMEDOCKED);
        config.setInitSide(DockContext.DOCK_SIDE_WEST);
        config.setInitIndex(0);
        config.setDataModelClass(SecondaryBasicDataModel.class);
        config.setDataViewClass(DockedView.class);
        docking.addDockableMapping(config);

        // add feature
        addApplicationFeature(docking);
        
        Action action = new OpenFileAction("Instrument", true);
        action.putValue(AutoInstallActionsFeature.MENU_ID, MenuConstants.FILE_MENU_ID);
        action.putValue(Action.NAME, "Open Instrument...");
        getActionMap().put("instrument", action);

        action = new OpenFileAction("Tuning", true);
        action.putValue(AutoInstallActionsFeature.MENU_ID, MenuConstants.FILE_MENU_ID);
        action.putValue(Action.NAME, "Open Tuning...");
        getActionMap().put("tuning", action);

        getActionMap().get("open").setEnabled(false);

        // application settings
        setExitApplicationOnLastDataView(false);
        setNewDataOnRun(false);
        
        action = new ToggleFrameAction("Console", true);
        action.putValue(AutoInstallActionsFeature.MENU_ID, MenuConstants.WINDOW_MENU_ID);
        getActionMap().put("consoleToggle", action);

        action = new ToggleFrameAction("Study", true);
        action.putValue(AutoInstallActionsFeature.MENU_ID, MenuConstants.WINDOW_MENU_ID);
        getActionMap().put("studyToggle", action);

        // The stock JDAF UndoAction and RedoAction are focused on the state of
        // the UndoManager of the focused DataModel. But the CodeEditor has its
        // own Undo and Redo actions. So we use a ComponentAction which will
        // automatically delegate to the CodeEditors Undo and Redo actions in its
        // ActionMap when the CodeEditor is focused
        getActionMap().put(ActionKeys.UNDO, new ComponentAction("undo"));
        getActionMap().put(ActionKeys.REDO, new ComponentAction("redo"));

        // add global selection
        addApplicationFeature(new GlobalSelectionFeature(false, true));

        // set the global selection when a file editor is activated
        addDataViewListener(new DataViewAdapter() {
            @Override
            public void dataViewActivated(DataViewEvent e) {
                DataModel model = getDataModel(e.getDataView());
                if (e.isPrimary() && model instanceof FileDataModel) {
                    File file = ((FileDataModel) model).getFile();

                    // global selection
                    GlobalSelectionFeature.getFeature(NafOptimizationRunner.this).setSelection(file);
                }
            }
        });

        // window size
        ApplicationWindowsUI windowsUI = getApplicationUIManager().getWindowsUI();
        windowsUI.setPreferredWindowSize(windowsUI.getPreferredMaximumWindowSize());

        getApplicationUIManager().setUseJideDocumentPane(true);
        addDataModelListener(new DataModelAdapter() {
            @Override
            public void dataModelClosing(DataModelEvent dataModelEvent) throws ApplicationVetoException {
                DataModel dataModel = dataModelEvent.getDataModel();
                if (dataModel.isDirty()) {

                    int reply = MessageDialogRequest
                            .showConfirmDialog(NafOptimizationRunner.this, "Would you like to save before closing", "Warning",
                                    MessageDialogRequest.YES_NO_CANCEL_DIALOG,
                                    MessageDialogRequest.WARNING_STYLE);
                    if (reply == JDAFConstants.RESPONSE_NO) {
                        return;
                    }
                    else if (reply == JDAFConstants.RESPONSE_YES) {
                        try {
                            dataModel.saveData();
                        }
                        catch (DataModelException ex) {
                            throw new ApplicationVetoException("Failed to save data", ex);
                        }
                    }
                    else {
                        //the user cancelled
                        throw new ApplicationVetoException();
                    }
                }
            }
        });
    }
    

    public static class DockedView extends DataViewPane {
        protected void initializeComponents() {
            setPreferredSize(new Dimension(300, 600));
        }
    }
}
