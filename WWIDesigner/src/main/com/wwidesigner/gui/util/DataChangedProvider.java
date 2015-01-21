package com.wwidesigner.gui.util;

public interface DataChangedProvider
{
	public void addDataChangedListener(DataChangedListener listener);

	public void removeDataChangedListener(DataChangedListener listener);
}
