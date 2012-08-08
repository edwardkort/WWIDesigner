/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class DefaultBoreSectionCalculator extends BoreSectionCalculator
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.BoreSectionCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(BoreSection section,
			double waveNumber, PhysicalParameters params)
	{
		double leftRadius = section.getLeftRadius();
		double rightRadius = section.getRightRadius();
		double length = section.getLength();
		double ZcLeft = params.calcZ0(leftRadius);

		if (leftRadius == rightRadius) // the case of a cylindrical segment
		{
			double alpha = (1 / leftRadius) * Math.sqrt(waveNumber)
					* params.getAlphaConstant();

			// Gamma = alpha + i*omega/v
			//    alpha =~ omega/c * epsilon
			//    omega/v =~ omega/c ( 1 + epsilon ) = omega/c + alpha
			// Gamma = i * omega/c + (1+i) * alpha
			Complex Gamma = Complex.I.multiply(waveNumber).add(
					Complex.valueOf(1, 1).multiply(alpha));

			Complex sinhL = Gamma.multiply(length).sinh();
			Complex coshL = Gamma.multiply(length).cosh();

			TransferMatrix tm = new TransferMatrix(coshL, sinhL.multiply(ZcLeft),
					sinhL.divide(ZcLeft), coshL);
			assert tm.determinant() == Complex.valueOf(1.0,0.0);
			return tm;
		}

		// the case of a cone
		double ZcRight = params.calcZ0(rightRadius);

		double one_over_x_in = (rightRadius - leftRadius)
				/ (leftRadius * length);
		double one_over_x_out = (rightRadius - leftRadius)
				/ (rightRadius * length);

		// inverse of the equivalent radius at which we calculate the losses
		double one_over_Req = Math.log(rightRadius / leftRadius)
				/ (rightRadius - leftRadius);

		Complex k_lossy = Complex.valueOf(1, -1).multiply(
				one_over_Req * Math.sqrt(waveNumber) * params.getAlphaConstant()).add(waveNumber);

		Complex k_lossy_L = k_lossy.multiply(length);

		Complex A = k_lossy_L.cos().multiply(rightRadius / leftRadius)
				.subtract( k_lossy_L.sin().multiply(one_over_x_in).divide(k_lossy) );
		Complex B = k_lossy_L.sin().multiply(Complex.I)
				.multiply((rightRadius / leftRadius) * ZcRight);
		Complex C = Complex.valueOf(rightRadius / leftRadius)
				.multiply(Complex.I.multiply(k_lossy_L.sin()).multiply(
						k_lossy.pow(-2).multiply(one_over_x_in*one_over_x_out).add(1) ).add(
								k_lossy_L.cos().multiply(one_over_x_in - one_over_x_out).divide(
										Complex.I.multiply(k_lossy))))
				.divide(ZcLeft);
		Complex D = k_lossy_L.cos().multiply(leftRadius / rightRadius)
				.add(k_lossy_L.sin().multiply(one_over_x_out).divide(k_lossy));

		TransferMatrix tm = new TransferMatrix(A, B, C, D); 
		assert tm.determinant() == Complex.valueOf(1.0,0.0);
		return tm;
	}

}
