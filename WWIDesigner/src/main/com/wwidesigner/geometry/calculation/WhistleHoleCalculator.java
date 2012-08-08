/**
 * 
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.geometry.Hole;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * For bare toneholes covered by fingers, this hole calculator
 * assumes that closed toneholes are at least partially filled
 * by finger pad, reducing or eliminating their compliance volume.
 * Based on Transfer Matrix model of Antoine Lefebvre.
 * @author Edward Kort, Antoine Lefebvre, Burton Patkau
 * 
 */
public class WhistleHoleCalculator extends HoleCalculator
{
	// For bare (key-less) toneholes, assume the player's finger
	// occupies a fixed length of the tonehole.
	private static double AssumedFingerSize = 0.002;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	public TransferMatrix calcTransferMatrix(Hole hole,
			double waveNumber, PhysicalParameters parameters)
	{
		double radius = hole.getDiameter() / 2;
		double boreRadius = hole.getBoreDiameter() / 2;
		Complex Ys = Complex.ZERO;	// 1/Zs
		Complex Za = Complex.ZERO;

		// double Z0 = parameters.calcZ0(boreRadius);
		double Z0h = parameters.calcZ0(radius);

		double delta = radius / boreRadius;

		double tm = (radius * delta / 8.)
				* (1. + 0.207 * delta * delta * delta);
		double te = hole.getHeight() + tm;

		double ta = 0.;

		// Complex Gamma = Complex.I.multiply(wave_number);

		if (hole.isOpenHole()) // open
		{
			double kb = waveNumber * radius;
			double ka = waveNumber * boreRadius;
			double xhi = 0.25 * kb * kb;

			ta = (-0.35 + 0.06 * Math.tanh(2.7 * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;

			Complex Zr = Complex.I.multiply(waveNumber * 0.61 * radius)
					.add(xhi);

			Complex Zo = (Zr.multiply(Math.cos(waveNumber * te)).add(Complex.I
					.multiply(Math.sin(waveNumber * te)))).divide(Complex.I
					.multiply(Zr).multiply(Math.sin(waveNumber * te))
					.add(Math.cos(waveNumber * te)));

			double ti = radius
					* (0.822 - 0.10 * delta - 1.57 * delta * delta + 2.14
							* delta * delta * delta - 1.6 * delta * delta
							* delta * delta + 0.50 * delta * delta * delta
							* delta * delta)
					* (1. + (1. - 4.56 * delta + 6.55 * delta * delta)
							* (0.17 * ka + 0.92 * ka * ka + 0.16 * ka * ka * ka - 0.29
									* ka * ka * ka * ka));

			Ys = Complex.ONE.divide( Complex.I.multiply(waveNumber * ti).add(Zo).multiply(Z0h) );

		}
		else if ( hole.getKey() == null )
		{
			// Tonehole closed by player's finger.
			if ( hole.getHeight() <= AssumedFingerSize )
			{
				// Finger is likely to fill the hole.  Ignore the hole entirely.
				ta = 0.;
				Ys = Complex.ZERO;
			}
			else {
				ta = (-0.12 - 0.17 * Math.tanh(2.4 * ( hole.getHeight() - AssumedFingerSize ) / radius))
						* radius * delta * delta * delta * delta;
				Ys = Complex.valueOf( 0, 
						- Math.tan(waveNumber * (te-AssumedFingerSize)) / Z0h );
			}
		}
		else
		{
			// Tonehole closed by key.
			ta = (-0.12 - 0.17 * Math.tanh(2.4 * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;
			Ys = Complex.valueOf( 0, - Math.tan(waveNumber * te) / Z0h );
		}

		Za = Complex.I.multiply(Z0h * waveNumber * ta);
		Complex Za_Zs = Za.multiply(Ys);

		Complex A = Za_Zs.divide(2.).add(1.);
		Complex B = Za.multiply(Za_Zs.divide(4.).add(1.));
		Complex C = Ys;
		// Choose A and D to make the determinant = 1.
		// Complex A = Complex.ONE.add(B.multiply(C)).sqrt();
		TransferMatrix result = new TransferMatrix( A, B, C, A );
		
		assert result.determinant() == Complex.valueOf(1.0,0.0);

		return result;
	}

}
