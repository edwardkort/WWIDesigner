package com.wwidesigner.gui;

import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.FileFormat;
import com.jidesoft.app.framework.file.FileFormatFilenameFilter;
import com.jidesoft.app.framework.gui.ApplicationWindowsUI;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.app.framework.gui.GUIApplication;
import com.jidesoft.app.framework.gui.feature.GlobalSelectionFeature;
import com.jidesoft.app.framework.gui.filebased.FileHandlingFeature;
import com.jidesoft.app.framework.gui.filebased.FileIcon;
import com.jidesoft.tree.TreeUtils;
import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * FileSystemDataView.java
 * <p>
 * Facilitates a DataView that reflects the local file system in a file tree. The corresponding <code>DataModel</code>
 * should have a data type of File[] denoting the directories to show in the File tree. Double-clicking on a node
 * sends the file as criteria to GUIApplication.openData().
 * <p>
 * This DataView accepts GlobalSelectionFeature selections of type <code>java.io.File</code>.
 * <p>
 * Used in demos: <code>SplitCodeEditor</code>
 */
@SuppressWarnings("serial")
public class FileSystemDataView extends DataViewPane implements EventSubscriber {
	private JTree tree;
	
	@Override
	protected void initializeComponents() {
		// create file tree
		tree = new JTree();
		tree.setRootVisible(false);
		tree.addMouseListener(new TreeClickHandler());
		tree.setCellRenderer(new FileTreeRenderer());
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(225, 100));
		add(scrollPane);
        setPreferredSize(new Dimension(300, 150));
		
		// listen to global selection
		getApplication().getEventManager().subscribe(GlobalSelectionFeature.GLOBAL_SELECTION, this);
	}
	
	/**
	 * Sets the tree model.
	 */
	@Override
	public void updateView(DataModel dataModel) throws DataModelException {
		// use registered formats if possible
		FileHandlingFeature fileHandling = FileHandlingFeature.getFeature(getApplication());
		FileFormat formats = null;
		FilenameFilter filter = null;
		if(fileHandling != null) {
			formats = FileFormat.combinedFileFormat(fileHandling.getSupportedFileFormats());
			filter = new FileFormatFilenameFilter(formats);
		}
		else {
			// otherwise just files in local path
			filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return ((name.endsWith(".java") || (name.endsWith(".txt")) && !name.startsWith(".")) || new File(dir, name).isDirectory());
				}
			};
		}
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		java.util.List<File> dirs = Arrays.asList((File[])dataModel.getCriteria());
		for (File file : dirs) {
			MutableTreeNode node = makeNode(filter, file);
			if(node != null) {
				root.add(node);
			}
		}
		TreeModel model = new DefaultTreeModel(root);
		tree.setModel(model);
	}
	
	/*
	 * display the globally selected file in tree.
	 */
	public void doEvent(SubscriberEvent e) {
		GlobalSelectionFeature sel = (GlobalSelectionFeature)e.getSource();
		if(sel.hasSelection(File.class)) {
			TreeNode node = (TreeNode)TreeUtils.findTreeNode(tree, sel.getSelection());
			if(node != null) {
				java.util.List<TreeNode> parents = new ArrayList<TreeNode>();
				while(node != null) {
					parents.add(0, node);
					node = node.getParent();
				}
				TreePath path = new TreePath(parents.toArray());
				tree.setSelectionPath(path);
			}
		}
	}
	
	/*
     * Call GUIApplication.openData() on double-click, passing in the File as the criteria.
     */
    private static class TreeClickHandler extends MouseAdapter {
    	public void mouseClicked(MouseEvent e) {
    		if(e.getClickCount() > 1) {
	    		JTree tree = (JTree)e.getSource();
	    		TreePath path = tree.getSelectionPath();
	    		if(path != null) {
	    			File file = (File)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
	    			if(!file.isDirectory()) {
	    				try {
							GUIApplication application = ApplicationWindowsUI.getApplicationFromWindow(tree.getTopLevelAncestor());
							application.openData(file);
							
							// global selection
							GlobalSelectionFeature.getFeature(application).setSelection(file);
							
						} catch (DataModelException e1) {
							e1.printStackTrace();
						}
	    			}
	    		}
    		}
    	}
    }
	
    /*
     * File Tree nodes.
     */
    private static DefaultMutableTreeNode makeNode(FilenameFilter filter, File file) {
    	if(file.isDirectory()) {
    		DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
    		File[] files = file.listFiles(filter);
        	for (File f : files) {
        		if(!f.getName().startsWith(".")) {
        			DefaultMutableTreeNode child = makeNode(filter, f);
	    			if(child != null && (!f.isDirectory() || (f.isDirectory() && child.getChildCount() != 0))) {
	    				node.add(child);
	    			}
        		}
    		}
        	return node;
    	}
    	else {
    		if(filter.accept(file.getParentFile(), file.getName())) {
    			return new DefaultMutableTreeNode(file);
    		}
    		else {
    			return null;
    		}
    	}
    }
    
    /*
     * Renderer for File nodes.
     */
    private static class FileTreeRenderer extends DefaultTreeCellRenderer {
    	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    		File file = (File)((DefaultMutableTreeNode)value).getUserObject();
    		JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, file == null ? "Root" : file.getName(), sel, expanded, leaf,
    				row, hasFocus);
    		if(file != null) {
	    		if(!SystemInfo.isMacOSX()) {
	    			if(!file.isDirectory()) {
	    				label.setIcon(new FileIcon(FileFormat.getExtension(file)));
	    			}
	    		}
    		}
    		return label;
    	}
    }
}