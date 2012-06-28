package com.wwidesigner.geometry;

import com.wwidesigner.math.StateVector;
import com.wwidesigner.util.PhysicalParameters;

interface TerminationInterface
{
	public StateVector calcStateVector(double wave_number, PhysicalParameters params);	
}