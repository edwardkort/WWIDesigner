/**
 * Mouthpiece calculator for a fipple mouthpiece.
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

import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import com.wwidesigner.geometry.BoreSection;
import com.wwidesigner.geometry.ComponentInterface;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.math.StateVector;
import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Mouthpiece calculation for a fipple mouthpiece, modeling
 * the window as a (short) tube with area equal to the window area
 * and flanged open end.
 * @author Burton Patkau
 * 
 */
public class SimpleFippleMouthpieceCalculator extends MouthpieceCalculator
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcTransferMatrix(double,
	 * com.wwidesigner.util.PhysicalParameters)
	 */
	@Override
	protected TransferMatrix calcTransferMatrix(Mouthpiece mouthpiece,
			double waveNumber, PhysicalParameters parameters)
	{
		double freq = parameters.calcFrequency(waveNumber);
		
		Complex Zwindow = calcZ(mouthpiece, freq, parameters);
		
		return new TransferMatrix(Complex.ONE, Zwindow, Complex.ZERO, Complex.ONE);		
	}
	
	@Override
	public StateVector calcStateVector(StateVector boreState,
			Mouthpiece mouthpiece, double waveNumber,
			PhysicalParameters parameters)
	{
		StateVector sv = boreState;
		List<BoreSection> headspace = mouthpiece.getHeadspace();
		if (headspace.size() > 0)
		{
			// If we have headspace, assume a closed upper end,
			// and multiply by transfer matrices of each
			// bore segment in the headspace.

			StateVector headspaceState = StateVector.ClosedEnd();
			TransferMatrix tm;
			for (int componentNr = 0; componentNr < headspace.size(); ++componentNr)
			{
				ComponentInterface component = headspace.get(componentNr);
				assert component instanceof BoreSection;
				BoreSection section = (BoreSection) component;
				tm = Tube.calcConeMatrix(waveNumber, section.getLength(),
						section.getRightRadius(), section.getLeftRadius(),
						parameters);
				headspaceState = tm.multiply(headspaceState);
			}

			// Assume the mouthpiece sees the bore impedance in parallel with
			// the headspace impedance.
			sv = boreState.parallel(headspaceState);
		}
		sv = calcTransferMatrix(mouthpiece, waveNumber, parameters)
				.multiply(sv);
		return sv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.wwidesigner.geometry.MouthpieceCalculator#calcReflectanceMultiplier()
	 */
	@Override
	public int calcReflectanceMultiplier()
	{
		return -1;
	}

	/**
	 * Calculate the impedance of the whistle window, at specified frequency.
	 * @param mouthpiece
	 * @param freq
	 * @param physicalParams
	 * @return Complex impedance of whistle window.
	 */
	public Complex calcZ(Mouthpiece mouthpiece,
			double freq, PhysicalParameters physicalParams)
	{
		// Reactance modeled from measurements of real whistles.
		double effSize = FastMath.sqrt(mouthpiece.getFipple().getWindowLength()
				* mouthpiece.getFipple().getWindowWidth() );
		// Model for use in absence of blade height measurement.
		double windowHeight;
		if (mouthpiece.getFipple().getWindowHeight() != null)
		{
			windowHeight = mouthpiece.getFipple().getWindowHeight();
		}
		else if (mouthpiece.getFipple().getWindwayHeight() != null)
		{
			windowHeight = mouthpiece.getFipple().getWindwayHeight();
		}
		else
		{
			windowHeight = 0.001;	// Default to 1 mm.
		}
		double Xw = physicalParams.getRho() * freq/effSize
				* ( 7.345 - 2.18e-4 * freq
					- 3.95e-3 / windowHeight );
		// Model for use when blade height measurement is available.
		// double Xw = physicalParams.getRho() * freq/effSize
		// 		* ( 5.824 - 2.76e-4 * freq
		//				+ 2.04 * mouthpiece.getFipple().getWindowHeight()/effSize
		//				- 3.36 * mouthpiece.getFipple().getBladeHeight()/mouthpiece.getFipple().getWindwayHeight());
		
		// Resistance modeled as short cylindrical tube with same area as window.
		double Rw = physicalParams.getRho()
				* ( 6.42 * freq*freq/physicalParams.getSpeedOfSound()
						+ 0.0184 * FastMath.sqrt(freq)*windowHeight
							/ (effSize*effSize*effSize));
		return new Complex(Rw,Xw);
	}

	public Complex calcZ_old(Mouthpiece mouthpiece,
			double freq, PhysicalParameters physicalParams)
	{
		// Assume the open window acts as a flanged tube with an effective radius
		// that corresponds to the window area.
		double effRadius = FastMath.sqrt(mouthpiece.getFipple().getWindowLength()
				* mouthpiece.getFipple().getWindowWidth() / FastMath.PI );
		double waveNumber = physicalParams.calcWaveNumber(freq);

		StateVector sv = new StateVector(
				Tube.calcZflanged( freq, effRadius, physicalParams ),
				Complex.ONE );
		TransferMatrix tm = Tube.calcCylinderMatrix(waveNumber,
				mouthpiece.getFipple().getWindowHeight(), 
				effRadius, physicalParams );
		sv = tm.multiply( sv );
		return sv.getImpedance();
	}

	@Override
	public Double calcGain(Mouthpiece mouthpiece,
			double freq, Complex Z,
			PhysicalParameters physicalParams)
	{
		double radius = mouthpiece.getBoreDiameter() / 2.;
		double waveNumber = physicalParams.calcWaveNumber(freq);
		return mouthpiece.getGainFactor() * waveNumber * radius*radius
				/ Z.abs();
	}
}
