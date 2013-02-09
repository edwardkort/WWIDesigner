package com.wwidesigner.note.wizard;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.WelcomeWizardPage;

class WelcomePage extends WelcomeWizardPage
{

	public WelcomePage()
	{
		super("The Tuning Model");
	}

	@Override
	public int getLeftPaneItems()
	{
		return LEFTPANE_STEPS;
	}

	@Override
	public JComponent createWizardContent()
	{
		JScrollPane scrollPane = null;
		try
		{
			JEditorPane editPane = new JEditorPane();
			editPane.setEditable(false);
			editPane.setPage(ClassLoader
					.getSystemResource("com/wwidesigner/note/wizard/Welcome.html"));
			scrollPane = new JScrollPane(editPane);
		}
		catch (Exception e)
		{
		}

		return scrollPane;
	}

	@Override
	public void setupWizardButtons()
	{
		fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
		fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.CANCEL);
	}

}