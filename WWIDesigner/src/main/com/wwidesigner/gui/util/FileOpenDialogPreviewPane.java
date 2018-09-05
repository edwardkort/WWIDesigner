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
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;

import com.jidesoft.app.framework.gui.DialogAdapter;
import com.jidesoft.app.framework.gui.DialogEvent;
import com.jidesoft.app.framework.gui.DialogRequest;
import com.jidesoft.app.framework.gui.FileDialogRequest;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.gui.CategoryType;
import com.wwidesigner.gui.StudyModel;
import com.wwidesigner.note.Tuning;
import com.wwidesigner.optimization.Constraints;

public class FileOpenDialogPreviewPane extends DialogAdapter
{
	XmlPreview preview = new XmlPreview();
	private PreviewPanel previewPanel = new PreviewPanel();
	private final int previewWidth = 200;

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
					// Expand dialog size to accommodate the preview pane.
					JDialog dialog = (JDialog) source;
					Dimension originalSize = dialog.getPreferredSize();
					dialog.setPreferredSize(new Dimension(originalSize.width
							+ previewWidth, originalSize.height));
					dialog.setMinimumSize(new Dimension(originalSize.width
							+ previewWidth, originalSize.height));

					JFileChooser chooser = (JFileChooser) ((JDialog) source)
							.getContentPane().getComponent(0);
					chooser.addPropertyChangeListener(new PropertyChangeListener()
					{
						public void propertyChange(PropertyChangeEvent chEvent)
						{
							String propertyName = chEvent.getPropertyName();
							if (propertyName
									.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
							{
								File file = (File) chEvent.getNewValue();
								preview.configure(file);
							}
							else if (propertyName
									.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)
									|| propertyName
											.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY))
							{
								preview.configure(null);
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
			setPreferredSize(new Dimension(previewWidth, 0));
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEtchedBorder());
			label.setBorder(BorderFactory.createEtchedBorder());
			add(label, BorderLayout.NORTH);
			add(preview, BorderLayout.CENTER);
		}
	}

	class XmlPreview extends JPanel
	{
		private JTextPane textArea = new JTextPane();
		private JScrollPane scrollPane = new JScrollPane(textArea);

		public XmlPreview()
		{
			textArea.setEditable(false);
			textArea.setEditorKit(new HTMLEditorKit());
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
				if (category == null)
				{
					category = "Unknown";
				}
				StringBuilder sb = new StringBuilder();
				switch (category)
				{
					case CategoryType.INSTRUMENT_CATEGORY_ID:
						Instrument instrument = StudyModel
								.getInstrument(fileContents);
						sb.append("<b>XML type:</b><br/>");
						sb.append(category);
						sb.append("<br/><b>Name:</b><br/>");
						sb.append(instrument.getName());
						sb.append("<br/><b>Description:</b><br/>");
						sb.append(instrument.getDescription());
						sb.append("<br/><b>Number of holes:</b><br/>");
						sb.append(instrument.getHole().size());
						break;
					case CategoryType.TUNING_CATEGORY_ID:
						Tuning tuning = StudyModel.getTuning(fileContents);
						sb.append("<b>XML type:</b><br/>");
						sb.append(category);
						sb.append("<br/><b>Name:</b><br/>");
						sb.append(tuning.getName());
						sb.append("<br/><b>Description:</b><br/>");
						sb.append(tuning.getComment());
						sb.append("<br/><b>Number of holes:</b><br/>");
						sb.append(tuning.getNumberOfHoles());
						break;
					case CategoryType.CONSTRAINTS_CATEGORY_ID:
						Constraints constraints = StudyModel
								.getConstraints(fileContents);
						sb.append("<b>XML type:</b><br/>");
						sb.append(category);
						sb.append("<br/><b>Name:</b><br/>");
						sb.append(constraints.getConstraintsName());
						sb.append("<br/><b>Optimizer:</b><br/>");
						sb.append(constraints.getObjectiveDisplayName());
						sb.append("<br/><b>Number of constraints:</b><br/>");
						sb.append(constraints.getTotalNumberOfConstraints());
						sb.append("<br/><b>Number of holes:</b><br/>");
						sb.append(constraints.getNumberOfHoles());
						break;
					default:
						sb.append("<b>XML type:</b><br/>");
						sb.append("Unknown");
				}
				textArea.setText(sb.toString());
				MutableAttributeSet attrs = textArea.getInputAttributes();
				StyleConstants.setFontFamily(attrs, "SanSerif");
				StyleConstants.setFontSize(attrs, 12);
				textArea.getStyledDocument().setCharacterAttributes(0,
						sb.length() + 1, attrs, false);

			}
			else
			{
				textArea.setText(null);
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
