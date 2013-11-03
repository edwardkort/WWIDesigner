/**
 * 
 */
package com.wwidesigner.gui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.tree.TreeUtils;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.gui.StudyModel.Category;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

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
						study.setCategorySelection(
								(Category) parentNode.getUserObject(),
								(String) node.getUserObject());
					}
					updateView();
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(225, 100));
		add(scrollPane);

		Preferences myPreferences = getApplication().getPreferences();
		String modelName = myPreferences.get(OptimizationPreferences.STUDY_MODEL_OPT,
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
		// Build the selection tree.

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		List<TreePath> selectionPaths = new ArrayList<TreePath>();
		
		// Add all static categories and selection options to the tree.

		for (Category category : study.getCategories())
		{
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
			if (node != null)
			{
				node.setAllowsChildren(true);
				root.add(node);
			}
			Map<String, Object> subcategories = category.getSubs();
			String selectedSub = category.getSelectedSub();
			for (String name : subcategories.keySet())
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
		boolean canDo = study.canTune();
		getApplication().getEventManager().publish(
				NafOptimizationRunner.TUNING_ACTIVE_EVENT_ID, canDo);

		canDo = study.canOptimize();
		getApplication().getEventManager().publish(
				NafOptimizationRunner.OPTIMIZATION_ACTIVE_EVENT_ID, canDo);
	}

	@Override
	public void doEvent(SubscriberEvent event)
	{
		String eventId = event.getEvent();
		DataModel source = (DataModel) event.getSource();
		if (source instanceof FileDataModel)
		{
			String data = (String) ((FileDataModel) source).getData();
			String categoryName = getCategoryName(data);
			if (categoryName != null)
			{
				Category category = study.getCategory(categoryName);
				String subName = source.getName();
				switch (eventId)
				{
					case NafOptimizationRunner.FILE_OPENED_EVENT_ID:
						category.addSub(subName, source);
						category.setSelectedSub(subName);
						break;
					case NafOptimizationRunner.FILE_CLOSED_EVENT_ID:
						category.removeSub(subName);
						break;
					case NafOptimizationRunner.FILE_SAVED_EVENT_ID:
					case NafOptimizationRunner.WINDOW_RENAMED_EVENT_ID:
						category.replaceSub(subName, (FileDataModel) source);
						break;
				}
				updateView();
			}
		}
	}

	protected String getCategoryName(String xmlString)
	{
		// Check Instrument
		BindFactory bindFactory = GeometryBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Instrument", true)) // TODO Make
																	// constants
																	// in
																	// binding
																	// framework
		{
			return StudyModel.INSTRUMENT_CATEGORY_ID;
		}

		// Check Tuning
		bindFactory = NoteBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Tuning", true)) // TODO Make
																// constants in
																// binding
																// framework
		{
			return StudyModel.TUNING_CATEGORY_ID;
		}

		return null;
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

	public void optimizeInstrument()
	{
		try
		{
			String xmlInstrument = study.optimizeInstrument();
			FileBasedApplication app = (FileBasedApplication) getApplication();
			DataModel data = app.newData("xml");
			((BasicDataModel)data).setData(xmlInstrument);
			addDataModelToStudy(data);
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
				CodeEditorView view = (CodeEditorView) app.getDataView(data);
				if (view != null)
				{
					String xmlInstrument2 = view.getText();
					Instrument  instrument2 = study.getInstrument(xmlInstrument2);
					if (instrument2 == null)
					{
						System.out.print("\nError: Current editor tab, ");
						System.out.print(data.getName());
						System.out.println(", is not a valid instrument.");
						System.out.println("Select the edit tab for a valid instrument.");
						return;
					}
					study.compareInstrument(data.getName(), instrument2 );
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void addDataModelToStudy(DataModel dataModel)
	{
		if (dataModel instanceof FileDataModel)
		{
			String data = (String) ((FileDataModel) dataModel).getData();
			String categoryName = getCategoryName(data);
			if (categoryName != null)
			{
				Category category = study.getCategory(categoryName);
				category.addSub(dataModel.getName(), dataModel);
				updateView();
			}
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
		DataModel[] models = getApplication().getDataModels();
		this.study = study;
		updateView();
		for ( DataModel model : models )
		{
			addDataModelToStudy(model);
		}
	}

	/**
	 * Set the study model to a specified object.
	 */
	public void setStudyModel(String studyClassName)
	{
		if (studyClassName.contentEquals(OptimizationPreferences.NAF_STUDY_NAME))
		{
			setStudyModel( new NafStudyModel() );
		}
		else if (studyClassName.contentEquals(OptimizationPreferences.WHISTLE_STUDY_NAME))
		{
			setStudyModel( new WhistleStudyModel() );
		}
		else
		{
			// Default study model.
			setStudyModel( new WhistleStudyModel() );
		}
	}

}
