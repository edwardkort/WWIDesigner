package com.wwidesigner.gui;

import java.awt.Component;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jidesoft.app.framework.gui.DataViewPane;
import com.jidesoft.editor.CodeEditor;
import com.jidesoft.editor.language.LanguageSpecManager;

public class ContainedXmlTextView extends ContainedXmlView
{
	private CodeEditor codeEditor;
	private DocumentListener docListener;

	public ContainedXmlTextView(DataViewPane parent)
	{
		super(parent);

		codeEditor = new CodeEditor();
		LanguageSpecManager.getInstance().getLanguageSpec("XML")
				.configureCodeEditor(codeEditor);

		setDataDirty();
	}

	@Override
	protected void setDataDirty()
	{
		docListener = new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				parent.makeDirty(true);
			}

			public void removeUpdate(DocumentEvent e)
			{
				parent.makeDirty(true);
			}

			public void changedUpdate(DocumentEvent e)
			{
				// unnecessary
			}
		};
		codeEditor.getDocument().addDocumentListener(docListener);
	}

	@Override
	public String getText()
	{
		return codeEditor.getText();
	}

	@Override
	public void setText(String text)
	{
		try
		{
			codeEditor.getDocument().removeDocumentListener(docListener);
			codeEditor.setText(text != null ? text : "");
			codeEditor.getDocument().addDocumentListener(docListener);
		}
		catch (Exception e)
		{

		}
	}

	@Override
	public Component getViewComponent()
	{
		return codeEditor;
	}

}
