/**
 * Class to allow users to interact with instrument study models to analyze and optimize instruments.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.gui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.bind.MarshalException;
import javax.xml.bind.UnmarshalException;

import org.apache.commons.math3.exception.ZeroException;
import org.xml.sax.SAXParseException;

import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.MessageDialogRequest;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.app.framework.gui.framed.DockingApplicationFeature;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.tooltip.ExpandedTipUtils;
import com.jidesoft.tree.TreeUtils;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.gui.util.DataOpenException;
import com.wwidesigner.gui.util.HoleNumberMismatchException;
import com.wwidesigner.gui.util.InstrumentTypeException;
import com.wwidesigner.gui.util.OptimizerMismatchException;
import com.wwidesigner.modelling.SketchInstrument;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.InvalidFieldException;
import com.wwidesigner.util.OperationCancelledException;

/**
 * @author kort
 * 
 */
public class StudyView extends DataViewPane implements EventSubscriber
{
	private static final long serialVersionUID = 1L;

	JTree tree;
	StudyModel study;

	@Override
	protected void initializeComponents()
	{
		// create file tree
		tree = new JTree()
		{
			@Override
			public String getToolTipText(MouseEvent e)
			{
				String tip = null;
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null)
				{
					if (path.getPathCount() == 3) // it is a leaf
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
								.getLastPathComponent();
						if (node instanceof TreeNodeWithToolTips)
						{
							tip = ((TreeNodeWithToolTips) node).getToolTip();
						}
					}
				}
				return tip == null ? getToolTipText() : tip;
			}
		};
		// Show tooltips for the Study view, and let them persist for 8 seconds.
		ToolTipManager.sharedInstance().registerComponent(tree);
		ToolTipManager.sharedInstance().setDismissDelay(8000);
		// If a Study view node doesn't fit in the pane, expand it when hovering
		// over it.
		ExpandedTipUtils.install(tree);
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null)
				{
					if (path.getPathCount() == 3) // it is a leaf
					{
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
								.getLastPathComponent();
						DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node
								.getParent();
						String category = (String) parentNode.getUserObject();
						study.setCategorySelection(category,
								(String) node.getUserObject());
						if (CategoryType.INSTRUMENT_CATEGORY_ID.equals(category)
								|| CategoryType.TUNING_CATEGORY_ID
										.equals(category))
						{
							try
							{
								study.validHoleCount();
							}
							catch (Exception ex)
							{
								showException(ex);
							}
						}
					}
					updateView();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(225, 100));
		add(scrollPane);

		Preferences myPreferences = getApplication().getPreferences();
		String modelName = myPreferences.get(
				OptimizationPreferences.STUDY_MODEL_OPT,
				OptimizationPreferences.NAF_STUDY_NAME);
		setStudyModel(modelName);
		study.setPreferences(myPreferences);

		getApplication().getEventManager()
				.subscribe(WIDesigner.FILE_OPENED_EVENT_ID, this);
		getApplication().getEventManager()
				.subscribe(WIDesigner.FILE_CLOSED_EVENT_ID, this);
		getApplication().getEventManager()
				.subscribe(WIDesigner.FILE_SAVED_EVENT_ID, this);
		getApplication().getEventManager()
				.subscribe(WIDesigner.WINDOW_RENAMED_EVENT_ID, this);
	}

	protected void updateView()
	{
		// Reset the Constraints category as needed.
		study.updateConstraints();

		// Build the selection tree.
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		List<TreePath> selectionPaths = new ArrayList<TreePath>();

		// Add all static categories and selection options to the tree.
		for (String category : study.getCategoryNames())
		{
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
			if (node != null)
			{
				node.setAllowsChildren(true);
				root.add(node);
			}
			String selectedSub = study.getSelectedSub(category);
			Map<String, String> toolTips = study.getCategory(category)
					.getToolTips();
			for (String name : study.getSubcategories(category))
			{
				TreeNodeWithToolTips childNode = new TreeNodeWithToolTips(name);
				if (childNode != null)
				{
					node.add(childNode);
					if (name.equals(selectedSub))
					{
						selectionPaths.add(new TreePath(childNode.getPath()));
					}
					String tip = toolTips.get(name);
					if (tip != null)
					{
						childNode.setToolTip(tip);
					}
				}
			}
		}

		TreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);
		TreeUtils.expandAll(tree);
		tree.setSelectionPaths(selectionPaths.toArray(new TreePath[0]));
		setStudyViewName();
		// Update on the appropriate thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				updateActions();
			}
		});
	}

	protected void setStudyViewName()
	{
		DockingApplicationFeature docking = (DockingApplicationFeature) getApplication()
				.getApplicationFeature(DockingApplicationFeature.class);
		DockableFrame frame = docking.getDockableFrame(this);
		frame.setTitle(study.getDisplayName());

	}

	protected void updateActions()
	{
		boolean isInstrumentSelected = false;
		boolean canDoTuning = false;
		boolean canDoOptimization = false;
		boolean canGraphNote = false;
		String selectedInstrumentName;
		selectedInstrumentName = study.getSelectedInstrumentName();
		isInstrumentSelected = (selectedInstrumentName != null
				&& selectedInstrumentName != "");
		if (isInstrumentSelected)
		{
			canDoTuning = study.canTune();
			if (canDoTuning)
			{
				canDoOptimization = study.canOptimize();
			}
			canGraphNote = getSelectedFingering() != null;
		}
		else
		{
			// Event source cannot be null;
			selectedInstrumentName = "";
		}

		getApplication().getEventManager().publish(
				WIDesigner.OPTIMIZATION_ACTIVE_EVENT_ID, canDoOptimization);
		getApplication().getEventManager()
				.publish(WIDesigner.TUNING_ACTIVE_EVENT_ID, canDoTuning);
		String constraintsDirectory = ((WIDesigner) getApplication())
				.getConstraintsRootDirectoryPath();
		getApplication().getEventManager().publish(
				WIDesigner.CONSTRAINTS_ACTIVE_EVENT_ID,
				study.isOptimizerFullySpecified(constraintsDirectory));
		getApplication().getEventManager().publish(
				WIDesigner.CONSTRAINTS_CAN_CREATE_EVENT_ID,
				study.isOptimizerCreateSpecified());
		getApplication().getEventManager().publish(
				WIDesigner.INSTRUMENT_SELECTED_EVENT_ID,
				selectedInstrumentName);
		getApplication().getEventManager()
				.publish(WIDesigner.NOTE_SELECTED_EVENT_ID, canGraphNote);
	}

	@Override
	public void doEvent(SubscriberEvent event)
	{
		String eventId = event.getEvent();
		Object eventSource = event.getSource();
		if (eventSource instanceof FileDataModel)
		{
			FileDataModel source = (FileDataModel) eventSource;
			switch (eventId)
			{
				case WIDesigner.FILE_OPENED_EVENT_ID:
					try
					{
						study.addDataModel(source, false);
					}
					catch (Exception ex)
					{
						showException(ex);
					}
					break;
				case WIDesigner.FILE_CLOSED_EVENT_ID:
					study.removeDataModel(source);
					break;
				case WIDesigner.FILE_SAVED_EVENT_ID:
				case WIDesigner.WINDOW_RENAMED_EVENT_ID:
					try
					{
						study.replaceDataModel(source);
					}
					catch (DataOpenException ex)
					{
						showException(ex);
					}
					break;
			}
			updateView();
		}
	}

	public void getTuning()
	{
		try
		{
			study.calculateTuning("Tuning"); // This a title, not a constant
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void getSupplementaryInfo()
	{
		try
		{
			study.calculateSupplementaryInfo("Supplementary Information");
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void graphTuning()
	{
		try
		{
			study.graphTuning("Tuning"); // This a title, not a constant
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void getDefaultConstraints()
	{
		try
		{
			String xmlConstraints = study.getDefaultConstraints(
					getApplication().getApplicationUIManager().getWindowsUI()
							.getDialogParent());
			// For the cancelled hole-grouping scenario
			if (xmlConstraints != null)
			{
				addNewDataModel(xmlConstraints);
			}
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void getBlankConstraints()
	{
		try
		{
			String xmlConstraints = study.getBlankConstraints(
					getApplication().getApplicationUIManager().getWindowsUI()
							.getDialogParent());
			// For the cancelled hole-grouping scenario
			if (xmlConstraints != null)
			{
				addNewDataModel(xmlConstraints);
			}
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void optimizeInstrument()
	{
		try
		{
			String xmlInstrument = study.optimizeInstrument();
			addNewDataModel(xmlInstrument);
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void cancelOptimization()
	{
		study.cancelOptimization();
	}

	private void addNewDataModel(String xmlData) throws Exception
	{
		if (xmlData != null && !xmlData.isEmpty())
		{
			FileBasedApplication app = (FileBasedApplication) getApplication();
			FileDataModel data = (FileDataModel) app.newData("xml");
			// At this point, we have a data model and data view that are still
			// empty. Load the data into the data model, then re-load the
			// data view from the data model.
			data.setData(xmlData);
			data.setDirty(true);
			// If a view has been assigned, update the study's data models.
			DataView view = app.getDataView(data);
			if (view != null)
			{
				view.updateView(data);
				study.addDataModel(data, true);
				updateView();
			}
		}
	}

	public void sketchInstrument()
	{
		try
		{
			SketchInstrument sketch = new SketchInstrument();
			sketch.draw(study.getInstrument(),
					study.getSelectedInstrumentName(), false);
		}
		catch (Exception ex)
		{
			showException(ex);
		}
	}

	public void compareInstrument()
	{
		try
		{
			FileBasedApplication app = (FileBasedApplication) getApplication();
			DataModel data = app.getFocusedModel();
			DataView view = app.getDataView(data);
			String xmlInstrument2 = ((XmlToggleView) view).getText();
			Instrument instrument2 = StudyModel.getInstrument(xmlInstrument2);
			LengthType defaultLengthType = ((WIDesigner) getApplication())
					.getApplicationLengthType();
			study.compareInstrument(data.getName(), instrument2,
					defaultLengthType);
		}
		catch (Exception e)
		{
			showException(e);
		}
	}

	public Fingering getSelectedFingering()
	{
		FileBasedApplication app = (FileBasedApplication) getApplication();
		if (app == null)
		{
			return null;
		}
		DataModel data = app.getFocusedModel();
		if (data == null)
		{
			return null;
		}
		DataView view = app.getDataView(data);
		if (view == null)
		{
			return null;
		}
		ContainedXmlView contained = ((XmlToggleView) view).getCurrentView();
		if (contained instanceof ContainedTuningView)
		{
			return ((ContainedTuningView) contained).getSelectedFingering();
		}
		else if (contained instanceof ContainedNafTuningView)
		{
			return ((ContainedNafTuningView) contained).getSelectedFingering();
		}
		return null;
	}

	public void graphNote()
	{
		try
		{
			Fingering fingering = getSelectedFingering();
			// Put in to show an understandable error.
			if (fingering == null)
			{
				throw new Exception(
						"Cannot retrieve a selected fingering.  Select a fingering on the turning panel.");
			}
			study.graphNote(fingering);
		}
		catch (Exception e)
		{
			showException(e);
		}
	}

	public StudyModel getStudyModel()
	{
		return study;
	}

	/**
	 * Set the study model given a study class name.
	 */
	public void setStudyModel(StudyModel study)
	{
		this.study = study;

		DataModel[] models = getApplication().getDataModels();
		for (DataModel model : models)
		{
			if (model instanceof FileDataModel)
			{
				try
				{
					study.addDataModel((FileDataModel) model, false);
				}
				catch (Exception ex)
				{
					showException(ex);
				}
			}
		}
		updateView();
	}

	/**
	 * Set the study model to a specified object.
	 */
	public void setStudyModel(String studyClassName)
	{
		if (studyClassName
				.contentEquals(OptimizationPreferences.NAF_STUDY_NAME))
		{
			setStudyModel(
					new NafStudyModel(getApplication().getApplicationUIManager()
							.getWindowsUI().getDialogParent()));
		}
		else if (studyClassName
				.contentEquals(OptimizationPreferences.WHISTLE_STUDY_NAME))
		{
			setStudyModel(new WhistleStudyModel());
		}
		else if (studyClassName
				.contentEquals(OptimizationPreferences.FLUTE_STUDY_NAME))
		{
			setStudyModel(new FluteStudyModel());
		}
		else if (studyClassName
				.contentEquals(OptimizationPreferences.REED_STUDY_NAME))
		{
			setStudyModel(new ReedStudyModel());
		}
		else
		{
			// Default study model.
			setStudyModel(new WhistleStudyModel());
		}
	}

	/**
	 * Display a message box reporting a processing exception.
	 * 
	 * @param ex
	 *            - the exception encountered.
	 */
	public void showException(Exception ex)
	{
		Exception exception = ex;
		Throwable cause = exception.getCause();
		if (exception instanceof DataModelException
				&& cause instanceof Exception)
		{
			// We use DataModelExceptions as a wrapper for more specific cause
			// exception.
			exception = (Exception) cause;
			cause = exception.getCause();
		}
		int messageType = MessageDialogRequest.ERROR_STYLE; // Message box
															// style.
		final String exceptionType; // Message box title.
		String exceptionMessage = exception.getMessage(); // Message box text.
		boolean withTrace = false; // true to print on console log.

		if (exception instanceof DataOpenException)
		{
			DataOpenException doException = (DataOpenException) exception;
			exceptionType = doException.getType();
			messageType = doException.isWarning()
					? MessageDialogRequest.WARNING_STYLE
					: MessageDialogRequest.ERROR_STYLE;
		}
		else if (exception instanceof InvalidFieldException)
		{
			InvalidFieldException fldException = (InvalidFieldException) exception;
			exceptionType = fldException.getLabel();
			fldException.printMessages();
		}
		else if (exception instanceof HoleNumberMismatchException)
		{
			exceptionType = "Hole number mismatch";
			messageType = MessageDialogRequest.WARNING_STYLE;
		}
		else if (exception instanceof UnmarshalException
				|| exception instanceof MarshalException)
		{
			exceptionType = "Invalid XML Definition";
			exceptionMessage = "Invalid XML structure.\n";
			exceptionMessage += exception.getCause().getMessage();
			if (cause instanceof SAXParseException)
			{
				SAXParseException parseEx = (SAXParseException) cause;
				exceptionMessage += "\nAt line " + parseEx.getLineNumber()
						+ ", column " + parseEx.getColumnNumber() + ".";
			}
		}
		else if (exception instanceof DataModelException)
		{
			withTrace = true;
			exceptionType = "Error in data model";
		}
		else if (exception instanceof ZeroException)
		{
			exceptionType = "Operation cannot be performed";
			exceptionMessage = "Optimization requires at least 1 variable.";
			messageType = MessageDialogRequest.ERROR_STYLE;
		}
		else if (exception instanceof OperationCancelledException)
		{
			exceptionType = "Operation cancelled";
			messageType = MessageDialogRequest.WARNING_STYLE;
		}
		else if (exception instanceof OptimizerMismatchException
				|| exception instanceof InstrumentTypeException)
		{
			exceptionType = "Incorrect selection";
			messageType = MessageDialogRequest.ERROR_STYLE;
		}
		else
		{
			withTrace = true;
			exceptionType = "Operation failed";
		}

		// showException can be called on any thread, but message dialog must
		// run on the AWT thread.
		final String dialogMessage = exceptionMessage;
		final int dialogType = messageType;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				MessageDialogRequest.showMessageDialog(getApplication(),
						dialogMessage, exceptionType, dialogType);
			}
		});
		if (withTrace)
		{
			System.out.println(exception.getClass().getName() + " Exception: "
					+ exceptionMessage);
			exception.printStackTrace();
		}
	}

	public File getConstraintsLeafDirectory(String rootDirectoryPath)
	{
		return study.getConstraintsLeafDirectory(rootDirectoryPath);
	}

	public File getConstraintsLeafDirectory(String rootDirectoryPath,
			Constraints constraints)
	{
		return study.getConstraintsLeafDirectory(rootDirectoryPath,
				constraints);
	}

	class TreeNodeWithToolTips extends DefaultMutableTreeNode
	{
		private String toolTip;
		private String displayName;

		public TreeNodeWithToolTips(Object userObject)
		{
			super(userObject);

			if (userObject != null)
			{
				String key = (String) userObject;
				if (key.contains(File.separator))
				{
					displayName = key
							.substring(key.lastIndexOf(File.separator) + 1);
					setToolTip(key);
				}
				else
				{
					displayName = key;
				}
			}
			else
			{
				displayName = new String();
			}
		}

		public void setToolTip(String tip)
		{
			toolTip = tip;
		}

		public String getToolTip()
		{
			return toolTip;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}
}
