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
public abstract class MouthpieceCalculator
{
	public MouthpieceCalculator()
	{
	}

	protected abstract TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters);
	
	public StateVector calcStateVector(StateVector boreState, Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		// Default state vector calculation assumes the transfer matrix includes
		// the effect of any headspace. 
		return calcTransferMatrix(mouthpiece, waveNumber, parameters).multiply(boreState);
	}

	public abstract int calcReflectanceMultiplier();

	public abstract Double calcGain(Mouthpiece mouthpiece,
			double freq, Complex Z, PhysicalParameters physicalParams);
}
