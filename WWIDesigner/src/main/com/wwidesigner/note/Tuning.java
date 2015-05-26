/**
 * Class describing an instrument's tuning pattern.
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
package com.wwidesigner.note;

import java.util.List;

import com.wwidesigner.util.InvalidFieldException;
import com.wwidesigner.util.InvalidFieldHandler;

/**
 * @author kort
 * 
 */
public class Tuning extends FingeringPattern implements TuningInterface
{
	public void checkValidity() throws InvalidFieldException
	{
		InvalidFieldHandler handler = new InvalidFieldHandler("Tuning");
		if (name.isEmpty())
		{
			handler.logError("Enter a name for the tuning.");
		}
		if (fingering.size() == 0)
		{
			handler.logError("Enter one or more notes for the tuning.");
		}
		for (int i = 0; i < fingering.size(); ++i)
		{
			Note note = fingering.get(i).getNote();
			if (note == null)
			{
				handler.logError("Missing note in row " + (i + 1) + ".");
			}
			else {
				String name = note.getName();
				if (name == null || name.isEmpty())
				{
					handler.logError("Enter a note name for row " + (i + 1) + ".");
					name = "note";
				}
				List<Boolean> holes = fingering.get(i).getOpenHole();
				if (holes == null)
				{
					handler.logError("Missing fingering for " + name + " in row "
							+ (i + 1) + ".");
				}
				if (holes.size() != numberOfHoles)
				{
					handler.logError( "Fingering for "
							+ name + " in row " + (i + 1)
							+ " has wrong number of holes.");
				}
				if (note.getFrequency() == null && note.getFrequencyMax() == null
						&& note.getFrequencyMin() == null)
				{
					handler.logError("Enter at least one frequency for " + name
							+ " in row " + (i + 1) + ".");
				}
				if (note.getFrequency() != null && note.getFrequency() <= 0
						|| note.getFrequencyMax() != null
						&& note.getFrequencyMax() <= 0
						|| note.getFrequencyMin() != null
						&& note.getFrequencyMin() <= 0)
				{
					handler.logError("Frequency for " + name + " in row " + (i + 1)
							+ " must be positive.");
				}
			}
		}
		handler.reportErrors(false);
	}
}
