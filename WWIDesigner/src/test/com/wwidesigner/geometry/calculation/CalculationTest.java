/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.math3.complex.Complex;
import org.junit.Test;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class CalculationTest
{
	static final double BaseFrequency = 587.33;	// Reference frequency, D5.
	static final double BaseRadius = 0.006; // Reference radius, in meters.
	static final double BaseLength = 0.250; // Reference radius, in meters.

	@Test
	public final void testImpedance()
	{
		try
		{
			PhysicalParameters parameters = new PhysicalParameters(25.,
					TemperatureType.C);
			
			double z0 = parameters.calcZ0(BaseRadius);
			
			Complex zLoad = Tube.calcZload(BaseFrequency, BaseRadius, parameters).divide(z0);
			assertEquals("Re(Z) incorrect", 0.00101768, zLoad.getReal(), 1.0e-6);
			assertEquals("Im(Z) incorrect", 0.039, zLoad.getImaginary(), 0.0001);

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testCylinder()
	{
		try
		{
			PhysicalParameters parameters = new PhysicalParameters(25.,
					TemperatureType.C);
			double z0 = parameters.calcZ0(BaseRadius);
			double waveNumber = parameters.calcWaveNumber(BaseFrequency);

			TerminationCalculator term = new IdealOpenEndCalculator();
			StateVector sv = term.calcStateVector(null, waveNumber, parameters);
			
			TransferMatrix tm = Tube.calcCylinderMatrix(waveNumber, BaseLength, BaseRadius, parameters);
			assertEquals("Determinant incorrect", 1.0, tm.determinant().getReal(), 0.0001);
			assertEquals("Determinant.imag incorrect", 0.0, tm.determinant().getImaginary(), 0.0001);
			Complex zLoad = tm.multiply(sv).Impedance().divide(z0);

			assertEquals("Re(Z) incorrect",  0.03712, zLoad.getReal(), 0.00001);
			assertEquals("Im(Z) incorrect", -0.48647, zLoad.getImaginary(), 0.00001);

			BoreSectionCalculator boreCalc = new DefaultBoreSectionCalculator();
			BoreSection bore = new BoreSection(BaseLength, BaseRadius, BaseRadius);
			TransferMatrix tm2 = boreCalc.calcTransferMatrix(bore, waveNumber, parameters);
			assertEquals("Determinant 2 incorrect", 1.0, tm2.determinant().getReal(), 0.0001);
			assertEquals("Determinant.imag 2 incorrect", 0.0, tm2.determinant().getImaginary(), 0.0001);
			Complex zLoad2 = tm2.multiply(sv).Impedance().divide(z0);

			assertEquals("Re(Z2) incorrect",  0.03712, zLoad2.getReal(), 0.00001);
			assertEquals("Im(Z2) incorrect", -0.48647, zLoad2.getImaginary(), 0.00001);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public final void testCone()
	{
		try
		{
			PhysicalParameters parameters = new PhysicalParameters(25.,
					TemperatureType.C);
			double z0 = parameters.calcZ0(BaseRadius);
			double waveNumber = parameters.calcWaveNumber(BaseFrequency);

			TerminationCalculator term = new IdealOpenEndCalculator();
			StateVector sv = term.calcStateVector(null, waveNumber, parameters);
			
			TransferMatrix tm = Tube.calcConeMatrix(waveNumber, BaseLength, BaseRadius, 0.75 * BaseRadius, parameters);
			assertEquals("Determinant incorrect", 1.0, tm.determinant().getReal(), 0.0001);
			assertEquals("Determinant.imag incorrect", 0.0, tm.determinant().getImaginary(), 0.0001);
			Complex zLoad = tm.multiply(sv).Impedance().divide(z0);

			assertEquals("Re(Z) incorrect",  0.03871, zLoad.getReal(), 0.00001);
			assertEquals("Im(Z) incorrect", -0.46038, zLoad.getImaginary(), 0.00001);

			BoreSectionCalculator boreCalc = new DefaultBoreSectionCalculator();
			BoreSection bore = new BoreSection(BaseLength, BaseRadius, 0.75 * BaseRadius);
			TransferMatrix tm2 = boreCalc.calcTransferMatrix(bore, waveNumber, parameters);
			assertEquals("Determinant 2 incorrect", 1.0, tm2.determinant().getReal(), 0.0001);
			assertEquals("Determinant.imag 2 incorrect", 0.0, tm2.determinant().getImaginary(), 0.0001);
			Complex zLoad2 = tm2.multiply(sv).Impedance().divide(z0);

			assertEquals("Re(Z2) incorrect",  0.03871, zLoad2.getReal(), 0.00001);
			assertEquals("Im(Z2) incorrect", -0.46038, zLoad2.getImaginary(), 0.00001);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}
