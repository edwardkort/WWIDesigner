package com.wwidesigner.gui.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class DirectoryChooserPanel extends JPanel
{
	private JTextField textField;
	private JButton button;
	String directoryPath;

	public DirectoryChooserPanel()
	{
		initializeComponents();
	}

	private void initializeComponents()
	{
		button = new JButton("Browse...");
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(400,
				button.getPreferredSize().height));
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser(directoryPath);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				int state = chooser.showOpenDialog(null);
				File file = chooser.getSelectedFile();
				if (state == JFileChooser.APPROVE_OPTION && file != null)
				{
					textField.setText(file.getPath());
				}
			}

		});
		setLayout(new FlowLayout());
		add(textField);
		add(button);
	}

	public String getSelectedDirectory()
	{
		directoryPath = textField.getText();
		if (directoryPath != null)
		{
			directoryPath = directoryPath.trim();
		}

		return directoryPath;
	}

	public void setSelectedDirectory(String directoryPath)
	{
		textField.setText(directoryPath);
		this.directoryPath = directoryPath;
	}

	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setBounds(300, 300, 1000, 100);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		frame.getContentPane().add(new DirectoryChooserPanel());
		frame.pack();
	}

}
