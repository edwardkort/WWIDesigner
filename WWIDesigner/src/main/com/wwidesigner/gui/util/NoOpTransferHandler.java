package com.wwidesigner.gui.util;

import javax.swing.TransferHandler;

public class NoOpTransferHandler extends TransferHandler
{
	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		return false;
	}
}
