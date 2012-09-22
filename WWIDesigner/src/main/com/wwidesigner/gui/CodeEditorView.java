package com.wwidesigner.gui;

import com.jidesoft.app.framework.BasicDataModel;
import com.jidesoft.app.framework.DataModel;
import com.jidesoft.app.framework.gui.ComponentPageable;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.editor.CodeEditor;
import com.jidesoft.editor.language.LanguageSpecManager;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.print.Pageable;

/**
 * CodeEditorView.java
 * <p>
 * Facilitates a DataView using the JIDE <code>CodeEditor</code> with the "Java"
 * language spec.
 * <p>
 * Used in demos: <code>SimpleCodeEditor</code>
 */
@SuppressWarnings("serial")
public class CodeEditorView extends DataViewPane
{
	private CodeEditor codeEditor;
	private DocumentListener docListener;

	protected void initializeComponents()
	{

		// sets the window size
		setPreferredSize(new Dimension(550, 400));
		setBorder(null);

		// init code editor
		codeEditor = new CodeEditor();
		LanguageSpecManager.getInstance().getLanguageSpec("XML")
				.configureCodeEditor(codeEditor);
		add(codeEditor);

		docListener = new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				makeDirty(true);
			}

			public void removeUpdate(DocumentEvent e)
			{
				makeDirty(true);
			}

			public void changedUpdate(DocumentEvent e)
			{
				// unnecessary
			}
		};
		codeEditor.getDocument().addDocumentListener(docListener);
	}

	public void updateView(DataModel dataModel)
	{
		try
		{
			codeEditor.getDocument().removeDocumentListener(docListener);
			codeEditor
					.setText(((BasicDataModel) dataModel).getData() != null ? ((BasicDataModel) dataModel)
							.getData().toString() : "");
			codeEditor.getDocument().addDocumentListener(docListener);
		}
		catch (Exception e)
		{

		}
	}

	public void updateModel(DataModel dataModel)
	{
		((BasicDataModel) dataModel).setData(codeEditor.getText());
	}
	
    public Pageable getPageable() {
    	return new ComponentPageable(codeEditor);
    }

}