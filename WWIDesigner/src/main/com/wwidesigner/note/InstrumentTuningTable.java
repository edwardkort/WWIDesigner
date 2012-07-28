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
		model.addColumn("Expected Frequency");
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

	public void showTuning()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JTable table = new JTable(model);
				JFrame frame = new JFrame(title);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(800, 600);
				frame.getContentPane().add(new JScrollPane(table));
				frame.setVisible(true);
			}
		});
	}

}
