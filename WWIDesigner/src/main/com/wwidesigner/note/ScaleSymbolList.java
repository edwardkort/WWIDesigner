/**
 * 
 */
package com.wwidesigner.note;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kort
 * 
 */
public class ScaleSymbolList
{
	protected String name;
	protected String comment;
	protected List<String> scaleSymbol;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param aName
	 *            the name to set
	 */
	public void setName(String aName)
	{
		this.name = aName;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param aComment
	 *            the comment to set
	 */
	public void setComment(String aComment)
	{
		this.comment = aComment;
	}

	/**
	 * @return the scaleSymbol
	 */
	public List<String> getScaleSymbol()
	{
		if (scaleSymbol == null)
		{
			scaleSymbol = new ArrayList<String>();
		}
		return this.scaleSymbol;
	}

	/**
	 * @param aScaleSymbol
	 *            the scaleSymbol to set
	 */
	public void setScaleSymbol(List<String> aScaleSymbol)
	{
		this.scaleSymbol = aScaleSymbol;
	}

	public void addScaleSymbol(String symbol)
	{
		getScaleSymbol();
		scaleSymbol.add(symbol);
	}

	private static final String[][] NOTE_NAMES = new String[][] {
			{ "C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G",
					"G#", "Ab", "A", "A#", "Bb", "B" },
			{ "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" },
			{ "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" } };

	private static final int ALL_SYMBOLS = 0;
	private static final int SHARPS_ONLY = 1;
	private static final int FLATS_ONLY = 2;
	private static final String[] ACCIDENTALS = new String[] { "",
			", sharps only", ", flats only" };

	public static ScaleSymbolList makeTraditionalSymbols(int accidentals)
	{
		ScaleSymbolList symbolList = new ScaleSymbolList();

		symbolList.setName("Traditional Symbols" + ACCIDENTALS[accidentals]);
		symbolList
				.setComment("The traditional (Helmholtz) symbols in 11 octaves"
						+ ACCIDENTALS[accidentals] + ".");

		List<String> symbols = new ArrayList<String>();
		String octaves = ",,,";
		for (int i = 0; i <= octaves.length(); i++)
		{
			String octave = octaves.substring(i);
			for (String symbol : NOTE_NAMES[accidentals])
			{
				symbol += octave;
				symbols.add(symbol);
			}
		}
		octaves = "\'\'\'\'\'\'";
		for (int i = octaves.length(); i >= 0; i--)
		{
			String octave = octaves.substring(i);
			for (String symbol : NOTE_NAMES[accidentals])
			{
				symbol = symbol.toLowerCase() + octave;
				symbols.add(symbol);
			}
		}
		symbolList.setScaleSymbol(symbols);

		return symbolList;
	}

	public static ScaleSymbolList makeScientificSymbols(int accidentals)
	{
		ScaleSymbolList symbolList = new ScaleSymbolList();

		symbolList.setName("Scientific Symbols" + ACCIDENTALS[accidentals]);
		symbolList.setComment("The scientific symbols in 11 octaves"
				+ ACCIDENTALS[accidentals] + ".");

		List<String> symbols = new ArrayList<String>();
		for (int i = -1; i < 10; i++)
		{
			for (String symbol : NOTE_NAMES[accidentals])
			{
				symbol += i;
				symbols.add(symbol);
			}
		}
		symbolList.setScaleSymbol(symbols);

		return symbolList;
	}

	public static ScaleSymbolList makeMidiSymbols()
	{
		ScaleSymbolList symbolList = new ScaleSymbolList();

		symbolList.setName("MIDI Symbols");
		symbolList.setComment("The MIDI symbols in 11 octaves.");

		List<String> symbols = new ArrayList<String>();
		for (int i = 0; i < 128; i++)
		{
			String symbol = String.valueOf(i);
			symbols.add(symbol);
		}
		symbolList.setScaleSymbol(symbols);
		return symbolList;
	}

	public enum StandardSymbols
	{
		TRADITIONAL("Traditional (Helmholtz) symbols"), TRADITIONAL_SHARPS(
				"Traditional (Helmholtz) symbols, sharps only"), TRADITIONAL_FLATS(
				"Traditional (Helmholtz) symbols, flats only"), SCIENTIFIC(
				"Scientific symbols"), SCIENTIFIC_SHARPS(
				"Scientific symbols, sharps only"), SCIENTIFIC_FLATS(
				"Scientific symbols, flats only"), MIDI("MIDI numbers");
		private String description;

		private StandardSymbols(String aDescription)
		{
			this.description = aDescription;
		}

		public String toString()
		{
			return description;
		}
	}

	public static ScaleSymbolList makeStandardSymbols(StandardSymbols symbolType)
	{
		switch (symbolType)
		{
			case TRADITIONAL:
				return makeTraditionalSymbols(ALL_SYMBOLS);
			case TRADITIONAL_SHARPS:
				return makeTraditionalSymbols(SHARPS_ONLY);
			case TRADITIONAL_FLATS:
				return makeTraditionalSymbols(FLATS_ONLY);
			case SCIENTIFIC:
				return makeScientificSymbols(ALL_SYMBOLS);
			case SCIENTIFIC_SHARPS:
				return makeScientificSymbols(SHARPS_ONLY);
			case SCIENTIFIC_FLATS:
				return makeScientificSymbols(FLATS_ONLY);
			case MIDI:
				return makeMidiSymbols();
			default:
				return null;
		}
	}

	public void removeNulls()
	{
		// Don't check name
		if (comment != null && comment.trim().length() == 0)
		{
			comment = null;
		}
		if (scaleSymbol != null && scaleSymbol.size() > 0)
		{
			for (int i = scaleSymbol.size() - 1; i >= 0; i--)
			{
				String symbol = scaleSymbol.get(i);
				if (symbol == null || symbol.trim().length() == 0)
				{
					scaleSymbol.remove(i);
				}
			}
		}
	}

}
