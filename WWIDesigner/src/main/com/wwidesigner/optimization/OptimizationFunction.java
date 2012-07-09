package com.wwidesigner.optimization;

import java.util.List;

//import com.wwidesigner.optimization.OptimizationStudy;
import com.wwidesigner.util.PhysicalParameters;
import com.wwidesigner.util.Constants.TemperatureType;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.complex.Complex;

public class OptimizationFunction implements MultivariateFunction
{
	private OptimizableInstrument mInstrument;
    private List<OptimizationTarget> mTargets;
    private PhysicalParameters mAir;
    
    public OptimizationFunction(OptimizableInstrument instrument, List<OptimizationTarget> targets)
    {
    	mInstrument = instrument;
    	mTargets = targets;
    	mAir = new PhysicalParameters(25., TemperatureType.C);
    }

	@Override
	public double value(double[] state_vector)
	{        
		updateGeometry(state_vector);	
		double error = calculateErrorNorm();
		
		return error;
	}
	
	public List<OptimizationTarget> getTargets()
	{
	    return mTargets;
	}
	
	public Complex calculateReflectionCoefficient(OptimizationTarget target)
	{
		double f = target.getFrequency();
		double k = 2*Math.PI*f/mAir.getSpeedOfSound();

		return mInstrument.calculateReflectionCoefficient(target.getFingering(), k);
	}

	public double calculateErrorNorm()
	{
		double norm = 0.;
		for (OptimizationTarget target: mTargets)
		{		
            double reflectance_angle = calculateReflectionCoefficient(target).getArgument();
	        norm += reflectance_angle*reflectance_angle;			
		}
		return norm;	
	}

	public void updateGeometry(double[] state_vector)
	{
		mInstrument.updateGeometry(state_vector);		
	}
}
