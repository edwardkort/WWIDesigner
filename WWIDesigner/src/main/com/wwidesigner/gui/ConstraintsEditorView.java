package com.wwidesigner.gui;

import java.awt.Component;

import javax.swing.JScrollPane;

import com.jidesoft.app.framework.DataModelException;
import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.gui.util.DataChangedEvent;
import com.wwidesigner.gui.util.DataChangedListener;
import com.wwidesigner.optimization.Constraints;
import com.wwidesigner.optimization.bind.OptimizationBindFactory;
import com.wwidesigner.optimization.view.ConstraintsPanel;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.LengthType;

public class ConstraintsEditorView extends ContainedXmlView
{
	protected ConstraintsPanel constraintsPanel;
	private JScrollPane scrollPane;

	public ConstraintsEditorView(DataViewPane aParent)
	{
		super(aParent);

		constraintsPanel = new ConstraintsPanel();
		scrollPane = new JScrollPane(constraintsPanel);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);

		setDataDirty();
	}

	@Override
	public Component getViewComponent()
	{
		return scrollPane;
	}

	@Override
	public String getText() throws DataModelException
	{
		Constraints constraints = constraintsPanel.getConstraintValues();
		String xmlText = null;
		try
		{
			xmlText = StudyModel.marshal(constraints);
		}
		catch (Exception e)
		{
			throw new DataModelException(null, e);
		}

		return xmlText;
	}

	@Override
	// Perhaps this method should throw Exceptions up to the parent rather than
	// trap them here.
	public void setText(String text) throws DataModelException
	{
		BindFactory bindFactory = OptimizationBindFactory.getInstance();
		try
		{
			Constraints constraints = (Constraints) bindFactory.unmarshalXml(
					text, true);
			LengthType dimensionType = getApplicationLengthType();
			constraints.setDimensionType(dimensionType);
			constraints.setConstraintParent();
			constraintsPanel.setConstraintValues(constraints);
		}
		catch (Exception e)
		{
			throw new DataModelException(null, e);
		}
	}

	@Override
	protected void setDataDirty()
	{
		constraintsPanel.addDataChangedListener(new DataChangedListener()
		{

			@Override
			public void dataChanged(DataChangedEvent event)
			{
				if (event.getSource() instanceof ConstraintsPanel)
				{
					parent.makeDirty(true);
				}
			}

		});
	}

}
