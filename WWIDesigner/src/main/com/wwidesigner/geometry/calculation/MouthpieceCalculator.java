/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class MouthpieceCalculator
{
	public MouthpieceCalculator()
	{
	}

	/**
	 * Calculate Transfer Matrix that represents effect of mouthpiece
	 * in series with the bore.
	 * @param mouthpiece - instrument mouthpiece description.
	 * @param waveNumber - k = 2*pi*f/c
	 * @param parameters
	 * @return TM for effect of mouthpiece
	 */
	protected TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		// Default mouthpiece is a pure open end for flow-node mouthpiece,
		// and a pure closed end for a pressure-node mouthpiece.
		if (mouthpiece.isPressureNode())
		{
			double headRadius = mouthpiece.getBoreDiameter() / 2.;
			double z0 = parameters.calcZ0(headRadius);
	        return new TransferMatrix(Complex.ZERO, new Complex(z0, 0), Complex.ONE.divide(z0), Complex.ZERO);
		}
		return TransferMatrix.makeIdentity();
	}

	/**
	 * For flow-node mouthpiece, return [P, U] as seen by driving source.
	 * For pressure-node mouthpiece, return [Z0*U, P/Z0] as seen by driving source.
	 * 
	 * @param boreState - [P, U] of bore, as seen by mouthpiece.
	 * @param mouthpiece - instrument mouthpiece description.
	 * @param waveNumber - k = 2*pi*f/c
	 * @param parameters
	 * @return State vector seen by driving source.
	 */
	public StateVector calcStateVector(StateVector boreState, Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		// Default state vector calculation assumes the transfer matrix includes
		// the effect of any headspace. 
		return calcTransferMatrix(mouthpiece, waveNumber, parameters).multiply(boreState);
	}
}
