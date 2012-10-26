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

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelEvent;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileDataModel;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.tree.TreeUtils;
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

		study = new StudyModel();
		updateView();

		getApplication().getEventManager().subscribe(NafOptimizationRunner.FILE_OPENED_EVENT_ID, this);
		getApplication().getEventManager().subscribe(NafOptimizationRunner.FILE_CLOSED_EVENT_ID, this);
		getApplication().getEventManager().subscribe(NafOptimizationRunner.FILE_SAVED_EVENT_ID, this);
	}

	protected void updateView()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		List<TreePath> selectionPaths = new ArrayList<TreePath>();
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
		getApplication().getEventManager().publish(NafOptimizationRunner.TUNING_ACTIVE_EVENT_ID, canDo);

		canDo = study.canOptimize();
		getApplication().getEventManager().publish(NafOptimizationRunner.OPTIMIZATION_ACTIVE_EVENT_ID, canDo);
	}

	@Override
	public void doEvent(SubscriberEvent event)
	{
		DataModel source = ((DataModelEvent) event.getSource()).getDataModel();
		if (source instanceof FileDataModel)
		{
			String data = (String) ((FileDataModel) source).getData();
			String categoryName = getCategoryName(data);
			if (categoryName != null)
			{
				Category category = study.getCategory(categoryName);
				String eventId = event.getEvent();
				String subName = source.getName();
				if (NafOptimizationRunner.FILE_OPENED_EVENT_ID.equals(eventId))
				{
					category.addSub(subName, source);
					updateView();
				}
				else if (NafOptimizationRunner.FILE_CLOSED_EVENT_ID.equals(eventId))
				{
					category.removeSub(subName);
					updateView();
				}
				else if (NafOptimizationRunner.FILE_SAVED_EVENT_ID.equals(eventId)) {
					category.replaceSub(subName, (FileDataModel)source);
					updateView();
				}
			}
		}
	}

	protected String getCategoryName(String xmlString)
	{
		// Check Instrument
		BindFactory bindFactory = GeometryBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Instrument", true)) // TODO Make constants in binding framework
		{
			return StudyModel.INSTRUMENT_CATEGORY_ID;
		}

		// Check Tuning
		bindFactory = NoteBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Tuning", true)) // TODO Make constants in binding framework
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
			System.out.println(e);
		}
	}

	public void optimizeInstrument()
	{
		try
		{
			String xmlInstrument = study.optimizeInstrument();
			FileBasedApplication app = (FileBasedApplication) getApplication();
			DataModel data = app.newData("xml");
			// These sleeps are due to the way JDAF handles threading.
			// The DataModel and DataViews seem to be updated on separate threads
			// Dumb!
			CodeEditorView view = null;
			while (view == null) {
				view = (CodeEditorView) app.getDataView(data);
				Thread.sleep(100);
			}
			view.setText(xmlInstrument);
			Thread.sleep(100);
			view.updateModel(data);
			addDataModelToStudy(data);
		}
		catch (Exception e)
		{
			System.out.println(e);
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

}
