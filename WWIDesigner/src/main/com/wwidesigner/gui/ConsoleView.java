/**
 * Class to capture and display console output (System.out).
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.jidesoft.app.framework.gui.DataViewPane;

/**
 * @author kort
 * 
 */
public class ConsoleView extends DataViewPane
{
	JTextArea textArea;

	protected void initializeComponents()
	{

		// sets the window size
		setPreferredSize(new Dimension(550, 400));
		setBackground(Color.WHITE);
		setBorder(null);

		// init text area
		textArea = new JTextArea();
		textArea.setFont(textArea.getFont().deriveFont(12f));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 4));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		add(scrollPane);

		// initial focus
		setDefaultFocusComponent(textArea);

		redirectSystemStreams();

		System.out.println("Woodwind Instrument Designer");
		System.out.println("Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
		System.out.println("This is free software, and you are welcome to redistribute it");
		System.out.println("under the terms of the GNU General Public License, version 3 or later.");
		System.out.println();
		System.out.println("Layout data directory: " + getApplication().getDataDirectory().getAbsolutePath());
		System.out.println();
	}

	void updateTextArea(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textArea.append(text);
			}
		});
	}

	public void clear()
	{
		textArea.setText("");
	}

	private void redirectSystemStreams()
	{
		OutputStream out = new OutputStream()
		{
			@Override
			public void write(int b) throws IOException
			{
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException
			{
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException
			{
				write(b, 0, b.length);
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
}
