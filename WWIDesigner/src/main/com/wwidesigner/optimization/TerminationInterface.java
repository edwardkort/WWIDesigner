package com.wwidesigner.optimization;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

interface TerminationInterface
{
	public StateVector stateVector(double wave_number, PhysicalParameters params);	
}