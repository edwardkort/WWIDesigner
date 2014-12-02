package com.wwidesigner.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.jidesoft.app.framework.gui.DialogAdapter;
import com.jidesoft.app.framework.gui.DialogEvent;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.FileDialogRequest;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.gui.StudyModel;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.Constraints;

public class FileOpenDialogPreviewPane extends DialogAdapter
{
	private XmlPreview preview = new XmlPreview();
	private PreviewPanel previewPanel = new PreviewPanel();

	@Override
	public void dialogShowing(DialogEvent event)
	{
		DialogRequest request = event.getRequest();
		if (request instanceof FileDialogRequest)
		{
			if (((FileDialogRequest) request).getDialogType() == FileDialogRequest.OPEN_DIALOG)
			{
				Object source = event.getSource();
				if (source instanceof JDialog)
				{
					JFileChooser chooser = (JFileChooser) ((JDialog) source)
							.getContentPane().getComponent(0);
					chooser.addPropertyChangeListener(new PropertyChangeListener()
					{
						public void propertyChange(PropertyChangeEvent chEvent)
						{
							if (chEvent
									.getPropertyName()
									.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
							{
								File file = (File) chEvent.getNewValue();
								preview.configure(file);
							}
						}
					});
					chooser.setAccessory(previewPanel);
				}
			}
		}
	}

	class PreviewPanel extends JPanel
	{
		public PreviewPanel()
		{
			JLabel label = new JLabel("XML Contents", SwingConstants.CENTER);
			setPreferredSize(new Dimension(200, 0));
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEtchedBorder());
			label.setBorder(BorderFactory.createEtchedBorder());
			add(label, BorderLayout.NORTH);
			add(preview, BorderLayout.CENTER);
		}
	}

	class XmlPreview extends JPanel
	{
		private JTextArea textArea = new JTextArea();
		private JScrollPane scrollPane = new JScrollPane(textArea);

		public XmlPreview()
		{
			textArea.setEditable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setLayout(new BorderLayout());
			add(scrollPane, BorderLayout.CENTER);
		}

		public void configure(File file)
		{
			if (file != null && file.isFile() && file.canRead())
			{
				String fileContents = getFileContents(file);
				String category = StudyModel.getCategoryName(fileContents);
				StringBuilder sb = new StringBuilder();
				switch (category)
				{
					case StudyModel.INSTRUMENT_CATEGORY_ID:
						Instrument instrument = StudyModel
								.getInstrument(fileContents);
						sb.append("XML type:\n");
						sb.append("    " + category);
						sb.append("\nName:\n");
						sb.append("    " + instrument.getName());
						sb.append("\nDescription:\n");
						sb.append("    " + instrument.getDescription());
						sb.append("\nNumber of holes:\n");
						sb.append("    " + instrument.getHole().size());
						break;
					case StudyModel.TUNING_CATEGORY_ID:
						Tuning tuning = StudyModel.getTuning(fileContents);
						sb.append("XML type:\n");
						sb.append("    " + category);
						sb.append("\nName:\n");
						sb.append("    " + tuning.getName());
						sb.append("\nDescription:\n");
						sb.append("    " + tuning.getComment());
						sb.append("\nNumber of holes:\n");
						sb.append("    " + tuning.getNumberOfHoles());
						break;
					case StudyModel.CONSTRAINTS_CATEGORY_ID:
						Constraints constraints = StudyModel
								.getConstraints(fileContents);
						sb.append("XML type:\n");
						sb.append("    " + category);
						sb.append("\nName:\n");
						sb.append("    " + constraints.getConstraintsName());
						sb.append("\nOptimizer:\n");
						sb.append("    "
								+ constraints.getObjectiveDisplayName());
						sb.append("\nNumber of constraints:\n");
						sb.append("    "
								+ constraints.getTotalNumberOfConstraints());
						sb.append("\nNumber of holes:\n");
						sb.append("    " + constraints.getNumberOfHoles());
						break;
					default:
						sb.append("XML type:\n");
						sb.append("    Unknown");
				}
				textArea.setText(sb.toString());
			}
		}

	}

	public static String getFileContents(File file)
	{
		String contents = new String();
		try
		{
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String str;
			while ((str = reader.readLine()) != null)
			{
				sb.append(str);
				sb.append("\n");
			}
			contents = sb.toString();
			reader.close();
		}
		catch (Exception ex)
		{

		}

		return contents;
	}
}
