package com.wwidesigner.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.CellRendererPane;

public class ComponentPainter
{

	public static Image paintComponent(Component c, int width, int height)
	{

		// Set it to it's preferred size. (optional)
		c.setSize(c.getPreferredSize());
		layoutComponent(c);

		BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		CellRendererPane crp = new CellRendererPane();
		crp.add(c);
		crp.paintComponent(img.createGraphics(), c, crp, c.getBounds());
		return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}

	// from the example of user489041
	public static void layoutComponent(Component c)
	{
		synchronized (c.getTreeLock())
		{
			c.doLayout();
			if (c instanceof Container)
				for (Component child : ((Container) c).getComponents())
					layoutComponent(child);
		}
	}

}
