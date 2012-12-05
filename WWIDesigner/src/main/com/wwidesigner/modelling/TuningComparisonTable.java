package com.wwidesigner.modelling;

import java.util.List;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Display a tabular report comparing the target/measured tuning
 * to the predicted tuning for an instrument.
 * @author Burton Patkau
 * 
 */
public class TuningComparisonTable extends DefaultTableModel
{
	String title;
	DecimalFormat format_0;
	DecimalFormat format_00;
	// Flags controlling which columns to display.
	boolean tgtHasFnom  = false;
	boolean predHasFnom = false;
	boolean tgtHasFmax  = false;
	boolean predHasFmax = false;
	boolean tgtHasFmin  = false;
	boolean predHasFmin = false;

	public TuningComparisonTable(String title)
	{
		this.title = title;
		format_0  = new DecimalFormat("#0.0");
		format_00 = new DecimalFormat("#0.00");
	}
	
	protected void setColumns( Tuning target, Tuning predicted )
	{
		tgtHasFnom  = false;
		predHasFnom = false;
		tgtHasFmax  = false;
		predHasFmax = false;
		tgtHasFmin  = false;
		predHasFmin = false;
		List<Fingering> tgtFingering  = target.getFingering();
		List<Fingering> predFingering = predicted.getFingering();
		Note tgtNote;
		Note predNote;

		assert tgtFingering.size() == predFingering.size();
		for ( int i = 0; i < tgtFingering.size(); i++ )
		{
			tgtNote  = tgtFingering.get(i).getNote();
			predNote = predFingering.get(i).getNote();
			assert tgtNote.getName() == predNote.getName();
			if ( tgtNote != null )
			{
				if ( tgtNote.getFrequency() != null )
				{
					tgtHasFnom = true;
				}
				if ( tgtNote.getFrequencyMax() != null )
				{
					tgtHasFmax = true;
				}
				if ( tgtNote.getFrequencyMin() != null )
				{
					tgtHasFmin = true;
				}
			}
			if ( predNote != null )
			{
				if ( predNote.getFrequencyMax() != null )
				{
					predHasFmax = true;
				}
				if ( predNote.getFrequencyMin() != null )
				{
					predHasFmin = true;
				}
			}
			if ( tgtNote != null && predNote != null
				&& tgtNote.getFrequency() != null && predNote.getFrequency() != null
				&& tgtNote.getFrequency() != predNote.getFrequency() )
			{
				predHasFnom = true;
			}
		}
	}
	
	protected void addHeadings()
	{
		addColumn("Note");
		if (tgtHasFnom)
		{
			addColumn("Expected");
		}
		if (predHasFnom)
		{
			addColumn("Predicted");
			if (tgtHasFnom)
			{
				addColumn("Deviation (cents)");
			}
		}
		if (tgtHasFmin)
		{
			addColumn("fmin");
		}
		if (predHasFmin)
		{
			addColumn("Pred fmin");
			if (tgtHasFmin)
			{
				addColumn("fmin Dev (cents)");
			}
		}
		if (tgtHasFmax)
		{
			addColumn("fmax");
		}
		if (predHasFmax)
		{
			addColumn("Pred fmax");
			if (tgtHasFmax)
			{
				addColumn("fmax Dev (cents)");
			}
		}
	}
	
	protected String formatted(Double f)
	{
		if ( f == null )
		{
			return "N/A";
		}
		else
		{
			return format_00.format(f);
		}
	}

	public void buildTable(Tuning target, Tuning predicted)
	{
		List<Fingering> tgtFingering  = target.getFingering();
		List<Fingering> predFingering = predicted.getFingering();
		Note tgtNote;
		Note predNote;
		Double tgtF;
		Double predF;
		Double cents;
		int colNr;

		double totalNomError = 0.0;		// Net error in predicting fnom, in cents.
		double varianceNom = 0.0;		// Sum of squared error in predicting fnom.
		int nrNomPredictions = 0;		// Number of predictions of fnom.
		double totalMaxError = 0.0;		// Net error in predicting fmax, in cents.
		double varianceMax = 0.0;		// Sum of squared error in predicting fmax.
		int nrMaxPredictions = 0;		// Number of predictions of fmax.
		double totalMinError = 0.0;		// Net error in predicting fmin, in cents.
		double varianceMin = 0.0;		// Sum of squared error in predicting fmin.
		int nrMinPredictions = 0;		// Number of predictions of fmin.
		
		setColumns(target, predicted);
		addHeadings();

		for ( int i = 0; i < tgtFingering.size(); ++ i )
		{
			tgtNote  = tgtFingering.get(i).getNote();
			predNote = predFingering.get(i).getNote();

			Object[] values = new Object[getColumnCount()];
			colNr = 0;

			values[colNr++] = tgtNote.getName();

			tgtF  = tgtNote.getFrequency();
			if (tgtHasFnom)
			{
				values[colNr++] = formatted(tgtF);
			}
			if (predHasFnom)
			{
				predF = predNote.getFrequency();
				values[colNr++] = formatted(predF);
				if (tgtHasFnom)
				{
					if ( predF == null )
					{
						values[colNr++] = " ";
					}
					else
					{
						cents = Note.cents(tgtF, predF);
						values[colNr++] = format_0.format(cents);
						totalNomError += cents;
						varianceNom   += cents*cents;
						nrNomPredictions += 1;
					}
				}
			}

			if (tgtHasFmin)
			{
				tgtF  = tgtNote.getFrequencyMin();
				values[colNr++] = formatted(tgtF);
			}
			if (predHasFmin)
			{
				predF = predNote.getFrequencyMin();
				values[colNr++] = formatted(predF);
				if (tgtHasFmin)
				{
					if ( predF == null )
					{
						values[colNr++] = " ";
					}
					else
					{
						cents = Note.cents(tgtF, predF);
						values[colNr++] = format_0.format(cents);
						totalMinError += cents;
						varianceMin   += cents*cents;
						nrMinPredictions += 1;
					}
				}
			}

			if (tgtHasFmax)
			{
				tgtF  = tgtNote.getFrequencyMax();
				values[colNr++] = formatted(tgtF);
			}
			if (predHasFmax)
			{
				predF = predNote.getFrequencyMax();
				values[colNr++] = formatted(predF);
				if (tgtHasFmax)
				{
					if ( predF == null )
					{
						cents = null;
						values[colNr++] = " ";
					}
					else
					{
						cents = Note.cents(tgtF, predF);
						values[colNr++] = format_0.format(cents);
						totalMaxError += cents;
						varianceMax   += cents*cents;
						nrMaxPredictions += 1;
					}
				}
			}

			addRow(values);
		}

		if (nrNomPredictions == 0 && nrMinPredictions == 0 && nrMaxPredictions == 0)
		{
			return;
		}
		Object[] errorRow = new Object[getColumnCount()];
		Object[] devRow = new Object[getColumnCount()];
		colNr = 0;
		errorRow[colNr] = "Net Error";
		devRow[colNr++] = "Deviation";
		if ( tgtHasFnom || predHasFnom )
		{
			errorRow[colNr] = " ";
			devRow[colNr++] = " ";
		}
		if ( tgtHasFnom && predHasFnom )
		{
			errorRow[colNr] = " ";
			devRow[colNr++] = " ";
			if ( nrNomPredictions > 0 )
			{
				errorRow[colNr] = format_00.format(totalNomError/nrNomPredictions);
				devRow[colNr++] = format_00.format(Math.sqrt(varianceNom/nrNomPredictions));
			}
			else
			{
				errorRow[colNr] = " ";
				devRow[colNr++] = " ";
			}
		}
		if ( tgtHasFmin || predHasFmin )
		{
			errorRow[colNr] = " ";
			devRow[colNr++] = " ";
		}
		if ( tgtHasFmin && predHasFmin )
		{
			errorRow[colNr] = " ";
			devRow[colNr++] = " ";
			if ( nrMinPredictions > 0 )
			{
				errorRow[colNr] = format_00.format(totalMinError/nrMinPredictions);
				devRow[colNr++] = format_00.format(Math.sqrt(varianceMin/nrMinPredictions));
			}
			else
			{
				errorRow[colNr] = " ";
				devRow[colNr++] = " ";
			}
		}
		if ( tgtHasFmax || predHasFmax )
		{
			errorRow[colNr] = " ";
			devRow[colNr++] = " ";
		}
		if ( tgtHasFmax && predHasFmax )
		{
			errorRow[colNr] = " ";
			devRow[colNr++] = " ";
			if ( nrMaxPredictions > 0 )
			{
				errorRow[colNr] = format_00.format(totalMaxError/nrMaxPredictions);
				devRow[colNr++] = format_00.format(Math.sqrt(varianceMax/nrMaxPredictions));
			}
			else
			{
				errorRow[colNr] = " ";
				devRow[colNr++] = " ";
			}
		}
		addRow(errorRow);
		addRow(devRow);
	}
	
	public void printTuning(OutputStream os)
	{
		PrintWriter pw = new PrintWriter( os );
		pw.println(title);
		int col;
		pw.printf("%-11s",getColumnName(0));
		for ( col = 1; col < getColumnCount(); col++ )
		{
			if ( getColumnName(col).length() < 11 )
			{
				pw.printf("%11s",getColumnName(col));
			}
			else
			{
				pw.print("           ");
			}
		}
		pw.println();
		String secondLine = new String();
		for ( col = 0; col < getColumnCount(); col++ )
		{
			if ( getColumnName(col).length() >= 11 )
			{
				while ( secondLine.length() < 11 * col )
				{
					secondLine += " ";
				}
				secondLine += getColumnName(col);
			}
		}
		if ( ! secondLine.isEmpty() )
		{
			pw.println(secondLine);
		}
		for ( int row = 0; row < getRowCount(); row++)
		{
			pw.printf("%-11s", getValueAt(row, 0));
			for ( col = 1; col < getColumnCount(); col++ )
			{
				pw.printf("%11s", getValueAt(row, col));
			}
			pw.println();
		}
		pw.println();
		pw.flush();
	}

	public void showTuning()
	{
		showTuning(true);
	}

	public void showTuning(final boolean exitOnClose)
	{
		final TableModel model = this;
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
