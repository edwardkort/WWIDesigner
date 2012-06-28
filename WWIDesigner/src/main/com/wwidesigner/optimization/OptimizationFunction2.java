package com.wwidesigner.optimization;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.note.TuningInterface;
import com.wwidesigner.note.bind.XmlFingering;
import com.wwidesigner.util.PhysicalParameters;

public class OptimizationFunction2 implements MultivariateFunction
{
	private OptimizableInstrument2 instrument;
	private List<XmlFingering> fingeringTargets;
	private PhysicalParameters physicalParams;

	public OptimizationFunction2(OptimizableInstrument2 instrument,
			TuningInterface tuning, PhysicalParameters physicalParameters)
	{
		this.instrument = instrument;
		fingeringTargets = tuning.getFingering();
		physicalParams = physicalParameters;
	}

	@Override
	public double value(double[] state_vector)
	{
		instrument.updateGeometry(state_vector);
		return calculateErrorNorm();
	}

	public double calculateErrorNorm()
	{
		double norm = 0.;
		for (XmlFingering target : fingeringTargets)
		{
			Complex reflectionCoeff = instrument.getBaseInstrument()
					.calculateReflectionCoefficient(target, physicalParams);
			double reflectance_angle = reflectionCoeff.getArgument();
			norm += reflectance_angle * reflectance_angle;
		}
		return norm;
	}

}
