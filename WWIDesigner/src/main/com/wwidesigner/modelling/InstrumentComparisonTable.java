package com.wwidesigner.modelling;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.util.Constants.LengthType;

/**
 * Display a tabular report comparing differences between two related instruments.
 * 
 * @author Burton Patkau
 * 
 */
public class InstrumentComparisonTable extends DefaultTableModel
{
	String title;
	DecimalFormat format_;
	DecimalFormat format_0;
	DecimalFormat format_00;

	// Level at which we consider a difference significant.
	// Mostly to eliminate minor floating point errors.
	public static final double MinDifference = 0.000001;

	public InstrumentComparisonTable(String title, LengthType defaultLengthType)
	{
		this.title = title;
		format_   = new DecimalFormat("#0");
		format_0  = new DecimalFormat("#0.0");
		format_00 = new DecimalFormat("#0.00");
	}
	
	protected void addHeadings(String oldName, String newName)
	{
		addColumn("Dimension");
		addColumn(oldName);
		addColumn(newName);
		addColumn("Change");
		addColumn("% Change");
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

	protected void addValues(String dimension, String oldValue, String newValue, boolean evenIfEqual)
	{
		if ( evenIfEqual || ! oldValue.equals(newValue) )
		{
			Object[] newRow = new Object[5];
			newRow[0] = dimension;
			newRow[1] = oldValue;
			newRow[2] = newValue;
			newRow[3] = "";
			newRow[4] = "";
			addRow(newRow);
		}
	}

	protected void addValues(String dimension, int oldValue, int newValue, boolean evenIfEqual)
	{
		if ( evenIfEqual || oldValue != newValue )
		{
			Object[] newRow = new Object[5];
			newRow[0] = dimension;
			newRow[1] = oldValue;
			newRow[2] = newValue;
			newRow[3] = format_.format(newValue - oldValue);
			newRow[4] = "";
			addRow(newRow);
		}
	}

	protected void addValues(String dimension, Double oldValue, Double newValue, boolean evenIfEqual)
	{
		if ( evenIfEqual 
			|| (oldValue == null && newValue != null) 
			|| (oldValue != null && newValue == null) 
			|| (oldValue != null && newValue != null && Math.abs(oldValue - newValue) >= MinDifference ) )
		{
			Object[] newRow = new Object[5];
			newRow[0] = dimension;
			newRow[1] = oldValue;
			newRow[2] = newValue;
			newRow[3] = "";
			newRow[4] = "";
			if (oldValue != null && newValue != null)
			{
				Double change = newValue - oldValue;
				newRow[3] = format_00.format(change);
				newRow[4] = format_0.format(100.0 * change/oldValue);
			}
			addRow(newRow);
		}
	}
	
	protected String lengthTypeName(LengthType lengthType)
	{
		switch (lengthType)
		{
			case M:
				return "m.";
			case CM:
				return "cm.";
			case MM:
				return "mm.";
			case FT:
				return "ft.";
			case IN:
				return "in.";
			default:
				return "N/A";
		}
	}
	
	/**
	 * Generate a name for a bore point.
	 * 
	 * @param segType
	 *            : generally "Hole" or "Bore".
	 * @param segNumber
	 *            : zero-based index of the bore point.
	 * @param dimensionName
	 *            : bore point attribute being named.
	 * @return Name for the bore point, as a string.
	 */
	protected String segmentName(String segType, int segNumber,
			String dimensionName)
	{
		return segType + " " + format_.format(segNumber + 1) + " "
				+ dimensionName;
	}

	/**
	 * Collect the data necessary to tabulate the differences between instruments.
	 * Following this call, use showTable() or printTable() to display the table.
	 */
	public void buildTable(String oldName, Instrument oldInstrument, 
			String newName, Instrument newInstrument)
	{
		title = "Compare " + oldName + " to " + newName;
		addHeadings(oldName, newName);
		addValues("Name", oldInstrument.getName(), newInstrument.getName(), true);

		List<Hole> oldHoles = oldInstrument.getHole();
		List<Hole> newHoles = newInstrument.getHole();
		List<BorePoint> oldBore = oldInstrument.getBorePoint();
		List<BorePoint> newBore = newInstrument.getBorePoint();
		Mouthpiece oldMouthpiece = oldInstrument.getMouthpiece();
		Mouthpiece newMouthpiece = newInstrument.getMouthpiece();
		addValues("Length in", lengthTypeName(oldInstrument.getLengthType()), 
				lengthTypeName(newInstrument.getLengthType()), false);
		if (oldMouthpiece.getClass().equals(newMouthpiece.getClass()))
		{
			addValues("Mouthpiece Position", oldMouthpiece.getPosition(), 
					newMouthpiece.getPosition(), false);
			addValues("Window Length", oldMouthpiece.getFipple().getWindowLength(), 
					newMouthpiece.getFipple().getWindowLength(), false);
			addValues("Window Width", oldMouthpiece.getFipple().getWindowWidth(), 
					newMouthpiece.getFipple().getWindowWidth(), false);
			addValues("Window Height", oldMouthpiece.getFipple().getWindowHeight(), 
					newMouthpiece.getFipple().getWindowHeight(), false);
			addValues("Windway Height", oldMouthpiece.getFipple().getWindwayHeight(), 
					newMouthpiece.getFipple().getWindwayHeight(), false);
			addValues("Windway Length", oldMouthpiece.getFipple().getWindwayLength(), 
					newMouthpiece.getFipple().getWindwayLength(), false);
			addValues("Fipple Factor", oldMouthpiece.getFipple().getFippleFactor(), 
					newMouthpiece.getFipple().getFippleFactor(), false);
		}
		addValues("Beta Factor", oldMouthpiece.getBeta(), 
				newMouthpiece.getBeta(), false);

		addValues("Holes", oldHoles.size(), newHoles.size(), false);
		if (oldHoles.size() == newHoles.size())
		{
			for ( int holeNr = 0; holeNr < oldHoles.size(); ++holeNr)
			{
				addValues(segmentName("Hole",holeNr,"Position"), 
						oldHoles.get(holeNr).getBorePosition(), 
						newHoles.get(holeNr).getBorePosition(), false);
				addValues(segmentName("Hole",holeNr,"Diameter"), 
						oldHoles.get(holeNr).getDiameter(),
						newHoles.get(holeNr).getDiameter(), false);
				addValues(segmentName("Hole",holeNr,"Height"), 
						oldHoles.get(holeNr).getHeight(),
						newHoles.get(holeNr).getHeight(), false);
			}
		}

		addValues("Bore Points", oldBore.size(), newBore.size(), false);
		if (oldBore.size() == newBore.size())
		{
			// Report individual bore segments.
			// Report final segment below.
			for ( int boreNr = 0; boreNr < oldBore.size() - 1; ++boreNr)
			{
				addValues(segmentName("Bore",boreNr,"Position"), 
						oldBore.get(boreNr).getBorePosition(), 
						newBore.get(boreNr).getBorePosition(), false);
				addValues(segmentName("Bore",boreNr,"Diameter"), 
						oldBore.get(boreNr).getBoreDiameter(),
						newBore.get(boreNr).getBoreDiameter(), false);
			}
		}
		else
		{
			// If number of bore segments differ, report only the first two segments here.
			// Report final segment below.
			addValues(segmentName("Bore",0,"Position"), 
					oldBore.get(0).getBorePosition(), 
					newBore.get(0).getBorePosition(), false);
			addValues(segmentName("Bore",0,"Diameter"), 
					oldBore.get(0).getBoreDiameter(),
					newBore.get(0).getBoreDiameter(), false);
			if ( oldBore.size() >= 2 && newBore.size() >= 2 )
			{
				// Since sizes are different, at least one instrument has more
				// than two bore sections.
				addValues(segmentName("Bore",1,"Position"), 
						oldBore.get(1).getBorePosition(), 
						newBore.get(1).getBorePosition(), false);
				addValues(segmentName("Bore",1,"Diameter"), 
						oldBore.get(1).getBoreDiameter(),
						newBore.get(1).getBoreDiameter(), false);
			}
		}
		addValues("Bore Length",
				oldBore.get(oldBore.size()-1).getBorePosition(), 
				newBore.get(newBore.size()-1).getBorePosition(), false);
		addValues("Final Bore Diameter", 
				oldBore.get(oldBore.size()-1).getBoreDiameter(),
				newBore.get(newBore.size()-1).getBoreDiameter(), false);
		
		addValues("Termination Flange", oldInstrument.getTermination().getFlangeDiameter(),
				oldInstrument.getTermination().getFlangeDiameter(), false);
	}

	public void printTable(OutputStream os)
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
				frame.setSize(600, 400);
				frame.getContentPane().add(new JScrollPane(table));
				frame.setVisible(true);
			}
		});
	}

}
