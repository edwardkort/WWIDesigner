/**
 * A multi-page wizard dialog to build tunings from scales and fingering patterns.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.note.wizard;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.SwingUtilities;

import com.jidesoft.dialog.AbstractDialogPage;
import com.jidesoft.dialog.PageList;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.wizard.AbstractWizardPage;
import com.jidesoft.wizard.WizardDialog;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.DataPopulatedProvider;

public class TuningWizardDialog extends WizardDialog
{
	private static final int PREFERRED_WIDTH = 1100;
	private static final int PREFERRED_HEIGHT = 800;
	private File currentSaveDirectory;

	public TuningWizardDialog(Frame owner, String title, boolean modal)
			throws HeadlessException
	{
		super(owner, title, modal);

		PageList pages = new PageList();

		AbstractWizardPage page1 = new WelcomePage();
		AbstractWizardPage page2 = new ScaleSymbolPage(this);
		AbstractWizardPage page3 = new TemperamentPage(this);
		AbstractWizardPage page4 = new ScaleIntervalPage(this);
		AbstractWizardPage page5 = new ScalePage(this);
		AbstractWizardPage page6 = new FingeringPatternPage(this);
		AbstractWizardPage page7 = new TuningPage(this);

		pages.append(page1);
		pages.append(page2);
		pages.append(page3);
		pages.append(page4);
		pages.append(page5);
		pages.append(page6);
		pages.append(page7);

		setPageList(pages);

		// Wire dependencies
		addDataPopulatedDependency(page2, page4);
		addDataPopulatedDependency(page3, page4);
		addDataPopulatedDependency(page4, page5);
		addDataPopulatedDependency(page5, page7);
		addDataPopulatedDependency(page6, page7);

		setStepsPaneNavigable(true);

		setScreenSize();

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});
	}

	/**
	 * Ensures that the preferred wizard dimensions (invoking no scrollbars in
	 * the widget pages) fit within the existing screen size.
	 */
	private void setScreenSize()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = Math.min(screenSize.width, PREFERRED_WIDTH);
		int height = Math.min(screenSize.height, PREFERRED_HEIGHT);
		setPreferredSize(new Dimension(width, height));
		pack();
		setResizable(true); // for wizard, it's better to make it not resizable.
		JideSwingUtilities.globalCenterWindow(this);
	}

	private void addDataPopulatedDependency(AbstractWizardPage provider,
			AbstractWizardPage listener)
	{
		((DataPopulatedProvider) provider)
				.addDataPopulatedListener((DataPopulatedListener) listener);
	}

	public Object getPageData(int pageIndex)
	{
		AbstractDialogPage page = getPageList().getPage(pageIndex);

		return ((DataProvider) page).getPageData();
	}

	public File getCurrentSaveDirectory()
	{
		return currentSaveDirectory;
	}

	public void setCurrentSaveDirectory(File currentSaveDirectory)
	{
		this.currentSaveDirectory = currentSaveDirectory;
	}

	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
				TuningWizardDialog wizard = new TuningWizardDialog(
						(Frame) null, "Tuning File Wizard", false);
				wizard.setVisible(true);
			}
		});
	}

}
