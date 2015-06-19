/**
 * Calculator to compute the transfer matrix of a soundhole in a round tube.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

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
	// occupies a fixed length of the tonehole, in meters.
	private static double AssumedFingerSize = 0.000;
	
	// End-correction applied to open toneholes,
	// typically 0.61 for unflanged holes,
	// up to 0.85 for flanged holes.
	
	private static double RadiationEndCorrection = 0.61;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 *
	 * Reference:
	 * Antoine Lefebvre, Computational Acoustic Methods for the Design of
     * Woodwind Instruments.  Ph.D. thesis, McGill University, 2010.
	 */
	@Override
	public TransferMatrix calcTransferMatrix(Hole hole,
			double waveNumber, PhysicalParameters parameters)
	{
		double radius = hole.getDiameter() / 2;
		double boreRadius = hole.getBoreDiameter() / 2;
		double delta = radius / boreRadius;

		Complex Ys = Complex.ZERO;	// 1/Zs
		Complex Za = Complex.ZERO;
		// double Z0 = parameters.calcZ0(boreRadius);
		double Z0h = parameters.calcZ0(radius);
		double ta = 0.;

		if (hole.isOpenHole()) // open
		{
			double fx = -0.044+delta*(0.269+delta*(-1.519+delta*(2.332+delta*(-1.897+delta*0.560))));
			double gx = 1 - FastMath.tanh(0.788*hole.getHeight()/radius);
			double hx = 1.643+delta*(-0.684+delta*(0.182+delta*(-0.394+delta*(0.295-delta*0.063))));
			double te = hole.getHeight() + radius * ( 1 + fx*gx ) * hx;
			double kb = waveNumber * radius;
			double xhi = 0.25 * kb * kb;

			fx = 1 + (0.261-delta*0.022)*(1-FastMath.tanh(2.364*hole.getHeight()/radius));
			gx = 0.302+delta*(-0.010-delta*0.006);
			ta = - fx * gx * radius * delta * delta * delta * delta;

			Ys = Complex.ONE.divide( Complex.I.multiply(FastMath.tan(waveNumber * te)).add(xhi).multiply(Z0h) );

		}
		else
		{
			double delta_t = radius/(boreRadius + hole.getHeight());
			double tm = (radius*delta/8.) * (1. + 0.207 * delta*delta*delta)
						- (radius*delta_t/8.) * (1. + 0.207 * delta_t*delta_t*delta_t);
			double te = hole.getHeight() + tm;

			// Tonehole closed by player's finger.
			if ( hole.getHeight() <= AssumedFingerSize )
			{
				// Finger is likely to fill the hole.  Ignore the hole entirely.
				ta = 0.;
				Ys = Complex.ZERO;
			}
			else {
				double fx = 1 - (0.956 - 0.104*delta)*(1-FastMath.tanh(2.390*hole.getHeight()/radius));
				double gx = 0.299 + delta*(-0.018 + 0.006*delta);
				ta = (-fx*gx) * radius * delta*delta*delta*delta;
				Ys = Complex.valueOf( 0, 
						FastMath.tan(waveNumber * (te-AssumedFingerSize)) / Z0h );
			}
		}

		Za = Complex.I.multiply(Z0h * waveNumber * ta);
		Complex Za_Zs = Za.multiply(Ys);

		Complex A = Za_Zs.divide(2.).add(1.);
		Complex B = Za.multiply(Za_Zs.divide(4.).add(1.));
		Complex C = Ys;
		TransferMatrix result = new TransferMatrix( A, B, C, A );
		
		assert result.determinant() == Complex.valueOf(1.0,0.0);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wwidesigner.geometry.HoleCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 *
	 * Reference:
	 * Antoine Lefebvre and Gary P. Scavone, Characterization of woodwind instrument
	 * toneholes with the finite element method, J. Acoust. Soc. Am. V. 131 (n. 4), April 2012.
	 */
	//@Override
	public TransferMatrix calcTransferMatrix_2012(Hole hole,
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

			ta = (-0.35 + 0.06 * FastMath.tanh(2.7 * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;

			Complex Zr = Complex.I.multiply(waveNumber * RadiationEndCorrection * radius)
					.add(xhi);

			Complex Zo = (Zr.multiply(FastMath.cos(waveNumber * te)).add(Complex.I
					.multiply(FastMath.sin(waveNumber * te)))).divide(Complex.I
					.multiply(Zr).multiply(FastMath.sin(waveNumber * te))
					.add(FastMath.cos(waveNumber * te)));

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
				ta = (-0.12 - 0.17 * FastMath.tanh(2.4 * ( hole.getHeight() - AssumedFingerSize ) / radius))
						* radius * delta * delta * delta * delta;
				Ys = Complex.valueOf( 0, 
						FastMath.tan(waveNumber * (te-AssumedFingerSize)) / Z0h );
			}
		}
		else
		{
			// Tonehole closed by key.
			ta = (-0.12 - 0.17 * FastMath.tanh(2.4 * hole.getHeight() / radius))
					* radius * delta * delta * delta * delta;
			Ys = Complex.valueOf( 0, FastMath.tan(waveNumber * te) / Z0h );
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
