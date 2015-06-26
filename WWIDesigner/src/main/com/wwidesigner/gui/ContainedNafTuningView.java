package com.wwidesigner.gui;

import java.awt.Component;

import javax.swing.JScrollPane;

import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.note.Fingering;
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
	public String getText() throws DataModelException
	{
		Tuning tuning = tuningPanel.getData();
		String xmlText = null;
		try
		{
			tuning.checkValidity();
			xmlText = StudyModel.marshal(tuning);
		}
		catch (Exception e)
		{
			throw new DataModelException(null, e);
		}

		return xmlText;
	}

	@Override
	public void setText(String text) throws DataModelException
	{
		BindFactory bindFactory = NoteBindFactory.getInstance();
		try
		{
			Tuning tuning = (Tuning) bindFactory.unmarshalXml(text, true);
			tuningPanel.loadData(tuning, true);
		}
		catch (Exception e)
		{
			throw new DataModelException(null, e);
		}
	}

	/**
	 * @return	The fingering from the first selected row of the fingering table,
	 * 		 	or from the first row, if no rows are selected.
	 */
	public Fingering getSelectedFingering()
	{
		return tuningPanel.getSelectedFingering();
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
