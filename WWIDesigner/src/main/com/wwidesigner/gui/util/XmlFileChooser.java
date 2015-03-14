package com.wwidesigner.gui.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class XmlFileChooser extends JFileChooser
{
	public XmlFileChooser()
	{
		setFileFilter(new XmlFileFilter());
	}

	public XmlFileChooser(File currentDirectory)
	{
		super(currentDirectory);
		setFileFilter(new XmlFileFilter());
	}

	class XmlFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File file)
		{
			if (file.isDirectory())
			{
				return true;
			}

			return file.getName().toLowerCase().endsWith(".xml");
		}

		@Override
		public String getDescription()
		{
			return "XML Files (*.xml)";
		}

	}
}
