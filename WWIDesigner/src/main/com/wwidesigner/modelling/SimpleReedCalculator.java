package com.wwidesigner.modelling;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.ComponentInterface;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.calculation.DefaultBoreSectionCalculator;
import com.wwidesigner.geometry.calculation.DefaultHoleCalculator;
import com.wwidesigner.geometry.calculation.NoOpReedMouthpieceCalculator;
import com.wwidesigner.geometry.calculation.ThickFlangedOpenEndCalculator;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

public class SimpleReedCalculator extends DefaultInstrumentCalculator
{

	public SimpleReedCalculator(Instrument instrument)
	{
		super(instrument, new NoOpReedMouthpieceCalculator(),
				new ThickFlangedOpenEndCalculator(),
				new DefaultHoleCalculator(), new DefaultBoreSectionCalculator());
	}

	// TODO: Remove this override.
	// ChalumeauOptimizationTest is highly sensitive to floating-point
	// inaccuracy, and passes only with this method for calculating  
	// the reflection coefficient.

	@Override
	public Complex calculateReflectionCoefficient(double frequency,
			PhysicalParameters physicalParams)
	{
		double waveNumber = 2 * Math.PI * frequency
				/ physicalParams.getSpeedOfSound();

		TransferMatrix transferMatrix = TransferMatrix.makeIdentity();
		TransferMatrix tm;

		for (ComponentInterface component : instrument.getComponents())
		{
			if (component instanceof BoreSection)
			{
				tm = boreSectionCalculator.calcTransferMatrix((BoreSection) component,
						waveNumber, physicalParams);
			}
			else if (component instanceof Hole)
			{
				tm = holeCalculator.calcTransferMatrix((Hole) component,
						waveNumber, physicalParams);
			}
			else
			{
				assert component instanceof Mouthpiece;
				tm = mouthpieceCalculator.calcTransferMatrix((Mouthpiece) component,
						waveNumber, physicalParams);
			}
			transferMatrix = TransferMatrix.multiply(transferMatrix, tm);
		}

		StateVector sv = TransferMatrix.multiply(transferMatrix,
				terminationCalculator.calcStateVector(instrument.getTermination(), waveNumber, physicalParams));

		// TODO This mouthpiece calculation will change
		double headRadius = instrument.getMouthpiece().getBoreDiameter() / 2.;
		double characteristic_impedance = physicalParams.calcZ0(headRadius);
		Complex reflectance = sv.Reflectance(characteristic_impedance);
		return reflectance;
	}
}
