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

/**
 * @author kort
 * 
 */
public class Tuning extends FingeringPattern implements TuningInterface
{
	public void checkValidity() throws InvalidFieldException
	{
		if (name.isEmpty())
		{
			throw new InvalidFieldException("Tuning",
					"Enter a name for the tuning.");
		}
		if (fingering.size() == 0)
		{
			throw new InvalidFieldException("Tuning",
					"Enter one or more notes for the tuning.");
		}
		for (int i = 0; i < fingering.size(); ++i)
		{
			Note note = fingering.get(i).getNote();
			if (note == null)
			{
				throw new InvalidFieldException("Tuning",
						"Missing note in row " + (i + 1) + ".");
			}
			String name = note.getName();
			if (name == null || name.isEmpty())
			{
				throw new InvalidFieldException("Tuning",
						"Enter a note name for row " + (i + 1) + ".");
			}
			List<Boolean> holes = fingering.get(i).getOpenHole();
			if (holes == null)
			{
				throw new InvalidFieldException("Tuning",
						"Missing fingering for " + name + " in row " + (i + 1)
								+ ".");
			}
			if (holes.size() != numberOfHoles)
			{
				throw new InvalidFieldException("Tuning", "Fingering for "
						+ name + " in row " + (i + 1)
						+ " has wrong number of holes.");
			}
			Integer weight = fingering.get(i).getOptimizationWeight();
			if (weight != null && weight < 0)
			{
				throw new InvalidFieldException("Tuning",
						"Optimization weight for " + name + " in row "
								+ (i + 1) + " must not be negative.");
			}
			if (note.getFrequency() == null && note.getFrequencyMax() == null
					&& note.getFrequencyMin() == null)
			{
				throw new InvalidFieldException("Tuning",
						"Enter at least one frequency for " + name + " in row "
								+ (i + 1) + ".");
			}
			if (note.getFrequency() != null && note.getFrequency() <= 0
					|| note.getFrequencyMax() != null
					&& note.getFrequencyMax() <= 0
					|| note.getFrequencyMin() != null
					&& note.getFrequencyMin() <= 0)
			{
				throw new InvalidFieldException("Tuning", "Frequency for "
						+ name + " in row " + (i + 1) + " must be positive.");
			}
		}
	}
}
