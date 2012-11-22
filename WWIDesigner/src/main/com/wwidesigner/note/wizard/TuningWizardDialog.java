package com.wwidesigner.note.wizard;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

	public TuningWizardDialog(Frame arg0, String arg1, boolean arg2)
			throws HeadlessException
	{
		super(arg0, arg1, arg2);

		PageList pages = new PageList();

		AbstractWizardPage page1 = new WelcomePage();
		AbstractWizardPage page2 = new ScaleSymbolPage(this);
		AbstractWizardPage page3 = new TemperamentPage(this);
		AbstractWizardPage page4 = new ScaleIntervalPage(this);
		AbstractWizardPage pageEnd = new CompletionPage();

		pages.append(page1);
		pages.append(page2);
		pages.append(page3);
		pages.append(page4);
		pages.append(pageEnd);

		setPageList(pages);

		// Wire dependencies
		addDataPopulatedDependency(page2, page4);
		addDataPopulatedDependency(page3, page4);

		setStepsPaneNavigable(true);
		setPreferredSize(new Dimension(900, 700));
		pack();
		setResizable(true); // for wizard, it's better to make it not resizable.
		JideSwingUtilities.globalCenterWindow(this);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}
		});
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

	/**
	 * @param args
	 */
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
						(Frame) null, "Instrument Tuning Tool", false);
				wizard.setVisible(true);
			}
		});
	}

}
