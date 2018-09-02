/**
 * Mouthpiece calculator for a reed mouthpiece.
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
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Trial mouthpiece calculator for all reed mouthpieces.
 * Models reactance adjustment as a linear function of frequency.
 * 
 * @author Burton Patkau
 * 
 */
public class SimpleReedMouthpieceCalculator extends MouthpieceCalculator
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	protected TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		if (! mouthpiece.isPressureNode())
		{
			// Resort to default if this is not a pressure-node mouthpiece.
			return super.calcTransferMatrix(mouthpiece, waveNumber, parameters);
		}

		double freq = parameters.calcFrequency(waveNumber);
		double alpha = 0.0d;
		Double beta = mouthpiece.getBeta();
		if (beta == null)
		{
			beta = 0.0d;
		}
//		double reedResonance = 440.0;
		if (mouthpiece.getSingleReed() != null)
		{
			alpha = mouthpiece.getSingleReed().getAlpha();
		}
		else if (mouthpiece.getDoubleReed() != null)
		{
			alpha = mouthpiece.getDoubleReed().getAlpha();
//			reedResonance = mouthpiece.getDoubleReed().getCrowFreq();
		}
		else if (mouthpiece.getLipReed() != null)
		{
			// For lip reeds, adjustment factor is negative.
			alpha = + mouthpiece.getLipReed().getAlpha();
			beta = - beta;
		}

		double headRadius = 0.5d * mouthpiece.getBoreDiameter();
		double z0 = parameters.calcZ0(headRadius);
		double X = alpha*1.0e-3d*freq + beta;
        TransferMatrix closedEnd = new TransferMatrix(
        		new Complex(0.0d, X), new Complex(z0, 0.0d), 
        		new Complex(1.0d, 0.0d), Complex.ZERO);
		return closedEnd;
	}
	
}
