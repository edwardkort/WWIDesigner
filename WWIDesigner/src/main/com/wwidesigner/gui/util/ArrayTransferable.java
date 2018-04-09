/* Encapsulate data transferred by copy or drag from a tabular data structure such as a JTable. */
package com.wwidesigner.gui.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import com.wwidesigner.note.Fingering;

public class ArrayTransferable implements Transferable
{
	public static DataFlavor STRINGS_FLAVOUR = new DataFlavor(String[].class, "String array");
	public static DataFlavor DOUBLES_FLAVOUR = new DataFlavor(Double[].class, "Double array");
	public static DataFlavor FINGERINGS_FLAVOUR = new DataFlavor(Fingering[].class, "Fingering array");
	public static DataFlavor TABLE_FLAVOUR = new DataFlavor(Object[][].class, "Table matrix");
	
	private DataFlavor[] supportedFlavors;
	private Object[] data;

	/**
	 * Create a transferable object from an array of rows.
	 * @param data - row data, one element per row.
	 */
	public ArrayTransferable(Object[] data)
	{
		this.data = data;
		// Assume anything from a table can be cast to a string (possibly empty),
		// or an array of strings (possibly empty or singleton).
		if (data != null && data.length > 0 && data[0] != null)
		{
			if (data[0] instanceof String)
			{
				supportedFlavors = new DataFlavor[2];
				supportedFlavors[0] = STRINGS_FLAVOUR;
				supportedFlavors[1] = DataFlavor.stringFlavor;
			}
			else if (data[0] instanceof Double)
			{
				// Don't support STRINGS_FLAVOUR, so a column of Doubles can't be
				// dragged to a String column of another table, even though
				// getTransferData will support STRINGS_FLAVOUR.
				supportedFlavors = new DataFlavor[2];
				supportedFlavors[0] = DOUBLES_FLAVOUR;
				supportedFlavors[1] = DataFlavor.stringFlavor;
			}
			else if (data[0] instanceof Fingering)
			{
				// Don't support STRINGS_FLAVOUR, so a column of Fingerings can't be
				// dragged to a String column of another table, even though
				// getTransferData will support STRINGS_FLAVOUR.
				supportedFlavors = new DataFlavor[2];
				supportedFlavors[0] = FINGERINGS_FLAVOUR;
				supportedFlavors[1] = DataFlavor.stringFlavor;
			}
			else if (data[0] instanceof Object[])
			{
				supportedFlavors = new DataFlavor[2];
				supportedFlavors[0] = TABLE_FLAVOUR;
				supportedFlavors[1] = DataFlavor.stringFlavor;
			}
			else
			{
				supportedFlavors = new DataFlavor[2];
				supportedFlavors[0] = STRINGS_FLAVOUR;
				supportedFlavors[1] = DataFlavor.stringFlavor;
			}
		}
		else
		{
			supportedFlavors = new DataFlavor[2];
			supportedFlavors[0] = STRINGS_FLAVOUR;
			supportedFlavors[1] = DataFlavor.stringFlavor;
		}
	}
	
	/**
	 * Create a transferable object from a multi-line string.
	 * @param tableString - one line per row, with tab-separated cells. 
	 */
	public ArrayTransferable(String tableString)
	{
		String[] rows = tableString.split("\r?\n");
		int colCount = 1;
		int rowNr;
		String[][] data = new String[rows.length][1];
		for (rowNr = 0; rowNr < rows.length; ++rowNr)
		{
			data[rowNr] = rows[rowNr].split("\t");
			if (data[rowNr].length > colCount)
			{
				colCount = data[rowNr].length;
			}
		}
		if (colCount > 1)
		{
			this.data = data;
			supportedFlavors = new DataFlavor[2];
			supportedFlavors[0] = TABLE_FLAVOUR;
			supportedFlavors[1] = DataFlavor.stringFlavor;
		}
		else
		{
			this.data = new String[rows.length];
			for (rowNr = 0; rowNr < rows.length; ++rowNr)
			{
				this.data[rowNr] = data[rowNr][0];
			}
			supportedFlavors = new DataFlavor[2];
			supportedFlavors[0] = STRINGS_FLAVOUR;
			supportedFlavors[1] = DataFlavor.stringFlavor;
		}
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		int i, j;
		if (flavor.equals(supportedFlavors[0]))
		{
			return data;
		}
		if (flavor.equals(STRINGS_FLAVOUR))
		{
			String[] stringed = new String[data.length];
			try
			{
				for (i = 0; i < data.length; ++i)
				{
					stringed[i] = String.valueOf(data[i]);
				}
			}
			catch (Exception ex)
			{
				throw new UnsupportedFlavorException(flavor);
			}
			return stringed;
		}
		if (flavor.equals(DataFlavor.stringFlavor))
		{
			String stringed = "";
			try
			{
				for (i = 0; i < data.length; ++i)
				{
					if (data[i] instanceof Object[])
					{
						Object[] row = (Object[]) data[i];
						for (j = 0; j < row.length; ++j)
						{
							if (j > 0)
							{
								stringed += "\t";
							}
							stringed += String.valueOf(row[j]);
						}
					}
					else
					{
						stringed += String.valueOf(data[i]);
					}
					stringed += "\n";
				}
			}
			catch (Exception ex)
			{
				throw new UnsupportedFlavorException(flavor);
			}
			return stringed;
		}

		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if (flavor.equals(supportedFlavors[0]))
		{
			return true;
		}
		if (flavor.equals(supportedFlavors[1]))
		{
			return true;
		}
		if (supportedFlavors.length > 2 && flavor.equals(supportedFlavors[3]))
		{
			return true;
		}
		return false;
	}

}
