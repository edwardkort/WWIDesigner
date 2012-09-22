package com.wwidesigner.gui;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.gui.ComponentPageable;
import com.jidesoft.app.framework.gui.DataViewPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.print.Pageable;

/**
 * TextView.java
 * <p>
 * Facilitates a DataView using the <code>JTextArea</code> for plain text editing.
 * <p>
 * Used in demos: <code>SimpleCodeEditor</code> and <code>DockedTextEditor</code>.
 */
public class TextView extends DataViewPane {

	private JTextArea textArea;
	private DocumentListener docListener;

    protected void initializeComponents() {
    	
    	// sets the window size
        setPreferredSize(new Dimension(550, 400));
        setBackground(Color.WHITE);
        setBorder(null);
        
        // init text area
        textArea = new JTextArea();
        textArea.setFont(textArea.getFont().deriveFont(12f));
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        docListener = new DocumentListener() {
        	public void insertUpdate(DocumentEvent e) {
        		makeDirty(true);
        	}
        	public void removeUpdate(DocumentEvent e) {
        		makeDirty(true);
        	}
        	public void changedUpdate(DocumentEvent e) {
        		// unnecessary for plain text
        	}
        };
        textArea.getDocument().addDocumentListener(docListener);
        textArea.setBorder(BorderFactory.createEmptyBorder(4,10,4,4));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        add(scrollPane);

        // initial focus
        setDefaultFocusComponent(textArea);
        
        // install editing features
        installEditables();
    }
    
    public void updateView(DataModel dataModel) {
    	textArea.getDocument().removeDocumentListener(docListener);
        textArea.setText(((BasicDataModel)dataModel).getData() != null ? ((BasicDataModel)dataModel).getData().toString() : "");
        textArea.setCaretPosition(0);
        textArea.getDocument().addDocumentListener(docListener);
    }
    
    public void updateModel(DataModel dataModel) {
    	((BasicDataModel)dataModel).setData(textArea.getText());
    }

    public Pageable getPageable() {
    	return new ComponentPageable(textArea);
    }
}