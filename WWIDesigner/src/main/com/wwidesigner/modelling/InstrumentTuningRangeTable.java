package com.wwidesigner.modelling;

import java.util.List;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.PlayingRange.NoPlayingRange;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * For a specified instrument and tuning, print a report listing
 * the predicted tuning of the instrument, and the deviation from measured values.
 * @author Burton Patkau
 * 
 */
public class InstrumentTuningRangeTable
{
	DefaultTableModel model;
	String title;
	DecimalFormat format_00;

	public InstrumentTuningRangeTable(String title)
	{
		model = new DefaultTableModel();
		this.title = title;
		format_00 = new DecimalFormat("#0.00");

		model.addColumn("Note");
		model.addColumn("Nominal");
		model.addColumn("fmin");
		model.addColumn("Pred fmin");
		model.addColumn("fmin Dev (cents)");
		model.addColumn("fmax");
		model.addColumn("Pred fmax");
		model.addColumn("fmax Dev (cents)");
	}

	public void buildTable(InstrumentCalculator calculator, Instrument instrument, Tuning tuning)
	{
		try
		{
			List<Fingering>  noteList = tuning.getFingering();

			double totalMaxError = 0.0;		// Net error in predicting fmax, in cents.
			double varianceMax = 0.0;		// Sum of squared error in predicting fmax.
			int nrMaxPredictions = 0;		// Number of predictions of fmax.
			double totalMinError = 0.0;		// Net error in predicting fmin, in cents.
			double varianceMin = 0.0;		// Sum of squared error in predicting fmin.
			int nrMinPredictions = 0;		// Number of predictions of fmin.

			for ( int i = 0; i < noteList.size(); ++ i )
			{
				Fingering fingering = noteList.get(i);
				Double fnom = fingering.getNote().getFrequency();
				Double actualMax = fingering.getNote().getFrequencyMax();
				Double actualMin = fingering.getNote().getFrequencyMin();
				double target = 0.0;
				Object[] values = new Object[8];

				// Fill in values from tuning file,
				// and identify target frequency for
				// predicting range.

				values[0] = fingering.getNote().getName();
				if ( fnom == null )
				{
					values[1] = "N/A";
				}
				else
				{
					values[1] = format_00.format(fnom);
					target = fnom;
				}
				if ( actualMax == null )
				{
					values[5] = "N/A";
				}
				else
				{
					values[5] = format_00.format(actualMax);
					if ( target == 0.0 ) {
						target = actualMax;
					}
				}
				if ( actualMin == null )
				{
					values[2] = "N/A";
				}
				else
				{
					values[2] = format_00.format(actualMin);
					if ( target == 0.0 ) {
						target = actualMin;
					}
				}
				if ( target == 0.0 )
				{
					// No target. We can't make a prediction.
					values[3] = format_00.format(Double.NaN);
					values[4] = format_00.format(Double.NaN);
					values[6] = format_00.format(Double.NaN);
					values[7] = format_00.format(Double.NaN);
				}
				else
				{
					// Predict playing range.
					PlayingRange range = new PlayingRange(calculator, fingering);
					double fmax, fmin;
					try {
						fmax = range.findXZero(target);
						values[6] = format_00.format(fmax);
					}
					catch (NoPlayingRange e)
					{
						fmax = 0.0;
						values[6] = format_00.format(Double.NaN);
					}
					try {
						fmin = range.findFmin(fmax);
						values[3] = format_00.format(fmin);
					}
					catch (NoPlayingRange e)
					{
						fmin = 0.0;
						values[6] = format_00.format(Double.NaN);
					}

					double cents;
					if ( actualMin != null && fmin > 0.0 )
					{
						cents = Note.cents(actualMin, fmin);
						values[4] = format_00.format(cents);
						totalMinError += cents;
						varianceMin   += cents*cents;
						nrMinPredictions += 1;
					}
					else
					{
						values[4] = format_00.format(Double.NaN);
					}

					if ( actualMax != null && fmax > 0.0 )
					{
						cents = Note.cents(actualMax, fmax);
						values[7] = format_00.format(cents);
						totalMaxError += cents;
						varianceMax   += cents*cents;
						nrMaxPredictions += 1;
					}
					else
					{
						values[7] = format_00.format(Double.NaN);
					}
				}
				model.addRow(values);
			}

			Object[] errorRow = new Object[8];
			Object[] devRow = new Object[8];
			errorRow[0] = "Net Error";
			errorRow[1] = " ";
			errorRow[2] = " ";
			errorRow[3] = " ";
			errorRow[5] = " ";
			errorRow[6] = " ";
			devRow[0] = "Deviation";
			devRow[1] = " ";
			devRow[2] = " ";
			devRow[4] = " ";
			devRow[5] = " ";
			devRow[6] = " ";

			if ( nrMaxPredictions > 0 )
			{
				errorRow[7] = format_00.format(totalMaxError/nrMaxPredictions);
				devRow[7] = format_00.format(Math.sqrt(varianceMax/nrMaxPredictions));
			}
			else
			{
				errorRow[7] = " ";
				devRow[7] = " ";
			}
			if ( nrMinPredictions > 0 )
			{
				errorRow[4] = format_00.format(totalMinError/nrMinPredictions);
				devRow[4] = format_00.format(Math.sqrt(varianceMin/nrMinPredictions));
			}
			else
			{
				errorRow[4] = " ";
				devRow[4] = " ";
			}
			model.addRow(errorRow);
			model.addRow(devRow);
		}
		catch (Exception e)
		{
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
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
				frame.setSize(600, 400);
				frame.getContentPane().add(new JScrollPane(table));
				frame.setVisible(true);
			}
		});
	}

}
