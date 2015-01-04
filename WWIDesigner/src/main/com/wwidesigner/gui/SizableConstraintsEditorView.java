package com.wwidesigner.gui;

import com.jidesoft.app.framework.gui.DataViewPane;

public class SizableConstraintsEditorView extends ConstraintsEditorView
{
	public SizableConstraintsEditorView(DataViewPane parent)
	{
		super(parent);
		constraintsPanel.setPanelDimension(600, 150);
		constraintsPanel.setColumnWidths(new int[] { 340, 100, 80, 80 });
	}

}
