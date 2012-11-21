package com.wwidesigner.note.wizard;

import com.jidesoft.wizard.CompletionWizardPage;

public class CompletionPage extends CompletionWizardPage
{
	public CompletionPage()
	{
		super("Instrument Tuning Summary");
	}

	@Override
	public int getLeftPaneItems()
	{
		return LEFTPANE_STEPS;
	}

}
