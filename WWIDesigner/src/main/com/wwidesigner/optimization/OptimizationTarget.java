package com.wwidesigner.optimization;

public class OptimizationTarget
{
	private double mFrequency;
	private Fingering mFingering;
	
	public OptimizationTarget(double frequency, Fingering fingering)
	{
		mFrequency = frequency;
		mFingering = fingering;
	}

	public double getFrequency()
	{
	    return mFrequency;
	}

	public Fingering getFingering()
	{
        return mFingering;
    }

}
