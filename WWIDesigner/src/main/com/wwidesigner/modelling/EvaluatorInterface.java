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