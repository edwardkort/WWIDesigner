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
import java.util.prefs.Preferences;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.MessageDialogRequest;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.tree.TreeUtils;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.gui.util.DataOpenException;
import com.wwidesigner.gui.util.HoleNumberMismatchException;
import com.wwidesigner.modelling.SketchInstrument;
import com.wwidesigner.optimization.Constraints;

/**
 * @author kort
 * 
 */
public class StudyView extends DataViewPane implements EventSubscriber
{
	private static final long serialVersionUID = 1L;

	private JTree tree;
	private StudyModel study;

	@Override
	protected void initializeComponents()
	{
		// create file tree
		tree = new JTree();
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
						if (StudyModel.INSTRUMENT_CATEGORY_ID.equals(category)
								|| StudyModel.TUNING_CATEGORY_ID
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

		getApplication().getEventManager().subscribe(
				NafOptimizationRunner.FILE_OPENED_EVENT_ID, this);
		getApplication().getEventManager().subscribe(
				NafOptimizationRunner.FILE_CLOSED_EVENT_ID, this);
		getApplication().getEventManager().subscribe(
				NafOptimizationRunner.FILE_SAVED_EVENT_ID, this);
		getApplication().getEventManager().subscribe(
				NafOptimizationRunner.WINDOW_RENAMED_EVENT_ID, this);
	}

	protected void updateView()
	{
		// Reset the Constaints category as needed.
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
			for (String name : study.getSubcategories(category))
			{
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
						name);
				if (childNode != null)
				{
					node.add(childNode);
					if (name.equals(selectedSub))
					{
						selectionPaths.add(new TreePath(childNode.getPath()));
					}
				}
			}
		}

		TreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);
		TreeUtils.expandAll(tree);
		tree.setSelectionPaths(selectionPaths.toArray(new TreePath[0]));
		updateActions();
	}

	protected void updateActions()
	{
		boolean canDoTuning = false;
		boolean canDoOptimization = false;
		try
		{
			canDoTuning = study.canTune();
			if (canDoTuning)
			{
				canDoOptimization = study.canOptimize();
			}
		}
		catch (Exception e)
		{
			MessageDialogRequest.showMessageDialog(getApplication(),
					e.getMessage(), "Input File Error",
					MessageDialogRequest.ERROR_STYLE);
		}

		getApplication().getEventManager().publish(
				NafOptimizationRunner.OPTIMIZATION_ACTIVE_EVENT_ID,
				canDoOptimization);
		getApplication().getEventManager().publish(
				NafOptimizationRunner.TUNING_ACTIVE_EVENT_ID, canDoTuning);
		String constraintsDirectory = ((NafOptimizationRunner) getApplication())
				.getConstraintsRootDirectoryPath();
		getApplication().getEventManager().publish(
				NafOptimizationRunner.CONSTRAINTS_ACTIVE_EVENT_ID,
				study.isOptimizerFullySpecified(constraintsDirectory));
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
				case NafOptimizationRunner.FILE_OPENED_EVENT_ID:
					try
					{
						study.addDataModel(source);
					}
					catch (Exception ex)
					{
						showException(ex);
					}
					break;
				case NafOptimizationRunner.FILE_CLOSED_EVENT_ID:
					study.removeDataModel(source);
					break;
				case NafOptimizationRunner.FILE_SAVED_EVENT_ID:
				case NafOptimizationRunner.WINDOW_RENAMED_EVENT_ID:
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
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void graphTuning()
	{
		try
		{
			study.graphTuning("Tuning"); // This a title, not a constant
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void getDefaultConstraints()
	{
		try
		{
			String xmlConstraints = study.getDefaultConstraints();
			addNewDataModel(xmlConstraints);
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void getBlankConstraints()
	{
		try
		{
			String xmlConstraints = study.getBlankConstraints();
			addNewDataModel(xmlConstraints);
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void optimizeInstrument()
	{
		try
		{
			String xmlInstrument = study.optimizeInstrument();
			addNewDataModel(xmlInstrument);
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void addNewDataModel(String xmlData) throws Exception
	{
		if (xmlData != null && !xmlData.isEmpty())
		{
			FileBasedApplication app = (FileBasedApplication) getApplication();
			FileDataModel data = (FileDataModel) app.newData("xml");
			data.setData(xmlData);
			study.addDataModel(data);
			updateView();
		}
	}

	public void sketchInstrument()
	{
		try
		{
			SketchInstrument sketch = new SketchInstrument();
			sketch.draw(study.getInstrument(), false);
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void compareInstrument()
	{
		try
		{
			FileBasedApplication app = (FileBasedApplication) getApplication();
			DataModel data = app.getFocusedModel();
			if (data != null)
			{
				DataView view = app.getDataView(data);
				if (view != null && view instanceof XmlToggleView)
				{
					String xmlInstrument2 = ((XmlToggleView) view).getText();
					Instrument instrument2 = StudyModel
							.getInstrument(xmlInstrument2);
					if (instrument2 == null)
					{
						System.out.print("\nError: Current editor tab, ");
						System.out.print(data.getName());
						System.out.println(", is not a valid instrument.");
						System.out
								.println("Select the edit tab for a valid instrument.");
						return;
					}
					study.compareInstrument(data.getName(), instrument2);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
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
					study.addDataModel((FileDataModel) model);
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
			setStudyModel(new NafStudyModel());
		}
		else if (studyClassName
				.contentEquals(OptimizationPreferences.WHISTLE_STUDY_NAME))
		{
			setStudyModel(new WhistleStudyModel());
		}
		else
		{
			// Default study model.
			setStudyModel(new WhistleStudyModel());
		}
	}

	protected void showException(Exception exception)
	{
		String exceptionType;
		int messageType;
		if (exception instanceof DataOpenException)
		{
			DataOpenException doException = (DataOpenException) exception;
			exceptionType = doException.getType();
			messageType = doException.isWarning() ? MessageDialogRequest.WARNING_STYLE
					: MessageDialogRequest.ERROR_STYLE;
		}
		else if (exception instanceof HoleNumberMismatchException)
		{
			exceptionType = "Hole number mismatch";
			messageType = MessageDialogRequest.WARNING_STYLE;
		}
		else
		{
			exceptionType = "Data content is invalid";
			messageType = MessageDialogRequest.ERROR_STYLE;
		}

		MessageDialogRequest.showMessageDialog(getApplication(),
				exception.getMessage(), exceptionType, messageType);
	}

	public File getConstraintsLeafDirectory(String rootDirectoryPath)
	{
		return study.getConstraintsLeafDirectory(rootDirectoryPath);
	}

	public File getConstraintsLeafDirectory(String rootDirectoryPath, Constraints constraints)
	{
		return study.getConstraintsLeafDirectory(rootDirectoryPath, constraints);
	}
}
