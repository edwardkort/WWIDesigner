package com.wwidesigner.geometry.view;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.util.Constants.LengthType;

/**
 * Display a tabular report comparing differences between two related
 * instruments.
 * 
 * @author Burton Patkau
 * 
 */
public class InstrumentComparisonTable extends DefaultTableModel
{
	protected String title;
	protected LengthType defaultLengthType;
	protected DecimalFormat format_;
	protected DecimalFormat format_0;
	protected DecimalFormat format_00;
	protected DecimalFormat format_default;

	// Level at which we consider a difference significant.
	protected double minDifference;

	public InstrumentComparisonTable(String title, LengthType defaultLengthType)
	{
		this.title = title;
		this.defaultLengthType = defaultLengthType;
		setPrecision();
	}

	protected void setPrecision()
	{
		format_default = new DecimalFormat();
		int defaultPrecision = defaultLengthType.getDecimalPrecision();
		format_default.setMinimumFractionDigits(defaultPrecision);
		format_default.setMaximumFractionDigits(defaultPrecision);
		format_default.setMinimumIntegerDigits(1);
		format_ = new DecimalFormat("#0");
		format_0 = new DecimalFormat("#0.0");
		format_00 = new DecimalFormat("#0.00");

		minDifference = Math.pow(10, -defaultPrecision);
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
		if (f == null)
		{
			return "N/A";
		}
		else
		{
			return format_00.format(f);
		}
	}

	protected void addValues(String dimension, String oldValue,
			String newValue, boolean evenIfEqual)
	{
		if (evenIfEqual || !oldValue.equals(newValue))
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

	protected void addValues(String dimension, int oldValue, int newValue,
			boolean evenIfEqual)
	{
		if (evenIfEqual || oldValue != newValue)
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

	protected void addValues(String dimension, Double oldValue,
			Double newValue, boolean evenIfEqual)
	{
		if (evenIfEqual
				|| (oldValue == null && newValue != null)
				|| (oldValue != null && newValue == null)
				|| (oldValue != null && newValue != null && Math.abs(oldValue
						- newValue) >= minDifference))
		{
			Object[] newRow = new Object[5];
			newRow[0] = dimension;
			newRow[1] = format_default.format(oldValue);
			newRow[2] = format_default.format(newValue);
			newRow[3] = "";
			newRow[4] = "";
			if (oldValue != null && newValue != null)
			{
				Double change = newValue - oldValue;
				newRow[3] = format_default.format(change);
				newRow[4] = format_0.format(100.0 * change / oldValue);
			}
			addRow(newRow);
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
	 * Collect the data necessary to tabulate the differences between
	 * instruments. Following this call, use showTable() or printTable() to
	 * display the table.
	 */
	public void buildTable(String oldName, Instrument oldInstrument,
			String newName, Instrument newInstrument)
	{
		title = "Compare " + oldName + " to " + newName;
		addHeadings(oldName, newName);
		addValues("Name", oldInstrument.getName(), newInstrument.getName(),
				true);

		List<Hole> oldHoles = oldInstrument.getHole();
		List<Hole> newHoles = newInstrument.getHole();
		List<BorePoint> oldBore = oldInstrument.getBorePoint();
		List<BorePoint> newBore = newInstrument.getBorePoint();
		Mouthpiece oldMouthpiece = oldInstrument.getMouthpiece();
		Mouthpiece newMouthpiece = newInstrument.getMouthpiece();
		addValues("Length in", defaultLengthType.name(),
				defaultLengthType.name(), true);
		if (oldMouthpiece.getClass().equals(newMouthpiece.getClass()))
		{
			addMouthpieceValues(oldMouthpiece, newMouthpiece);
		}
		addValues("Beta Factor", oldMouthpiece.getBeta(),
				newMouthpiece.getBeta(), false);

		addValues("Holes", oldHoles.size(), newHoles.size(), false);
		if (oldHoles.size() == newHoles.size())
		{
			String[] holeNames = getHoleNames(oldHoles);
			for (int holeNr = 0; holeNr < oldHoles.size(); ++holeNr)
			{
				addValues(holeNames[holeNr] + " Position", oldHoles.get(holeNr)
						.getBorePosition(), newHoles.get(holeNr)
						.getBorePosition(), false);
				addValues(holeNames[holeNr] + " Diameter", oldHoles.get(holeNr)
						.getDiameter(), newHoles.get(holeNr).getDiameter(),
						false);
				addValues(holeNames[holeNr] + " Height", oldHoles.get(holeNr)
						.getHeight(), newHoles.get(holeNr).getHeight(), false);
			}
		}

		addValues("Bore Points", oldBore.size(), newBore.size(), false);
		if (oldBore.size() == newBore.size())
		{
			// Report individual bore segments.
			// Report final segment below.
			for (int boreNr = 0; boreNr < oldBore.size() - 1; ++boreNr)
			{
				addValues(segmentName("Bore", boreNr, "Position"),
						oldBore.get(boreNr).getBorePosition(),
						newBore.get(boreNr).getBorePosition(), false);
				addValues(segmentName("Bore", boreNr, "Diameter"),
						oldBore.get(boreNr).getBoreDiameter(),
						newBore.get(boreNr).getBoreDiameter(), false);
			}
		}
		else
		{
			// If number of bore segments differ, report only the first two
			// segments here.
			// Report final segment below.
			addValues(segmentName("Bore", 0, "Position"), oldBore.get(0)
					.getBorePosition(), newBore.get(0).getBorePosition(), false);
			addValues(segmentName("Bore", 0, "Diameter"), oldBore.get(0)
					.getBoreDiameter(), newBore.get(0).getBoreDiameter(), false);
			if (oldBore.size() >= 2 && newBore.size() >= 2)
			{
				// Since sizes are different, at least one instrument has more
				// than two bore sections.
				addValues(segmentName("Bore", 1, "Position"), oldBore.get(1)
						.getBorePosition(), newBore.get(1).getBorePosition(),
						false);
				addValues(segmentName("Bore", 1, "Diameter"), oldBore.get(1)
						.getBoreDiameter(), newBore.get(1).getBoreDiameter(),
						false);
			}
		}
		addValues("Bore Length", oldBore.get(oldBore.size() - 1)
				.getBorePosition(), newBore.get(newBore.size() - 1)
				.getBorePosition(), false);
		addValues("Final Bore Diameter", oldBore.get(oldBore.size() - 1)
				.getBoreDiameter(), newBore.get(newBore.size() - 1)
				.getBoreDiameter(), false);

		addValues("Termination Flange", oldInstrument.getTermination()
				.getFlangeDiameter(), oldInstrument.getTermination()
				.getFlangeDiameter(), false);
	}

	protected String[] getHoleNames(List<Hole> oldHoles)
	{
		int numberOfHoles = oldHoles.size();
		String[] holeNames = new String[numberOfHoles];
		for (int i = 0; i < numberOfHoles; i++)
		{
			Hole hole = oldHoles.get(i);
			String name = hole.getName();
			if (name == null || name.length() == 0)
			{
				name = getDefaultHoleName(i, numberOfHoles);
			}
			holeNames[i] = name;
		}

		return holeNames;
	}

	protected String getDefaultHoleName(int holeIdx, int maxIdx)
	{
		String name = "Hole " + (holeIdx + 1);
		if (holeIdx == 0)
		{
			name += " (top)";
		}
		else if (holeIdx == maxIdx - 1)
		{
			name += " (bottom)";
		}

		return name;
	}

	protected void addMouthpieceValues(Mouthpiece oldMouthpiece,
			Mouthpiece newMouthpiece)
	{
		addValues("Mouthpiece Position", oldMouthpiece.getPosition(),
				newMouthpiece.getPosition(), false);
		if (oldMouthpiece.getFipple() != null && newMouthpiece.getFipple() != null)
		{
			addValues("Window Length", oldMouthpiece.getFipple().getWindowLength(),
					newMouthpiece.getFipple().getWindowLength(), false);
			addValues("Window Width", oldMouthpiece.getFipple().getWindowWidth(),
					newMouthpiece.getFipple().getWindowWidth(), false);
			addValues("Window Height", oldMouthpiece.getFipple().getWindowHeight(),
					newMouthpiece.getFipple().getWindowHeight(), false);
			addValues("Windway Height", oldMouthpiece.getFipple()
					.getWindwayHeight(), newMouthpiece.getFipple()
					.getWindwayHeight(), false);
			addValues("Windway Length", oldMouthpiece.getFipple()
					.getWindwayLength(), newMouthpiece.getFipple()
					.getWindwayLength(), false);
			addValues("Fipple Factor", oldMouthpiece.getFipple().getFippleFactor(),
					newMouthpiece.getFipple().getFippleFactor(), false);
		}
		if (oldMouthpiece.getEmbouchureHole() != null && newMouthpiece.getEmbouchureHole() != null)
		{
			addValues("Emb Hole Length", oldMouthpiece.getEmbouchureHole().getLength(),
					newMouthpiece.getEmbouchureHole().getLength(), false);
			addValues("Emb Hole Width", oldMouthpiece.getEmbouchureHole().getWidth(),
					newMouthpiece.getEmbouchureHole().getWidth(), false);
			addValues("Emb Hole Height", oldMouthpiece.getEmbouchureHole().getHeight(),
					newMouthpiece.getEmbouchureHole().getHeight(), false);
			addValues("Airstream Length", oldMouthpiece.getEmbouchureHole()
					.getAirstreamLength(), newMouthpiece.getEmbouchureHole()
					.getAirstreamLength(), false);
			addValues("Airstream Height", oldMouthpiece.getEmbouchureHole()
					.getAirstreamHeight(), newMouthpiece.getEmbouchureHole()
					.getAirstreamHeight(), false);
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
		showTable(false);
	}

	public void showTable(final boolean exitOnClose)
	{
		final TableModel model = this;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JTable table = new JTable(model);
				formatTable(table);
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

	protected void formatTable(JTable table)
	{
		// Resize Dimension column
		TableColumn col = table.getColumn(columnIdentifiers.get(0));
		col.setMinWidth(120);

		// Set right alignment for other columns
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		for (int i = 1; i < 5; i++)
		{
			col = table.getColumn(columnIdentifiers.get(i));
			col.setCellRenderer(renderer);
			if (i > 2)
			{
				col.setMaxWidth(80);
			}
		}
	}

}
