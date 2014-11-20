package com.wwidesigner.gui;

import java.awt.Component;

import com.jidesoft.app.framework.gui.DataViewPane;

/**
 * This class represents a data view that will be displayed within a
 * DataViewPane. It may be generic for XML type, or specific to a single
 * underlying type in the XML. The data, to and from this view, is an XML
 * String. This class need not be concerned determining the underlying type: it
 * is handled by the parent view and the StudyModel.
 * 
 * @author Edward N. Kort
 *
 */
public abstract class ContainedXmlView
{
	protected DataViewPane parent;

	public ContainedXmlView(DataViewPane parent)
	{
		this.parent = parent;
	}

	/**
	 * Contains the logic to make the parent.makeDirty(true) call.
	 */
	protected abstract void setDataDirty(boolean isDirty);

	/**
	 * Returns the viewable Component in the view. This may either be the view
	 * itself, or a contained Component.
	 * 
	 * @return
	 */
	public abstract Component getViewComponent();

	/**
	 * Extracts the data from the view as an XML String
	 * 
	 * @return
	 */
	public abstract String getText();

	/**
	 * Processes the input XML and displays the data appropriately in the view.
	 * 
	 * @param text
	 *            - an XML text representation of the data.
	 */
	public abstract void setText(String text);

}
