package com.wwidesigner.geometry.view;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.util.Constants.LengthType;

public class NafInstrumentComparisonTable extends InstrumentComparisonTable
{
	public NafInstrumentComparisonTable(String aTitle,
			LengthType aDefaultLengthType)
	{
		super(aTitle, aDefaultLengthType);
	}

	/**
	 * For the NAF, hole count starts at the bottom of the flute.
	 */
	@Override
	protected String getDefaultHoleName(int holeIdx, int maxIdx)
	{
		String name = "Hole " + (maxIdx - holeIdx);
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

	/**
	 * Change value names to match NAF terminology.
	 */
	@Override
	protected void addMouthpieceValues(Mouthpiece oldMouthpiece,
			Mouthpiece newMouthpiece)
	{
		addValues("Splitting-edge Position", oldMouthpiece.getPosition(),
				newMouthpiece.getPosition(), false);
		addValues("TSH Length", oldMouthpiece.getFipple().getWindowLength(),
				newMouthpiece.getFipple().getWindowLength(), false);
		addValues("TSH Width", oldMouthpiece.getFipple().getWindowWidth(),
				newMouthpiece.getFipple().getWindowWidth(), false);
		addValues("Flue Depth", oldMouthpiece.getFipple().getWindwayHeight(),
				newMouthpiece.getFipple().getWindwayHeight(), false);
		addValues("Fipple Factor", oldMouthpiece.getFipple().getFippleFactor(),
				newMouthpiece.getFipple().getFippleFactor(), false);
	}

}
