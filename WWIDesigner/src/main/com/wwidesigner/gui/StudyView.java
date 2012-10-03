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

		getApplication().getEventManager().subscribe("FileOpened", this);
		getApplication().getEventManager().subscribe("FileClosed", this);
	}

	public void updateView()
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
				if (event.getEvent().equals("FileOpened"))
				{
					category.addSub(source.getName(), source);
					updateView();
				}
				else if (event.getEvent().equals("FileClosed"))
				{
					String subName = source.getName();
					if (subName.equals(category.getSelectedSub()))
					{
						category.setSelectedSub(null);
					}
					category.removeSub(subName);

					updateView();
				}
			}
		}
	}

	private String getCategoryName(String xmlString)
	{
		// Check Instrument
		BindFactory bindFactory = GeometryBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Instrument", true))
		{
			return "Instrument";
		}

		// Check Tuning
		bindFactory = NoteBindFactory.getInstance();
		if (bindFactory.isValidXml(xmlString, "Tuning", true))
		{
			return "Tuning";
		}

		return null;
	}

}
