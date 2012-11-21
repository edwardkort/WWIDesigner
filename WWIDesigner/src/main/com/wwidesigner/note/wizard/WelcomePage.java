package com.wwidesigner.note.wizard;

import com.jidesoft.wizard.WelcomeWizardPage;

class WelcomePage extends WelcomeWizardPage
{

	public WelcomePage()
	{
		super("The Tuning Model");
	}

	@Override
	public int getLeftPaneItems() {
		return LEFTPANE_STEPS;
	}

}