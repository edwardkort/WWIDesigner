package com.wwidesigner.gui;

import java.awt.Component;
import java.util.prefs.Preferences;

import com.jidesoft.app.framework.gui.DataViewPane;
import com.wwidesigner.util.Constants.LengthType;

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
	 * Returns the LengthType specified in the application preferences.
	 * 
	 * @return If not set (in the Options dialog), returns the default
	 *         LengthType.
	 */
	protected LengthType getApplicationLengthType()
	{
		Preferences preferences = parent.getApplication().getPreferences();
		String lengthTypeName = preferences.get(
				OptimizationPreferences.LENGTH_TYPE_OPT,
				OptimizationPreferences.LENGTH_TYPE_DEFAULT);
		return LengthType.valueOf(lengthTypeName);
	}

	/**
	 * Contains the logic to make the parent.makeDirty(true) call.
	 */
	protected abstract void setDataDirty();

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
