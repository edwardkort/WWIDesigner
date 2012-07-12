package com.wwidesigner.optimization;

import org.apache.commons.math3.analysis.MultivariateFunction;

public interface OptimizationFunctionInterface extends MultivariateFunction
{

	public abstract double calculateErrorNorm();

}