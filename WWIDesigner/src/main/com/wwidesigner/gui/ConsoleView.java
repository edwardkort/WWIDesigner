/**
 * 
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
	private JTextArea textArea;

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
	}

	private void updateTextArea(final String text)
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
