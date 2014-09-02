/**
 * Generic interface for comparing target and predicted performance.
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

import com.wwidesigner.note.Fingering;

/**
 * Generic interface to a function that returns the difference
 * between the target performance of an instrument
 * with specified fingerings, and the predicted performance
 * for those fingerings.
 * 
 * @author Burton Patkau
 *
 */
public interface EvaluatorInterface
{
	/**
	 * Calculate the (signed) difference, in some measure,
	 * between target performance for each fingering in a list,
	 * and the predicted performance for that fingering.
	 * Interpretation depends on the implementation class.
	 * @param fingering  - Fingering and target note.
	 * @return difference between target and predicted performance.
	 * 			length = fingeringTargets.size().
	 */
	public abstract double[] calculateErrorVector(List<Fingering> fingeringTargets);
}