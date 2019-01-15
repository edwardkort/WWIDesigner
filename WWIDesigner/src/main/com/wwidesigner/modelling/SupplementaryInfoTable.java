/**
 * Display a tabular report of supplementary instrument performance values.
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

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.note.Note;
import com.wwidesigner.note.Tuning;

/**
 * Display a tabular report of supplementary instrument performance information
 * at a specified tuning.  At present, you should consider these numbers speculative.
 * 
 * @author Burton Patkau
 * 
 */
public class SupplementaryInfoTable extends DefaultTableModel
{
	protected final static double DeltaF = 0.0012;	// Baseline for derivative, about 2 cents.
	String title;
	DecimalFormat format_;
	DecimalFormat format_0;
	DecimalFormat format_00;
	DecimalFormat format_sci;

	public SupplementaryInfoTable(String aTitle)
	{
		this.title = aTitle;
		format_ = new DecimalFormat("#0");
		format_0 = new DecimalFormat("#0.0");
		format_00 = new DecimalFormat("#0.00");
		format_sci = new DecimalFormat("0.000E0");
	}


	protected String formatted(Double f)
	{
		if (f == null)
		{
			return "N/A";
		}
		else if (f >= 100.0)
		{
			return format_.format(f);
		}
		else
		{
			return format_0.format(f);
		}
	}
	
	protected String formatted2(Double f)
	{
		if (f == null)
		{
			return "";
		}
		return format_00.format(f);
	}
	
	/**
	 * Estimate Q factor using:<br/>
	 * Q = f0/2 * d/df (Im(z)/Re(z))<br/>
	 * cf. Arthur D. Yaghjian, Steven R. Best, "Impedance, Bandwidth, and Q of Antennas,"
	 * IEEE Transactions on Antennas and Propagation, V 53, n 4, April 2005.
	 */
	protected static double Q(double freq, Complex z, InstrumentCalculator calculator,
			Fingering fingering)
	{
		double freqPlus = freq * (1+DeltaF);
		Complex zPlus = calculator.calcZ(freqPlus, fingering);
		return 0.25*(freq+freqPlus)
				* (zPlus.getImaginary()/zPlus.getReal() - z.getImaginary()/z.getReal())
				/ (freqPlus - freq);
	}

	/*
	 * Estimate Q factor for a note, from 
	 * Michael J. Moloney and Daniel L. Hatten, "Acoustic quality factor and energy losses in cylindrical pipes,"
	 * Am. J. Phys. 69 (3), March 2001, p. 311.
		double waveNumber = parms.calcWaveNumber(freq);
		double qWall = 0.5 * radius * Math.sqrt(waveNumber)
				/ (Math.sqrt(parms.getRho()) * parms.getAlphaConstant());
		double qRadiation = 2.0 * length / (waveNumber*radius*radius);
		return 1.0/(1.0/qWall + 1.0/qRadiation);
	 */
	
	/**
	 * Collect the data necessary to tabulate the supplementary data for an
	 * instrument. Following this call, use showTuning() or printTuning() to
	 * display the table.
	 * 
	 * @param tuner
	 *            - instrument tuner loaded with instrument, tuning and calculator.
	 * @param usePredicted
	 *            - true to tabulate data at tuner's predicted tuning,
	 *              false to tabulate data at target tuning.
	 */
	public void buildTable(InstrumentTuner tuner, boolean usePredicted)
	{
		InstrumentCalculator calculator = tuner.getCalculator();
		Tuning target;
		if (usePredicted)
		{
			target = tuner.getPredictedTuning();
		}
		else
		{
			target = tuner.getTuning();
		}
		Instrument instrument = tuner.getInstrument();
		Mouthpiece mouthpiece = instrument.getMouthpiece();
		List<Fingering> fingerings = target.getFingering();
		Note note;
		Double freq;
		Double windowLength = null;		// Window length in meters, if available.
		Double windwayArea = null;		// Windway cross-section, in mm**2, if available.
		Complex z;
		double speed;
		if (mouthpiece.getFipple() != null)
		{
			windowLength = mouthpiece.getFipple().getWindowLength();
			if (mouthpiece.getFipple().getWindwayHeight() != null)
			{
				windwayArea = 1.0e6 * mouthpiece.getFipple().getWindowWidth()
						* mouthpiece.getFipple().getWindwayHeight();
				if (windwayArea == 0.0)
				{
					windwayArea = null;
				}
			}
		}

		int colNr;

		addColumn("Note");
		addColumn("Freq");
		addColumn("Im(Z)");
		addColumn("Log |Z|");
		addColumn("Log |Z2|");
		addColumn("Log |Z3|");
		addColumn("Gain");
		addColumn("Gain2");
		addColumn("Gain3");
		addColumn("Log G1/G2");
		addColumn("Log G3/G2");
		addColumn("Log G1*G3/G2^2");
		addColumn("Q Factor");
		if (windowLength != null)
		{
			addColumn("Air Speed");
			if (windwayArea != null)
			{
				addColumn("Air Flow Rate");
			}
		}

		for (int i = 0; i < fingerings.size(); ++i)
		{
			note = fingerings.get(i).getNote();

			Object[] values = new Object[getColumnCount()];
			colNr = 0;

			values[colNr++] = note.getName();
			freq = note.getFrequency();
			values[colNr++] = formatted2(freq);
			if (freq != null)
			{
				z = calculator.calcZ(freq, fingerings.get(i));
				Complex z2 = calculator.calcZ(freq*2., fingerings.get(i));
				Complex z3 = calculator.calcZ(freq*3., fingerings.get(i));
				values[colNr++] = format_sci.format(z.getImaginary());
				values[colNr++] = formatted(Math.log(z.abs()));
				values[colNr++] = formatted(Math.log(z2.abs()));
				values[colNr++] = formatted(Math.log(z3.abs()));
				double h1 = calculator.calcGain(freq, z);
				double h2 = calculator.calcGain(freq*2., z2);
				double h3 = calculator.calcGain(freq*3., z3);
				values[colNr++] = formatted(h1);
				values[colNr++] = formatted(h2);
				values[colNr++] = formatted(h3);
				values[colNr++] = formatted(Math.log(h1/h2));
				values[colNr++] = formatted(Math.log(h3/h2));
				values[colNr++] = formatted(Math.log(h1*h3/(h2*h2)));
				values[colNr++] = formatted(Q(freq, z, calculator, fingerings.get(i)));
				if (windowLength != null)
				{
					// Although speed is nominally in m/s, and flow rate
					// is nominally in ml/s, we don't publish these units.
					// At present, the quantities are best treated as relative values.
					speed = LinearVInstrumentTuner.velocity(freq, windowLength, z);
					values[colNr++] = formatted(speed);
					if (windwayArea != null)
					{
						values[colNr++] = formatted(speed * windwayArea);
					}
				}
			}
			else
			{
				values[colNr++] = "";
				values[colNr++] = "";
				values[colNr++] = "";
				if (windowLength != null)
				{
					values[colNr++] = "";
					if (windwayArea != null)
					{
						values[colNr++] = "";
					}
				}
			}


			addRow(values);
		}

	}

	public void printTable(OutputStream os)
	{
		PrintWriter pw = new PrintWriter(os);
		pw.println(title);
		int col;
		pw.printf("%-11s", getColumnName(0));
		for (col = 1; col < getColumnCount(); col++)
		{
			if (getColumnName(col).length() < 11)
			{
				pw.printf("%11s", getColumnName(col));
			}
			else
			{
				pw.print("           ");
			}
		}
		pw.println();
		String secondLine = new String();
		for (col = 0; col < getColumnCount(); col++)
		{
			if (getColumnName(col).length() >= 11)
			{
				while (secondLine.length() < 11 * col)
				{
					secondLine += " ";
				}
				secondLine += getColumnName(col);
			}
		}
		if (!secondLine.isEmpty())
		{
			pw.println(secondLine);
		}
		for (int row = 0; row < getRowCount(); row++)
		{
			pw.printf("%-11s", getValueAt(row, 0));
			for (col = 1; col < getColumnCount(); col++)
			{
				pw.printf("%11s", getValueAt(row, col));
			}
			pw.println();
		}
		pw.println();
		pw.flush();
	}

	public void showTable()
	{
		showTable(true);
	}

	public void showTable(final boolean exitOnClose)
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
				frame.setSize(500, 360);
				frame.getContentPane().add(new JScrollPane(table));
				frame.setVisible(true);
			}
		});
	}

}
