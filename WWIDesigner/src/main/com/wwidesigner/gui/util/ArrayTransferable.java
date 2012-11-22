package com.wwidesigner.gui.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ArrayTransferable implements Transferable
{
	private Class<?> type;
	private DataFlavor[] supportedFlavors;
	private Object[] data;

	public ArrayTransferable(Object[] data)
	{
		this.data = data;
		supportedFlavors = new DataFlavor[1];
		if (data != null && data.length > 0 && data[0] != null)
		{
			type = data[0].getClass();
			supportedFlavors[0] = new DataFlavor(type, type.toString());
		}
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		if (isDataFlavorSupported(flavor))
		{
			return data;
		}

		return data;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if (type == null || flavor == null)
		{
			return false;
		}
		
		return type.equals(flavor.getClass());
	}

}
