package com.wwidesigner.gui;

import java.awt.Component;

import javax.swing.JScrollPane;

import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.note.view.TuningWithWeightsPanel;
import com.wwidesigner.util.BindFactory;

public class ContainedNafTuningView extends ContainedXmlView implements
		DataChangedListener
{
	protected TuningWithWeightsPanel tuningPanel;
	private JScrollPane scrollPane;

	public ContainedNafTuningView(DataViewPane parent)
	{
		super(parent);

		tuningPanel = new TuningWithWeightsPanel(450);
		tuningPanel.addDataChangedListener(this);
		scrollPane = new JScrollPane(tuningPanel);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
	}

	@Override
	protected void setDataDirty()
	{
		parent.makeDirty(true);
	}

	@Override
	public Component getViewComponent()
	{
		return scrollPane;
	}

	@Override
	public String getText()
	{
		Tuning tuning = tuningPanel.getData();
		String xmlText = null;
		try
		{
			xmlText = StudyModel.marshal(tuning);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}

		return xmlText;
	}

	@Override
	public void setText(String text)
	{
		BindFactory bindFactory = NoteBindFactory.getInstance();
		try
		{
			Tuning tuning = (Tuning) bindFactory.unmarshalXml(text, true);
			tuningPanel.loadData(tuning, true);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	@Override
	public void dataChanged(DataChangedEvent event)
	{
		if (event.getSource() instanceof TuningWithWeightsPanel)
		{
			setDataDirty();
		}
	}

}
