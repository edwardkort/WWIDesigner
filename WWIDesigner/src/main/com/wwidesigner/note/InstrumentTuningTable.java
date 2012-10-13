/**
 * 
 */
package com.wwidesigner.note;

import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * @author kort
 * 
 */
public class InstrumentTuningTable
{
	DefaultTableModel model;
	String title;
	DecimalFormat format;

	public InstrumentTuningTable(String title)
	{
		model = new DefaultTableModel();
		this.title = title;
		model.addColumn("Note");
		model.addColumn("Expected Frequency");
		model.addColumn("Calculated Frequency");
		model.addColumn("Tuning Deviation (cents)");
		format = new DecimalFormat("#0.00");
	}

	public void addTuning(Fingering fingering, Double playedFrequency)
	{
		double expectedFrequency = fingering.getNote().getFrequency();
		Object[] values = new Object[4];
		values[0] = fingering.getNote().getName();
		values[1] = format.format(expectedFrequency);

		if (playedFrequency != null)
		{
			double freqRatio = playedFrequency / expectedFrequency;
			double centsDeviation = getCents(freqRatio);
			values[2] = format.format(playedFrequency);
			values[3] = format.format(centsDeviation);
		}
		else
		{
			values[2] = format.format(Double.NaN);
			values[3] = format.format(Double.NaN);
		}

		model.addRow(values);
	}

	public static double getCents(double freqRatio)
	{
		return 1200. * Math.log(freqRatio) / Math.log(2.);
	}

	public JTable getTuningTable()
	{
		JTable table = new JTable(model);

		return table;
	}

	public void showTuning()
	{
		showTuning(true);
	}

	public void showTuning(final boolean exitOnClose)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JTable table = new JTable(model);
				JFrame frame = new JFrame(title);
				if (exitOnClose)
				{
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				frame.setSize(550, 300);
				frame.getContentPane().add(new JScrollPane(table));
				frame.setVisible(true);
			}
		});
	}

}
